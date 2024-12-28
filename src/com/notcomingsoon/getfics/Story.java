package com.notcomingsoon.getfics;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;



/**
 * @author Winifred Tipton
 *
 */
public class Story {
	
	String fileAuthor;
	
	String fileTitle;
	
	String origAuthor;
	
	String origTitle;
	
	String delimitedAuthor;
	
	/* Only populate if previous version of story exists. */
	Calendar now;
	
	File outputDir;
	
	private static final String OUTPUT_DIRECTORY = GFProperties.getPropertyValue(GFProperties.OUTPUT_DIRECTORY_KEY);

	private static Logger logger = GFLogger.getLogger();
	
	private boolean isOneShot = false;

	private Charset charset;

	private static final String CONTENTS = "contents";
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMddHHmmss");
	
	private ArrayList<String> imageFailures = new ArrayList<String>();
	

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
		setDelimitedAuthor(author);


		File dir = GFFileUtils.createDirectory(this.getFileAuthor(), this.getFileTitle(), OUTPUT_DIRECTORY);
		setOutputDir(dir);
		/*
		File f = new File(dir, toString() + HTMLConstants.HTML_EXTENSION);
		if (f.exists()) {
			setNow(Calendar.getInstance());
		}
		*/

		
		logger.exiting("com.notcomingsoon.getfics.Story", "Story(String author, String title)");
	}

	void setDelimitedAuthor(String author) {
		delimitedAuthor = author.replace(',', ';');
	}
	
	public String getDelimitedAuthor() {
		return delimitedAuthor;
	}

	@Override
	public String toString()
	{
		return fileAuthor + "-" + fileTitle + getTimestamp();
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

	public Calendar getNow() {
		return now;
	}
	
	/** May be empty string. */
	public String getTimestamp() {
		String ts = "";
		if (null != now) {
			ts = sdf.format(now.getTime());
		}
		return ts;
	}

	/** Only populate if previous version of story exists. */
	private void setNow(Calendar now) {
		this.now = now;
	}

	public String getContentsFileName() {
		return CONTENTS + getTimestamp() + HTMLConstants.HTML_EXTENSION;
	}
	
	public void addImageFailure(String failure) {
		imageFailures.add(failure);
	}

	public ArrayList<String> getImageFailures() {
		// TODO Auto-generated method stub
		return imageFailures;
	}
}
