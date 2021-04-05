package solitaire.logging;

import java.io.OutputStream;
import java.io.PrintStream;

public class StreamLogger implements ILogger{
	private PrintStream stream;
	private final static String DEFAULTFORMATSTRING = "Severity: %s, message: %s";
	private String formatString = DEFAULTFORMATSTRING;

	public StreamLogger(OutputStream stream) {
		super();
		this.stream = new PrintStream(stream);
	}

	@Override
	public void log(String severity, String message, Exception exception) {
		if (exception != null) {
			setFormatString(DEFAULTFORMATSTRING + " (%s)");
		}
		stream.format(formatString, severity, message, exception);
		setFormatString(DEFAULTFORMATSTRING);
		stream.println();
		stream.flush();
	}
	
	void setFormatString(String formatString) {
		this.formatString = formatString;
	}
}
