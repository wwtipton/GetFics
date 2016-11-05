/**
 * 
 */
package com.notcomingsoon.getfics;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Winifred Tipton
 *
 */
public class GFFileUtils {

	private static Logger logger = GFLogger.getLogger();

	private static final char[] INVALID_CHARS = new char[]{'\\', '/', '?', ':', '*', '"', '>', '<', '|', '#'};
	
	private static final char PERIOD = '.'; 

	private static final char LAST_ASCII = 126;
	
	
	public static File createDirectory(String author, String title, String outputDirectory) {
		logger.entering("com.notcomingsoon.getfics.GFFileUtils", "createDirectory(String author, String title, String outputDirectory)");
		String authorDirectory = outputDirectory + File.separator + author;
		File dir = new File(authorDirectory);
		dir.mkdir();
		
		String storyDirectory =  authorDirectory + File.separator + title;
		dir = new File(storyDirectory);
		dir.mkdir();
		logger.exiting("com.notcomingsoon.getfics.GFFileUtils", "createDirectory(String author, String title, String outputDirectory)");
		return dir;
	}
	

	public static String stripInvalidChars(String in) {
		logger.entering("com.notcomingsoon.getfics.GFFileUtils", "stripInvalidChars(String in)");
		logger.info("String in = " + in);
		String s = in;
		
		StringBuilder sbS = new StringBuilder(s);
		StringBuilder sbOut = new StringBuilder();
		

		for (int j = 0; j < sbS.length(); j++){
			char c = sbS.charAt(j);
			if (c == PERIOD && j > 0){
				char prev = sbS.charAt(j - 1);
				if (!Character.isDigit(prev)){//TODO What is this for? Skip . unless in middle of number?
					continue;
				}
			}
			if (c <= LAST_ASCII && isValidChar(c) ){
				sbOut.append(c);
			}
		}
		
		s = sbOut.toString().trim();
		
		logger.exiting("com.notcomingsoon.getfics.GFFileUtils", "stripInvalidChars(String in)");
		return s;
	}


	private static boolean isValidChar(char c) {
		boolean isValid = true;
		
		for (int i = 0; i < INVALID_CHARS.length; i++){
			if (c == INVALID_CHARS[i]){
				isValid = false;
				break;
			}
		}
		return isValid;
	}

}
