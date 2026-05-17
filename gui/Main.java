package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

	@Override
	public void start(Stage stage) throws Exception {
		  FXMLLoader loader = new FXMLLoader(getClass().getResource("StartView.fxml"));
	        Scene scene = new Scene(loader.load(), 600, 400);

	        stage.setTitle("DoorDasH");
	        stage.setScene(scene);
	        stage.show();
	}
public static void main(String[] args){
	launch();
}

}
