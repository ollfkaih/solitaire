package solitaire.fxui;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import solitaire.logging.DistributingLogger;
import solitaire.logging.LabelLogger;
import solitaire.logging.StreamLogger;

public class StatusBarController {
	@FXML HBox BottomBar;
	
	public final static String DEALERROR = "Could not deal cards", NEWGAME = "New game started",
			SAVEERROR = "Could not save game", LOADERROR = "Could not load game", 
			SAVESUCCESS = "Game saved", LOADSUCCESS = "Game loaded", 
			SAVEFILECORRUPT = LOADERROR + " because the save file is corrupt",
			LOADGRAPHICSERROR = "Some images were not loaded, game will not play correctly",
			GAMEWON = "You won! Press F2 or 'Game' -> 'New game' to play again" ;

	
	private DistributingLogger logger;
	private StreamLogger warningLogger;
	private LabelLogger labelLogger;

	public void initialize() {
		warningLogger = new StreamLogger(System.out);
		labelLogger = new LabelLogger(BottomBar);
		
		logger = new DistributingLogger(labelLogger, warningLogger, labelLogger, warningLogger);
		//logger.log(ILogger.INFO, "New game started", null);
	}
	
	public void log(String severity, String message, Exception exception) {
		this.clearStatusBar();
		logger.log(severity, message, exception);
	}

	public void clearStatusBar() {
		BottomBar.getChildren().clear();
	}

}
