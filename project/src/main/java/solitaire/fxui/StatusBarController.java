package solitaire.fxui;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import solitaire.logging.DistributingLogger;
import solitaire.logging.ILogger;
import solitaire.logging.LabelLogger;
import solitaire.logging.StreamLogger;

public class StatusBarController {
	@FXML HBox BottomBar;
	
	public final static String DEALERROR = "Could not deal cards", NEWGAME = "New game started", SAVEERROR = "Could not save game", LOADERROR = "Could not load game", GAMEWON = "You won! Press F2 or 'Game' -> 'New game' to play again" ;
	
	private DistributingLogger logger;
	private StreamLogger warningLogger;
	private LabelLogger labelLogger;

	public void initialize() {
		warningLogger = new StreamLogger(System.out);
		labelLogger = new LabelLogger(BottomBar);
		logger = new DistributingLogger(labelLogger, warningLogger, labelLogger, warningLogger);
		logger.log(ILogger.INFO, "New game started", null);
	}
	
	public void log(String severity, String message, Exception exception) {
		logger.log(severity, message, exception);
	}

	public void clearStatusBar() {
		BottomBar.getChildren().clear();
	}

}
