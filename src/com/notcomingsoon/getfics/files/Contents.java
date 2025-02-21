package com.notcomingsoon.getfics.files;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import com.notcomingsoon.getfics.GFConstants;

public class Contents 
extends Chapter
implements GFConstants
{
	private static final String FILENAME = "contents";

	private static final String NAV_TAG = "nav";
	
	public static final String TOC = "Table of Contents";
	
	public static String OL_TAG = "ol";
	public static String LI_TAG = "li";
	
	ArrayList<Chapter> chapters;
	
	Document contents;
	
	Pattern p = Pattern.compile("[0-9]");
	
	boolean orderedList = true;

	static public boolean isTOCFile(File f) {
		boolean isTOC = false;
		
		String name = f.getName();
		if (null != name) {
			if (name.startsWith(FILENAME)) {
				isTOC = true;
			}
		}
		
		return isTOC;
	}
	

	public Contents(ArrayList<Chapter> chapters) throws IOException {
		super(FILENAME);
		setChapters(chapters);
		
		makeContents();
	}

	public ArrayList<Chapter> getChapters() {
		return chapters;
	}

	public void setChapters(ArrayList<Chapter> chapters) {
		this.chapters = chapters;
	}

	public Document getContents() {
		return contents;
	}

	public void setContents(Document contents) {
		this.contents = contents;
	}

	void writeChapter(File dir) throws IOException {
		OutputStreamWriter osw = getOSW(dir, getFilename());
		String content = contents.html();
		osw.write(content);

		osw.close();
	}

	void makeContents() throws UnsupportedEncodingException {
		contents = new Document(null);
		
		Element html = contents.appendElement(GFConstants.HTML_TAG);
		html.attr("xmlns", Parser.NamespaceHtml);
		html.attr("xmlns:epub", "http://www.idpf.org/2007/ops");
		
		Element head = html.appendElement(GFConstants.HEAD_TAG);
		Element titleTag = new Element("title");
		titleTag.text(TOC);
		head.appendChild(titleTag);

		Element body = html.appendElement(GFConstants.BODY_TAG);	
		Element nav = body.appendElement(NAV_TAG);
		nav.attr("epub:type", "toc");
		
		Element h2 = new Element(GFConstants.H2_TAG);
		h2.text(TOC);
		nav.appendChild(h2);
		
		Element ol = new Element(OL_TAG);
		nav.appendChild(ol);
	
		for (Chapter c : chapters) {
			Element li = new Element(LI_TAG);
			Element a = new Element(GFConstants.A_TAG);
			String href = c.getFilename();
			a.attr(GFConstants.HREF_ATTR, href);
			a.text(c.getName());
			li.appendChild(a);			
			ol.appendChild(li);
		}

		setOutputType(contents);
	}

}
