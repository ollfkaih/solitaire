package solitaire.logging;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LabelLogger implements ILogger {
	private HBox statusBar;
	
	public LabelLogger(HBox statusBar) {
		if (statusBar == null)
			throw new IllegalArgumentException("LabelLogger constructor requires a non-null HBox bottombar");
		this.statusBar = statusBar;
	}
	
	//TODO: write log with arg (LogConstant Event?) (maybe also logwithbutton?)	
	@Override
	public void log(String severity, String message, Exception exception) {
		Label label = new Label();
		label.setText(message);
		/*if (severity == ERROR) 
			label.setTranslateX(200);*/
		
		//Add label if not exist
		statusBar.getChildren().add(label);
		
		//TODO: Start timer, make a TimerTask with a runnable to log "null/nothing" to the label again (or better, maybe clear the text of the label) ? 
		//timer.schedule
	}
}
