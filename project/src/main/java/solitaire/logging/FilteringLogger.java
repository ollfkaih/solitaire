package solitaire.logging;

import java.util.ArrayList;
import java.util.List;

public class FilteringLogger implements ILogger{
	
	private ILogger severeLogger;
	private List<String> severities; 
	
	public FilteringLogger(ILogger logger, String... severities) {
		this.severeLogger = logger;
		this.severities = new ArrayList<>();
		for (String severity: severities) {
			this.severities.add(severity);
		}
	}
	
	public boolean isLogging(String severity) {
		if (severities.contains(severity))
			return true;
		return false;
	}

	public void setIsLogging(String severity, boolean value) {
		if (severities.contains(severity) && value == false)
			severities.remove(severity);
		else if (!severities.contains(severity) && value == true)
			severities.add(severity);
	}
	
	@Override
	public void log(String severity, String message, Exception exception) {
		if (severities.contains(severity))
			severeLogger.log(severity, message, exception);
	}

}
