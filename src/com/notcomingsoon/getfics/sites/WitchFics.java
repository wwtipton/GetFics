package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.files.Chapter;

/**
 * Note that this is via the Wayback Machine so everything is nested inside WBM stuff.
 * 
 * @author Winifred
 *
 */
public class WitchFics extends Site {
	
	private static final String BY = " by ";

	private static final String SRC = "src";

	private static final String WAYBACK_MACHINE_URL = "https://web.archive.org";

	private static final String PHPSESSID = "PHPSESSID";
			
	String iframeUrl = null;
	
	String introUrl = null;
	
	String navUrl = null;
	
	static{
		try {
			URI U = new URI(WAYBACK_MACHINE_URL);
			addCookie(U,PHPSESSID, "pg6dm0elglserhrbeqtfb7h4l3");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	

	public WitchFics(String ficUrl) throws Exception {
		super(ficUrl);
		getNestedSite();
	}

	private void getNestedSite() throws Exception {
		// This page has wayback machine stuff.
		Document doc = getPage(startUrl);
		Element iframe = doc.getElementById("playback");
		iframeUrl = iframe.attr(SRC);
		
		doc = getPage(iframeUrl);
		Elements frames = doc.getElementsByTag(GFConstants.FRAME_TAG);
		introUrl = WAYBACK_MACHINE_URL + frames.get(0).attr(SRC);
		startUrl = introUrl;
		navUrl = WAYBACK_MACHINE_URL + frames.get(1).attr(SRC);
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Document nav;
		Elements options = null;
		try {
			nav = getPage(navUrl);
			options = getChapterOptions(nav);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (options != null){
			
			String urlPrefix = null;
			int slashIndex = startUrl.lastIndexOf(GFConstants.URL_DIVIDER);
			urlPrefix = startUrl.substring(0, slashIndex+1);
			logger.info("urlPrefix = " + urlPrefix); 
	
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(GFConstants.HREF_ATTR);
				cUrl = urlPrefix + cUrl;
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}
		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
		return list;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements divs = doc.getElementsByTag(GFConstants.DIV_TAG);
		Element div = divs.first();
		options = div.getElementsByTag(GFConstants.A_TAG);
		return options;
	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements es = doc.getElementsContainingOwnText(BY);
		
		String t = es.get(es.size()-1).text();
		String[] parts = t.split(BY);
		String author = parts[parts.length -1];
		
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements fonts = doc.getElementsByTag(GFConstants.FONT_TAG);
		String title = fonts.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;

	}

	@Override
	protected void extractChapter(Document page, Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);
	
		Elements chBodys = page.getElementsByTag(GFConstants.BODY_TAG);
		Element chBody = chBodys.first();

		body.appendChild(chBody);
		
		addChapterFooter(body);
		
		chap.setDoc(freshDoc);
	//	loc.addChapter(chap);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");

	}

	@Override
	protected boolean isOneShot(Document doc) throws Exception {
		boolean isOneShot = false;
		
		try {
			Document nav = getPage(navUrl);
			Elements options = getChapterOptions(nav);
			if (options == null || options.size() <= 1){
				isOneShot = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return isOneShot;
		
	}

	static public boolean isWitchFics(String url){
		boolean retVal = false;
		if (url.contains(WITCH_FICS)){
			retVal = true;
		}
		
		return retVal;
	}

	@Override
	protected Chapter extractSummary(Document page) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Document summary = initDocument();

		Chapter newCh = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(summary, newCh);
		
		Elements divs = page.getElementsByAttributeValue(GFConstants.CLASS_ATTR, "storytext");
		Element div = divs.first();
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		newCh.setDoc(summary);
	//	loc.addChapter(newCh);
		
		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return newCh;
	}

	//@Override
	/*
	Document getPage(String url) throws Exception {
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return super.getPage(url);
	}
	*/

	
}
