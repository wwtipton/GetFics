package com.notcomingsoon.getfics.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.QuirksMode;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.LeafNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.jsoup.select.Selector;

import com.notcomingsoon.getfics.GFConstants;

public abstract class EpubFiles {
	
	final static String BAD_CHARS = "ç~*.\"\'/\\[]$@?():;!|=,\r\n";
	
	static String urlFileName(String name) throws UnsupportedEncodingException {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			String c = "" + name.charAt(i);
			if (BAD_CHARS.contains(c)) {
				continue;
			}
			sb.append(c);
		}
		
		String urlName = URLEncoder.encode(sb.toString(), GFConstants.UTF_8_CHARSET);
		return urlName;
	}
	
	static public Document setOutputType(Document doc) {
		return setOutputType(doc, false);
	}
	
	static public Document setOutputType(Document doc, boolean isXml) {
		Document.OutputSettings docOS = doc.outputSettings();
		docOS.charset(GFConstants.UTF_8_CHARSET);
		docOS.escapeMode(Entities.EscapeMode.xhtml);
		docOS.syntax(Document.OutputSettings.Syntax.xml);

		doc.quirksMode(QuirksMode.noQuirks);

		LeafNode dt = null;
		dt = doc.documentType();
		if (null != dt) {
			dt.remove();
		}

		if (isXml) {
			dt = makeXmlDeclaration();
		} else {
			dt = makeDocType();
		}
		doc.prependChild(dt);

		if (isXml) {
		} else {
			Element html = Selector.select("html", doc.root()).first();
			html.attr("xmlns", Parser.NamespaceHtml);
		}
		
		return doc;
	}
	
	static XmlDeclaration makeXmlDeclaration() {
		XmlDeclaration dt = new XmlDeclaration("xml", false);
		dt.attr(GFConstants.VERSION_ATTR, "1.0");
		dt.attr(GFConstants.ENCODING_ATTR, GFConstants.UTF_8_NAME);
		
		return dt;
	}
	
	static DocumentType makeDocType() {
		DocumentType dt = new DocumentType("HTML", "", "");
		
		return dt;
	}
	
	static OutputStreamWriter getOSW(File dir, String filename)
			throws IOException, FileNotFoundException, UnsupportedEncodingException {
		File f = new File(dir, filename);
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, GFConstants.UTF_8_NAME);
		return osw;
	}
	
	public static File createDirectory(String child, String parentDirectory) {
		String childDirectory = parentDirectory + File.separator + child;
		File dir = new File(childDirectory);
		dir.mkdir();
		
		return dir;
	}
	

}
