/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class PetulantPoetess extends Site {

	private static final Charset TPP_CHARSET = HTMLConstants.WIN_1252;
	private static final String VIEWUSER = "viewuser";
	private static final String VIEWSTORY = "viewstory";
	private static final int TITLE_DIV_INDEX = 6;
	private static final int CHAPTER_SELECT = 0;
	private static final String NAME = "name";
	private static final String SID = "sid";
	private static final int CHAPTER_BODY = 5;
	private Node emptyNode = new TextNode("",startUrl);
	
	private static final Cookie[] TPP_COOKIES = new Cookie[]
          {
		 	new Cookie("level", "0"), 
		 	new Cookie("adminloggedin", "0"), 
			new Cookie("loggedin", "1"), 
			new Cookie("penname", "Ouatic"), 
			new Cookie("userskin", "GraphicLite"), 
			new Cookie("useruid", "22383")
		 };

	/**
	 * @param ficUrl
	 */
	public PetulantPoetess(String ficUrl) {
		super(ficUrl);
		super.cookies = TPP_COOKIES;
		siteCharset = TPP_CHARSET;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		Elements options = getChapterOptions(doc);
		
		//first two elements are "chapter" and "story index" text rather than chapter
		options.remove(0);
		options.remove(0);
		
		int storyIndex = startUrl.indexOf(VIEWSTORY);
		String startChapter = startUrl.substring(storyIndex);
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(HTMLConstants.VALUE_ATTR);
			cUrl = startUrl.replace(startChapter, cUrl);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWUSER);
		
		String author = as.get(0).text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements divs = doc.getElementsByTag(HTMLConstants.DIV_TAG);
		Element div = divs.get(TITLE_DIV_INDEX);
		
		Elements bs = div.getElementsByTag(HTMLConstants.B_TAG);
		
		String title = bs.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		
		Elements tds = chapter.getElementsByTag(HTMLConstants.TD_TAG);
		Element td = tds.get(CHAPTER_BODY);
		
		td.tagName(HTMLConstants.SPAN_TAG);
		td.removeAttr(HTMLConstants.COLSPAN_ATTR);
		td.removeAttr(HTMLConstants.BGCOLOR_ATTR);
		Elements divs = td.getElementsByTag(HTMLConstants.DIV_TAG);
		for (Element div : divs){
			div.replaceWith(emptyNode);
		}
		
		body.appendChild(td);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;
	}



	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (options == null || options.size() == 0){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements selects = doc.getElementsByAttributeValue(NAME, SID);
		if (!selects.isEmpty()){
			Element select = selects.get(CHAPTER_SELECT);
			options = select.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}	

	static public boolean isTPP(String url){
		boolean retVal = false;
		if (url.contains(TPP)){
			retVal = true;
		}
		
		return retVal;
	}
}
