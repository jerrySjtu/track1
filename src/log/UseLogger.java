package log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UseLogger {
	// Always use the classname, this way you can refactor
	private final static Logger LOGGER = Logger.getLogger(UseLogger.class.getName());

	public static void main(String[] args) {
		UseLogger logger = new UseLogger();
		try {
			MyLogger.setup();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the log files");
		}
		logger.writeLog();
	}
	
	public static void writeLog() {
		// Set the LogLevel to Severe, only severe Messages will be written
		LOGGER.setLevel(Level.SEVERE);
		LOGGER.severe("Info Log");
		LOGGER.warning("Info Log");
		LOGGER.info("Info Log");
		LOGGER.finest("Really not important");
	}
}