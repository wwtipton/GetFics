package com.notcomingsoon.getfics;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;

/**
 * 
 */

/**
 * @author Winifred Tipton
 *
 */
public class Story {
	
	String fileAuthor;
	
	String fileTitle;
	
	String origAuthor;
	
	String origTitle;
	
	File outputDir;
	
	private static final String OUTPUT_DIRECTORY = GFProperties.getPropertyValue(GFProperties.OUTPUT_DIRECTORY_KEY);

	private static Logger logger = GFLogger.getLogger();
	
	private boolean isOneShot = false;

	private Charset charset;
	

	public String getFileAuthor() {
		return fileAuthor;
	}

	public boolean isOneShot() {
		return isOneShot;
	}

	public void setOneShot(boolean isOneShot) {
		this.isOneShot = isOneShot;
	}

	public void setFileAuthor(String author) throws UnsupportedEncodingException {
		this.fileAuthor = GFFileUtils.stripInvalidChars(author);
	}

	public String getFileTitle() {
		return fileTitle;
	}

	public void setFileTitle(String title) throws UnsupportedEncodingException {
		this.fileTitle = GFFileUtils.stripInvalidChars(title);
	}

	public String getOrigAuthor() {
		return origAuthor;
	}

	public void setOrigAuthor(String origAuthor) {
		this.origAuthor = origAuthor;
	}

	public String getOrigTitle() {
		return origTitle;
	}

	public void setOrigTitle(String origTitle) {
		this.origTitle = origTitle;
	}

	public File getOutputDir() {
		return outputDir;
	}

	void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	/**
	 * @param author
	 * @param title
	 * @param outputDir
	 * @throws UnsupportedEncodingException 
	 */
	public Story(String author, String title) throws UnsupportedEncodingException {
		super();
		logger.entering("com.notcomingsoon.getfics.Story", "Story(String author, String title)");
		setFileAuthor(author);
		setFileTitle(title);
		setOrigAuthor(author);
		setOrigTitle(title);


		File dir = GFFileUtils.createDirectory(this.getFileAuthor(), this.getFileTitle(), OUTPUT_DIRECTORY);
		setOutputDir(dir);
		
		logger.exiting("com.notcomingsoon.getfics.Story", "Story(String author, String title)");
	}

	@Override
	public String toString()
	{
		return fileAuthor + "-" + fileTitle;
	}


	public static Story createStory(String author, String title) throws Exception {
		logger.entering("com.notcomingsoon.getfics.Story", "createStory(String author, String title)");

		Story s = new Story(author, title);		
		
		logger.exiting("com.notcomingsoon.getfics.Story", "createStory(String author, String title)");
		return s;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}

	
}
