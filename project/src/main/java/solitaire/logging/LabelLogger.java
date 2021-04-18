package solitaire.logging;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class LabelLogger implements ILogger {
	private HBox statusBar;
	
	public LabelLogger(HBox statusBar) {
		if (statusBar == null)
			throw new IllegalArgumentException("LabelLogger constructor requires a non-null HBox bottombar");
		this.statusBar = statusBar;
	}
	
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
		Color textColor;
		switch (severity) {
		case ILogger.ERROR -> textColor = Color.RED;
		case ILogger.INFO -> textColor = Color.BLACK;
		default -> textColor = Color.BLUEVIOLET;
		}
		label.setTextFill(textColor);
	}
}
