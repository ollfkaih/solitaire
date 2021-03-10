package solitaire.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SolitaireApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
    	Parent parent = FXMLLoader.load(getClass().getResource("Solitaire.fxml"));
    	stage.setTitle("Solitaire"); //fallback
        stage.setScene(new Scene(parent));
        stage.show();
        //TODO: Change if stacks reposition code is added
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(SolitaireApp.class, args);
    }
}
