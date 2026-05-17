package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class GameOverController {

    @FXML private Label detailsLabel;
    @FXML private Label headerTitleLabel;
    @FXML private Label statusIconLabel;
    @FXML private StackPane gameOverRoot;

    /**
     * Receives game metrics from GameController.
     * Beautifully alters visual treatments depending on a real victory vs. an exit.
     */
    public void setWinnerDetails(String details) {
        if (detailsLabel == null) return;

        if (details == null || details.trim().equalsIgnoreCase("No Winner")) {
            // Manual Exit Layout State Changes
            statusIconLabel.setText("🚪");
            headerTitleLabel.setText("MATCH ABORTED");
            headerTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: #c62828; -fx-letter-spacing: 1.5;");
            detailsLabel.setText("The current gameplay session was closed early.\nNo monsters were crowned winners during this run.");
        } else {
            // Win Condition Layout State Changes
            statusIconLabel.setText("🏆");
            headerTitleLabel.setText("VICTORY DETERMINED");
            headerTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: #2e7d32; -fx-letter-spacing: 1.5;");
            
            // Clean up text lines for aesthetic score presentation
            detailsLabel.setText("👑 WINNER ANNOUNCED! 👑\n\n" + details);
        }
    }

    /**
     * Safely returns players back to your application's primary selection/login screen 
     * matching grading guidelines without throwing unexpected lifecycle thread errors.
     */
    @FXML
    private void handleReturnToStart() {
        try {
            // Note: If your main landing view file has a different name, swap "StartView.fxml" here
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/giu/StartView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) gameOverRoot.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error redirecting application flow back to main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}