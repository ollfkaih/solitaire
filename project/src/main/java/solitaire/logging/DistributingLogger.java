package solitaire.logging;

import java.util.HashMap;
import java.util.Map;

public class DistributingLogger implements ILogger {
	private Map<String, ILogger> loggerMap = new HashMap<String, ILogger>();
	
	public DistributingLogger(ILogger errorLogger, ILogger warningLogger, ILogger infoLogger) {
		loggerMap.put(ERROR, errorLogger);
		loggerMap.put(WARNING, warningLogger);
		loggerMap.put(INFO, infoLogger);
	}
	
	void setLogger(String severity, ILogger logger) {
		loggerMap.put(severity, logger);
	}
	
	@Override
	public void log(String severity, String message, Exception exception) {
		ILogger logger = loggerMap.get(severity);
		if (logger != null)
			logger.log(severity, message, exception);
	}

}
