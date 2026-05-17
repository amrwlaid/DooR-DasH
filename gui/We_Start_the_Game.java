package gui;

import game.engine.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class We_Start_the_Game {

    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private RadioButton monster1;
    @FXML private RadioButton monster2;

    private ToggleGroup group = new ToggleGroup();
    @FXML private Label warningLabel;
    @FXML
    private void initialize() {
        monster1.setToggleGroup(group);
        monster2.setToggleGroup(group);
        updateSelection();
    }

    @FXML
    private void handleSection1Click(MouseEvent event) {
        monster1.setSelected(true);
        updateSelection();
    }

    @FXML
    private void handleSection2Click(MouseEvent event) {
        monster2.setSelected(true);
        updateSelection();
    }

    private void updateSelection() {
        circle1.setFill(monster1.isSelected() ? Color.BLUEVIOLET : Color.LIGHTGRAY);
        circle2.setFill(monster2.isSelected() ? Color.BLUEVIOLET : Color.LIGHTGRAY);
    }

    private String getSelectedMonster() {

        if (monster1.isSelected()) {
            return "SCARER";
        }

        if (monster2.isSelected()) {
            return "LAUGHER";
        }

        return null;
    }
    @FXML
    private void startGame() {
        String chosenMonster = getSelectedMonster();
        
        if (chosenMonster == null) {
            if (warningLabel != null) {
                warningLabel.setText("⚠️ Please select a monster before continuing!");
                warningLabel.setVisible(true);
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Parent root = loader.load();

            // 1. Get the controller
            GameController controller = loader.getController();

            // 2. Convert the String to the Role Enum [cite: 268]
            Role selectedRole = Role.valueOf(chosenMonster); 

            // 3. Pass the actual Role object to the controller
            controller.initialize(selectedRole);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("DooR DasH: Scare vs Laugh Touchdown"); // [cite: 1]
            stage.show();

            Stage current = (Stage) monster1.getScene().getWindow();
            current.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}