package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * Note that this is via the Wayback Machine so everything is nested inside WBM stuff.
 * 
 * @author Winifred
 *
 */
public class WitchFics extends Site {
	
	private static final String BY = " by ";

	//private static final Charset WBM_CHARSET = HTMLConstants.UTF_8;
	
	private static final String SRC = "src";

	private static final String WAYBACK_MACHINE_URL = "https://web.archive.org";

	private static final String PHPSESSID = "PHPSESSID";
			
	String iframeUrl = null;
	
	String introUrl = null;
	
	String navUrl = null;
	
	private  Cookie[] WITCH_FIC_COOKIES =  new Cookie[]{ new Cookie(PHPSESSID, "pg6dm0elglserhrbeqtfb7h4l3")};

	

	public WitchFics(String ficUrl) throws IOException {
		super(ficUrl);
		super.cookies = WITCH_FIC_COOKIES;
		getNestedSite();
	}

	private void getNestedSite() throws IOException {
		// This page has wayback machine stuff.
		Document doc = getPage(startUrl);
		Element iframe = doc.getElementById("playback");
		iframeUrl = iframe.attr(SRC);
		
		doc = getPage(iframeUrl);
		Elements frames = doc.getElementsByTag(HTMLConstants.FRAME_TAG);
		introUrl = WAYBACK_MACHINE_URL + frames.get(0).attr(SRC);
		startUrl = introUrl;
		navUrl = WAYBACK_MACHINE_URL + frames.get(1).attr(SRC);
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
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
			
			String urlSuffix = null;
			String urlPrefix = null;
			int slashIndex = startUrl.lastIndexOf(HTMLConstants.URL_DIVIDER);
			urlPrefix = startUrl.substring(0, slashIndex+1);
			logger.info("urlPrefix = " + urlPrefix); 
	
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(HTMLConstants.HREF_ATTR);
				cUrl = urlPrefix + cUrl;
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements divs = doc.getElementsByTag(HTMLConstants.DIV_TAG);
		Element div = divs.first();
		options = div.getElementsByTag(HTMLConstants.A_TAG);
		return options;
	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements es = doc.getElementsContainingOwnText(BY);
		
		String t = es.get(es.size()-1).text();
		String[] parts = t.split(BY);
		String author = parts[parts.length -1];
		
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements fonts = doc.getElementsByTag(HTMLConstants.FONT_TAG);
		String title = fonts.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;

	}

	@Override
	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
	
		Elements chBodys = chapter.getElementsByTag(HTMLConstants.BODY_TAG);
		Element chBody = chBodys.first();

		body.appendChild(chBody);
		
		addChapterFooter(body);
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;

	}

	@Override
	protected boolean isOneShot(Document doc) {
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
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
		Chapter title = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, title);
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, "storytext");
		Element div = divs.first();
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	@Override
	Document getPage(String url) throws IOException {
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return super.getPage(url);
	}

	
}