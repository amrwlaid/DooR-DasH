package gui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Controlle_The_Background {
    @FXML
    private void openRules() throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("RulesView.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Game Rules");
        stage.setScene(new Scene(root));

        stage.show();
    }
    
    @FXML
    private void lets_play_the_game(ActionEvent e) throws IOException{
    	Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.close();
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("ChoosingView.fxml"));
        Parent root = loader.load();

        Stage s = new Stage();
        s.setTitle("Choose your denisty");
        s.setScene(new Scene(root));

        s.show();
       	
    }
    
}
