package gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class RulesController {

    @FXML
    private void closeWindow(ActionEvent e) {
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.close();
    }
}