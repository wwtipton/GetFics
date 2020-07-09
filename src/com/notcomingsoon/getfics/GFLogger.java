package com.notcomingsoon.getfics;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class GFLogger extends java.util.logging.Logger {

	private static Logger logger = null;
	
	protected GFLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}
	
	public static Logger getLogger(){
		if (logger == null){
			logger = (Logger) getLogger("GetFics");
			
			String logDir = GFProperties.getPropertyValue(GFProperties.OUTPUT_DIRECTORY_KEY) ;
			String logFile 
				= logDir
				+ File.separator 
				+ GFProperties.getPropertyValue(GFProperties.LOG_FILE_KEY);
			FileHandler fh;
			try {
				File f = new File(logDir);
				f.mkdir();
				
				f = new File(logFile);
				f.createNewFile();
				
				fh = new FileHandler(logFile);
				fh.setLevel(Level.ALL);
				fh.setFormatter(new SimpleFormatter());
				logger.addHandler(fh);
				logger.setLevel(Level.WARNING);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return logger;
	}
	

}
