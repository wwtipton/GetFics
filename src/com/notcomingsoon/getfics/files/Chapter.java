package com.notcomingsoon.getfics.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jsoup.nodes.Document;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.GFProperties;


public class Chapter 
 extends EpubFiles
 implements GFConstants
{
	private String name;
	private Document doc;
	String url;
	Boolean doesChapterExist = false;
	
	protected static final String SUMMARY_STRING = GFProperties.getString(GFProperties.SUMMARY); // $NON-NLS-1$

	
	public Boolean doesChapterExist() {
		return doesChapterExist;
	}


	public void setDoesChapterExist(Boolean doesChapterExist) {
		this.doesChapterExist = doesChapterExist;
	}

	private static String FILETYPE = ".xhtml";
	private static String LONE_AMPERSAND = " & ";
	private static String AND_LITERAL = " and ";
	private static String AMPERSAND_ENTITY = " &amp; ";
	
	static public boolean isChapterFile(File f) {
		boolean isChapter = false;
		
		String name = f.getName();
		if (null != name) {
			int period = name.lastIndexOf('.');
			if (period > 0) {
				String type = name.substring(period);
				if (FILETYPE.equalsIgnoreCase(type)) {
					isChapter = true;
				}
			}
		}
		
		return isChapter;
	}
	
	static public boolean isSummaryFile(File f) {
		boolean isSummary = false;
		
		String name = f.getName();
		if (null != name) {
			if (name.startsWith(SUMMARY_STRING)) {
				isSummary = true;
			}
		}
		
		return isSummary;
	}
	

	public Chapter(String url, String name) throws UnsupportedEncodingException {
		super();
		setUrl(url);
		setName(name);
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Chapter(String name, Document doc) throws UnsupportedEncodingException {
		setName(name);
		setDoc(doc);
	}
	
	public Chapter(String name) throws UnsupportedEncodingException {
		setName(name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getFilename() throws UnsupportedEncodingException {
		String filename = name;
		
		if (filename.contains(LONE_AMPERSAND)) {
			filename.replace(LONE_AMPERSAND, AND_LITERAL);
		}
		if (filename.contains(AMPERSAND_ENTITY)) {
			filename.replace(AMPERSAND_ENTITY, AND_LITERAL);
		}
		return urlFileName(name)+FILETYPE;
	}
	
	public void setName(String name) throws UnsupportedEncodingException {
		this.name = name;
	}
	
	public Document getDoc() {
		return doc;
	}
	
	public void setDoc(Document doc) {
		this.doc = doc;

	}
	
	void writeChapter(File dir) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		if (doesChapterExist) {
			return;
		}
		
		OutputStreamWriter osw = getOSW(dir, getFilename());
		String content = getDoc().html();
		osw.write(content);

		osw.close();
	}

	public boolean isTOC() {
		return (this instanceof Contents);
	}

}
