package com.notcomingsoon.getfics.files;

import java.io.File;
import java.io.FileFilter;
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
implements GFConstants,
FileFilter
{
	private static final String FILENAME = "contents";

	private static final String NAV_TAG = "nav";
	
	public static final String TOC = "Table of Contents";
	
	public static String OL_TAG = "ol";
	public static String LI_TAG = "li";
	
	ArrayList<Chapter> chapters;
	
	Document contents;
	
	String ficPath;
	
	Pattern p = Pattern.compile("[0-9]");
	
	public String getFicPath() {
		return ficPath;
	}

	private void setFicPath(String ficPath) {
		this.ficPath = ficPath;
	}


	boolean orderedList = true;

	public Contents(String ficPath2, ArrayList<Chapter> chapters) throws IOException {
		super(FILENAME);
		setFicPath(ficPath2);
		setChapters(chapters);
		
		makeContents();
		//loadContents();
	//	updateContents();
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

	/*
	void updateContents() throws IOException{
		Element html = Selector.select("html", contents.root()).first();
		html.attr("xmlns", Parser.NamespaceHtml);
		html.attr("xmlns:epub", "http://www.idpf.org/2007/ops");
		
		Element head = Selector.select("head", contents.root()).first();
		Element titleTag = new Element("title");
		titleTag.text("Table of Contents");
		head.appendChild(titleTag);
		
		Elements aRefs = Selector.select("a[href]", contents.root());
		
		String summary = EFProperties.getPropertyValue(EFProperties.SUMMARY_KEY);
		
		for(Element aRef:aRefs) {
			String href= aRef.attr("href");
			int poundIdx = href.indexOf('#');
			String title = href.substring(poundIdx +1);
			title = title + ".xhtml";
			href = urlFileName(title);
			aRef.attr("href", href);
			
			Matcher m = p.matcher(title.substring(0,1));
		}
				
	}
	*/
	
	void writeChapter(File dir) throws IOException {
		OutputStreamWriter osw = getOSW(dir, getFilename());
		String content = contents.html();
		osw.write(content);

		osw.close();
	}

	
	/*
	void loadContents() throws IOException {
		contents = null;

		File parent = new File(ficPath).getParentFile();
		File[] contentsList = parent.listFiles(this);
		
		// No contents.html
		if (null == contentsList || contentsList.length == 0) {
			return;
		}
		
		File c = null;
		for(File f : contentsList) {
			if (null == c) {
				c = f;
			}
			
			if (f.lastModified() > c.lastModified()) {
				c = f;
			}
		}
		
		Parser parser = Parser.htmlParser();
		
		Charset cs = Charset.defaultCharset();
		contents = Jsoup.parse(c, cs.displayName(), "", parser);

		Contents.setOutputType(contents);
	}
	*/
	
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
	

	public boolean accept(File pathname) {
		boolean acceptF = false;
		
		String name = pathname.getName();
		if (null != name && name.contains("contents")) {
			acceptF = true;
		}
		
		return acceptF;
	}
}
