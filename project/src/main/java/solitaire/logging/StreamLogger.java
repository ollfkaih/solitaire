package solitaire.logging;

import java.io.OutputStream;
import java.io.PrintStream;

public class StreamLogger implements ILogger{
	private PrintStream stream;
	private String formatString = "%s: %s (%s)";

	public StreamLogger(OutputStream stream) {
		super();
		this.stream = new PrintStream(stream);
	}

	@Override
	public void log(String severity, String message, Exception exception) {
		stream.format(formatString, severity, message, exception);
		stream.println();
		stream.flush();
	}
	
	void setFormatString(String formatString) {
		this.formatString = formatString;
	}
}
