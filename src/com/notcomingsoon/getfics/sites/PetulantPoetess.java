/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class PetulantPoetess extends Site {

	private static final int SUMMARY_TEXT_NODE = 1;
	private static final int USER_LINK = 0;
	private static final Charset TPP_CHARSET = HTMLConstants.WIN_1252;
	private static final String VIEWUSER = "viewuser";
	private static final String VIEWSTORY = "viewstory";
	private static final int TITLE_DIV_INDEX = 6;
	private static final int CHAPTER_SELECT = 0;
	private static final String NAME = "name";
	private static final String SID = "sid";
	private static final int CHAPTER_BODY = 5;
	private Node emptyNode = new TextNode("");
	private static String NEXT_LINK = "[Next]";
	
	private static final int SUMMARY_ROW = 2;

	private static  URI U = null;
	static{
		try {
			U = new URI("http://www.thepetulantpoetess.com/");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TODO these are from before the site move and may no longer be valid.
		addCookie(U,"level", "0");
		addCookie(U,"adminloggedin", "0");
		addCookie(U,"loggedin", "1");
		addCookie(U,"penname", "Ouatic");
		addCookie(U,"userskin", "GraphicLite");
		addCookie(U,"useruid", "22383");
	}

	/**
	 * @param ficUrl
	 */
	public PetulantPoetess(String ficUrl) {
		super(ficUrl);
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

	@Override
	protected Chapter extractSummary(Document story, Document chapter) throws Exception {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
		Chapter title = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, title);
		
		Elements elements = chapter.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWUSER);
		Element userElement = elements.get(USER_LINK);
		String userRef = userElement.attr(HTMLConstants.HREF_ATTR);
		int sepIdx = startUrl.lastIndexOf(HTMLConstants.SEPARATOR);
		String storyRef = startUrl.substring(sepIdx + 1);
		int ampIdx = storyRef.indexOf("&");
		if (ampIdx > 0){
			storyRef = storyRef.substring(0, ampIdx);
		}
		String baseUrl = startUrl.substring(0, sepIdx + 1);
		
		String summary = searchAuthor(story.tag(), baseUrl, userRef, storyRef);
		
		body.appendText(summary);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	private String searchAuthor(Tag storyTag, String baseUrl, String userRef, String storyRef) throws Exception {
		Element summary = new Element(storyTag, "");
		String summaryText = null;
		String authorUrl = baseUrl + userRef;
		while (summaryText == null && authorUrl != null){
			try {
				Document authorWorks = getPage(authorUrl);
				Elements aList = authorWorks.getElementsContainingText(NEXT_LINK);
				Element a = aList.last();
				if (a != null){
					authorUrl = baseUrl + a.attr(HTMLConstants.HREF_ATTR);
				} else {
					authorUrl = null;
				}
				aList = authorWorks.getElementsByAttributeValue(HTMLConstants.HREF_ATTR, storyRef);
				if (!aList.isEmpty()){
					Element table  = aList.first().parent().parent().parent().parent();
					Elements trList = table.getElementsByTag(HTMLConstants.TR_TAG);
					Element tr = trList.get(SUMMARY_ROW);
					Elements eList = tr.getElementsContainingText(SUMMARY_STRING);
					Element e = eList.get(SUMMARY_TEXT_NODE);
					int childCnt = e.childNodeSize();
					summaryText = e.childNode(childCnt - 1).toString();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return summaryText;
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
