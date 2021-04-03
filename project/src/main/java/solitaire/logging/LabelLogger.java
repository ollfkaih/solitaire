package solitaire.logging;

import javafx.scene.Node;
import javafx.scene.control.Button;
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
		Label label = null;
		if (statusBar.getChildren().size() == 0) {
			label = new Label();
			statusBar.getChildren().add(label);
		} else {
			Node n = statusBar.getChildren().get(0);
			if (n.getClass().equals(Label.class))
				label = (Label) n;
		}
		label.setText(message);
		label.setTranslateX(10);
		label.setTranslateY(5);
		/*if (severity == ERROR) 
			label.setTranslateX(200);*/
		//Button button = new Button("Click me");

		//Add label if not exist
		//statusBar.getChildren().add(button);
		
		//TODO: Start timer, make a TimerTask with a runnable to log "null/nothing" to the label again (or better, maybe clear the text of the label) ? 
		//timer.schedule
	}

	public void clearStatusBar() {
		statusBar.getChildren().clear();
	}
}
