package solitaire.fxui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import solitaire.logging.DistributingLogger;
import solitaire.logging.ILogger;
import solitaire.logging.LabelLogger;
import solitaire.logging.StreamLogger;

/**
 * A special controller that uses a DistributingLogger to log events 
 * to a label in the game's status bar or to file, depending on event severity
 */
public class StatusBarController implements ILogger {
	@FXML private HBox BottomBar;
	
	public final static String DEALERROR = "Could not deal cards", NEWGAME = "New game started",
			SAVEERROR = "Could not save game", LOADERROR = "Could not load game", 
			SAVESUCCESS = "Game saved", LOADSUCCESS = "Game loaded", 
			SAVEFILECORRUPT = LOADERROR + " because the save file is corrupt",
			LOADGRAPHICSERROR = "Some images were not loaded, game will not play correctly",
			LAZYSOLVEERROR = "No cards can be legally moved to finalstacks",
			GAMEWON = "You won! Press F2 or 'Game' -> 'New game' to play again" ;
	private DistributingLogger logger;

	/**
	 * Initializes 
	 */
	public void initialize() {
		File logFile = new File(IOHandler.getSaveFolderPath().toString(), "SolitaireLog.txt");
		ILogger warningLogger = createLogFile(logFile);
		ILogger labelLogger = new LabelLogger(BottomBar);
		
		logger = new DistributingLogger(labelLogger, warningLogger, labelLogger, warningLogger);
	}

	private ILogger createLogFile(File logFile) {
		OutputStream outputStream;
		ILogger logFileLogger = null;
		if (logFile.exists())
			logFile.delete();
		try {
			if (logFile.createNewFile()) {
				outputStream = new FileOutputStream(logFile, true);	
				logFileLogger = new StreamLogger(outputStream);	
			}
		} catch (Exception e) {
		}
		return logFileLogger;
	}
	
	public void log(String severity, String message, Exception exception) {
		this.clearStatusBar();
		logger.log(severity, message, exception);
	}

	public void clearStatusBar() {
		BottomBar.getChildren().clear();
	}

}
