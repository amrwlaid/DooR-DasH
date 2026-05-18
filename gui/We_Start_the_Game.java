package gui;

import game.engine.Role;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class We_Start_the_Game {

    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private RadioButton monster1;
    @FXML private RadioButton monster2;
    private ToggleGroup group = new ToggleGroup();
    @FXML private Label warningLabel;
    @FXML private VBox rootBox; // optional: add fx:id="rootBox" to your root VBox in FXML

    @FXML
    private void initialize() {
        monster1.setToggleGroup(group);
        monster2.setToggleGroup(group);
        updateSelection();

        // â”€â”€ ANIMATION: circles idle pulse â”€â”€
        animateCirclePulse(circle1, 0);
        animateCirclePulse(circle2, 300);

        // â”€â”€ ANIMATION: fade in root on load â”€â”€
        if (rootBox != null) {
            rootBox.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(700), rootBox);
            ft.setFromValue(0); ft.setToValue(1);
            ft.play();
        }
    }

    /** Gentle idle pulse on unselected circles */
    private void animateCirclePulse(Circle circle, int delayMs) {
        if (circle == null) return;
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), circle);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.08); pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setDelay(Duration.millis(delayMs));
        pulse.play();
    }

    @FXML
    private void handleSection1Click(MouseEvent event) {
        monster1.setSelected(true);
        updateSelection();
        animateCircleSelect(circle1, Color.BLUEVIOLET);
        animateCircleDeselect(circle2);
    }

    @FXML
    private void handleSection2Click(MouseEvent event) {
        monster2.setSelected(true);
        updateSelection();
        animateCircleSelect(circle2, Color.BLUEVIOLET);
        animateCircleDeselect(circle1);
    }

    /** Pop scale animation on the selected circle */
    private void animateCircleSelect(Circle c, Color color) {
        if (c == null) return;
        c.setFill(color);
        ScaleTransition pop = new ScaleTransition(Duration.millis(150), c);
        pop.setFromX(1.0); pop.setFromY(1.0);
        pop.setToX(1.25); pop.setToY(1.25);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();
    }

    /** Shrink + grey out the deselected circle */
    private void animateCircleDeselect(Circle c) {
        if (c == null) return;
        c.setFill(Color.LIGHTGRAY);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), c);
        shrink.setToX(1.0); shrink.setToY(1.0);
        shrink.play();
    }

    private void updateSelection() {
        circle1.setFill(monster1.isSelected() ? Color.BLUEVIOLET : Color.LIGHTGRAY);
        circle2.setFill(monster2.isSelected() ? Color.BLUEVIOLET : Color.LIGHTGRAY);
    }

    private String getSelectedMonster() {
        if (monster1.isSelected()) return "SCARER";
        if (monster2.isSelected()) return "LAUGHER";
        return null;
    }

    @FXML
    private void startGame() {
        String chosenMonster = getSelectedMonster();

        if (chosenMonster == null) {
            if (warningLabel != null) {
                warningLabel.setText("âš ï¸� Please select a monster before continuing!");
                warningLabel.setVisible(true);
                // â”€â”€ ANIMATION: shake the warning label â”€â”€
                TranslateTransition shake = new TranslateTransition(Duration.millis(60), warningLabel);
                shake.setFromX(0); shake.setByX(8);
                shake.setAutoReverse(true);
                shake.setCycleCount(6);
                shake.play();
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Parent root = loader.load();

            GameController controller = loader.getController();
            Role selectedRole = Role.valueOf(chosenMonster);
            controller.initialize(selectedRole);

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            // â”€â”€ ANIMATION: fade in the game scene â”€â”€
            root.setOpacity(0);
            stage.setScene(scene);
            stage.setTitle("DooR DasH: Scare vs Laugh Touchdown");
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();

            // Close start screen with fade out
            Stage current = (Stage) monster1.getScene().getWindow();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), current.getScene().getRoot());
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> current.close());
            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
