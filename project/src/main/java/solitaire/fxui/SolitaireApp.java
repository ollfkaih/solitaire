package solitaire.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import solitaire.model.SolConst;

public class SolitaireApp extends Application {
	
	FXMLLoader loader;
	SolitaireController controller;
	
    @Override
    public void start(Stage stage) throws Exception {
    	loader = new FXMLLoader((getClass().getResource("Solitaire.fxml")));
    	loader.load();
    	controller = (SolitaireController) loader.getController();
    	stage.setTitle("Solitaire"); 
        stage.setScene(new Scene(loader.getRoot()));
        try {
        stage.getIcons().add(new Image(SolitaireController.class.getResourceAsStream("img/icon.png")));
        } catch (Exception e) {}
        stage.show();	
        stage.setOnCloseRequest(event -> {
			try {
				appExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
        stage.setMinHeight(480 + SolConst.TOPDELTAY + SolConst.BOTTOMDELTAY);
        stage.setMinWidth(600);
    }

    public static void main(String[] args) {
        launch(SolitaireApp.class, args);
    }

    public void appExit() throws Exception {
    	controller.promptSave();
        super.stop();
    }
}
