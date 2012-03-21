package log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class MyLogger {
	private Logger logger;

	public MyLogger(String className, String logPath)
			throws SecurityException, IOException {
		logger = Logger.getLogger(className);
		FileHandler fh = new FileHandler(logPath);
		logger.addHandler(fh);
	}
	
	public void warning(String msg){
		logger.warning(msg);
	}

}
