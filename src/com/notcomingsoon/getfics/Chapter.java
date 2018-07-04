/**
 * 
 */
package com.notcomingsoon.getfics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 * @author Winifred Tipton
 *
 */
public class Chapter {
	@Override
	public String toString() {
		return origTitle;
	}


	String url;
	String fileTitle;
	String origTitle;
	
	private static Logger logger = GFLogger.getLogger();
	
	public static final String CONTENTS = "contents" + HTMLConstants.HTML_EXTENSION;	
	
	public static final String CHAPTER = "Chapter";
	public static final String TOC = "Table of Contents";
	private static final int ONE_SHOT = 1;
	
	/**
	 * @param url
	 * @param title
	 */
	public Chapter(String url, String title) {
		super();
		setUrl(url);
		setOrigTitle(title);
		setFileTitle(title);
	}
	
	public String getOrigTitle() {
		return origTitle;
	}

	public void setOrigTitle(String origTitle) {
		this.origTitle = origTitle;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFileTitle() {
		return fileTitle;
	}
	public void setFileTitle(String title) {
		this.fileTitle = GFFileUtils.stripInvalidChars(title);
	}


	/**
	 * If multi chapter story need a contents file.
	 * 
	 * @param story
	 * @param chapterList
	 * @param charset
	 * @throws IOException
	 */
	public static void writeContents(Story story, ArrayList<Chapter> chapterList, Charset charset) throws IOException {
		logger.entering("com.notcomingsoon.getfics.Chapter", "writeContents(File dir, ArrayList<Chapter> chapterList)");
		
		if (chapterList.size() > ONE_SHOT){
			File dir = story.getOutputDir();
			File f = new File(dir, CONTENTS);
	
			logger.info("f: " + f.toString());	
			
			FileOutputStream fos = new FileOutputStream(f);
			
			Document outDoc = new Document(story.getFileTitle());
			outDoc.outputSettings().charset(charset);
			Element html = outDoc.appendElement(HTMLConstants.HTML_TAG);
			Element body = html.appendElement(HTMLConstants.BODY_TAG);	
			Element nav = body.appendElement(HTMLConstants.NAV_TAG);
			nav.attr("epub:type", "toc");
			Element h2 = new Element(Tag.valueOf(HTMLConstants.H2_TAG), dir.getName());
			h2.text(TOC);
			nav.appendChild(h2);
			Element ol = new Element(Tag.valueOf(HTMLConstants.OL_TAG), dir.getName());
			nav.appendChild(ol);
		
			for (int i = 0; i < chapterList.size(); i++){
				Chapter c = chapterList.get(i);
				Element li = new Element(Tag.valueOf(HTMLConstants.LI_TAG), dir.getName());
				
				Element a = new Element(Tag.valueOf(HTMLConstants.A_TAG), dir.getName());
				String href = story.toString() + HTMLConstants.HTML_EXTENSION + HTMLConstants.TARGET + c.fileTitle;
				a.attr(HTMLConstants.HREF_ATTR, href);
				a.text(c.origTitle);
				li.appendChild(a);			
				
				ol.appendChild(li);
			}

			byte[] b = outDoc.html().getBytes();
			
			fos.write(b);
			fos.close();
		}
		logger.exiting("com.notcomingsoon.getfics.Chapter", "writeContents(File dir, ArrayList<Chapter> chapterList)");
	}

}
