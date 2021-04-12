package solitaire.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
    	stage.setTitle("Solitaire"); //fallback
        stage.setScene(new Scene(loader.getRoot()));
        stage.show();	
        stage.setOnCloseRequest(event -> {
			try {
				//TODO: Uncomment
				//appExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
        stage.setMinHeight(480 + SolConst.TOPDELTAY + 30 - 10); //TODO: Review (Root height + menubar, bottom bar and empty height)
        stage.setMinWidth(600);
        controller.setStage(stage);
    }

    public static void main(String[] args) {
        launch(SolitaireApp.class, args);
    }

    public void appExit() throws Exception {
    	controller.promptSave();
        super.stop();
    }
}
