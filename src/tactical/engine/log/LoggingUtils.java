package tactical.engine.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtils {
	public static Logger createLogger(Class<?> c) {
		Logger logger = Logger.getLogger(c.getName());
		logger.setLevel(Level.FINE);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.FINE);
		logger.addHandler(consoleHandler);
		return logger;
	}
}
