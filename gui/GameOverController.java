package gui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameOverController {

    @FXML private Label statusIconLabel;
    @FXML private Label headerTitleLabel;
    @FXML private Label detailsLabel;
    @FXML private StackPane gameOverRoot;

    @FXML
    private void initialize() {
        // Entrance animations fire as soon as the scene loads
        animateEntrance();
    }

    public void setWinnerDetails(String details) {
        if (detailsLabel != null) detailsLabel.setText(details);
    }

    // ── ANIMATION: trophy bounce + title fade + details slide in ──
    private void animateEntrance() {

        // 1. Trophy bounce
        if (statusIconLabel != null) {
            statusIconLabel.setScaleX(0);
            statusIconLabel.setScaleY(0);
            ScaleTransition trophyPop = new ScaleTransition(Duration.millis(500), statusIconLabel);
            trophyPop.setFromX(0); trophyPop.setFromY(0);
            trophyPop.setToX(1.2); trophyPop.setToY(1.2);
            trophyPop.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition trophySettle = new ScaleTransition(Duration.millis(200), statusIconLabel);
            trophySettle.setFromX(1.2); trophySettle.setFromY(1.2);
            trophySettle.setToX(1.0);  trophySettle.setToY(1.0);

            new SequentialTransition(trophyPop, trophySettle).play();

            // Continuous gentle pulse on trophy
            ScaleTransition pulse = new ScaleTransition(Duration.millis(900), statusIconLabel);
            pulse.setFromX(1.0); pulse.setFromY(1.0);
            pulse.setToX(1.1);  pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setDelay(Duration.millis(700));
            pulse.play();
        }

        // 2. Title fade in
        if (headerTitleLabel != null) {
            headerTitleLabel.setOpacity(0);
            FadeTransition titleFade = new FadeTransition(Duration.millis(600), headerTitleLabel);
            titleFade.setFromValue(0); titleFade.setToValue(1);
            titleFade.setDelay(Duration.millis(400));
            titleFade.play();
        }

        // 3. Details slide up from below
        if (detailsLabel != null) {
            detailsLabel.setOpacity(0);
            detailsLabel.setTranslateY(30);

            FadeTransition detailFade = new FadeTransition(Duration.millis(500), detailsLabel);
            detailFade.setFromValue(0); detailFade.setToValue(1);

            TranslateTransition detailSlide = new TranslateTransition(Duration.millis(500), detailsLabel);
            detailSlide.setFromY(30); detailSlide.setToY(0);

            ParallelTransition detailAnim = new ParallelTransition(detailFade, detailSlide);
            detailAnim.setDelay(Duration.millis(700));
            detailAnim.play();
        }
    }

    @FXML
    private void handleReturnToStart() {
        try {
            // Fade out before switching scene
            if (gameOverRoot != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), gameOverRoot);
                fadeOut.setFromValue(1); fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> loadStartScreen());
                fadeOut.play();
            } else {
                loadStartScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStartScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/StartView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (gameOverRoot != null
                ? gameOverRoot.getScene().getWindow()
                : headerTitleLabel.getScene().getWindow());

            Scene scene = new Scene(root);
            root.setOpacity(0);
            stage.setScene(scene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
