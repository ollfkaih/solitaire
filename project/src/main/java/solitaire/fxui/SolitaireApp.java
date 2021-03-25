package solitaire.fxui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SolitaireApp extends Application {
	
	FXMLLoader loader;
	
    @Override
    public void start(Stage stage) throws Exception {
    	loader = new FXMLLoader((getClass().getResource("Solitaire.fxml")));
    	loader.load();
    	stage.setTitle("Solitaire"); //fallback
        stage.setScene(new Scene(loader.getRoot()));
        //TODO: Make resizable if stacks reposition code is added
        //stage.setResizable(false);
        stage.show();	
        stage.setOnCloseRequest(e -> {
			try {
				//TODO: Uncommennt
				//appExit();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
        stage.setMinHeight(480 + 40 - 10); //TODO: Review (Root height + menubar and empty height)
        stage.setMinWidth(600);
    }

    public static void main(String[] args) {
        launch(SolitaireApp.class, args);
    }

    public void appExit() throws Exception {
    	SolitaireController controller = (SolitaireController) loader.getController();
    	controller.promptSave();
        super.stop();
    }
}
