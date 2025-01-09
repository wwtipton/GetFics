/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.files.Chapter;

/**
 * @author Winifred Tipton
 *
 */
public class PetulantPoetess extends Site {

	private static final int SUMMARY_TEXT_NODE = 1;
	private static final int USER_LINK = 0;
	private static final Charset TPP_CHARSET = GFConstants.WIN_1252;
	private static final String VIEWUSER = "viewuser";
	private static final String VIEWSTORY = "viewstory";
	private static final int TITLE_DIV_INDEX = 6;
	private static final int CHAPTER_SELECT = 0;
	private static final String NAME = "name";
	private static final String SID = "sid";
	private static final int CHAPTER_BODY = 5;
	private Node emptyNode = new TextNode("");
	private static String NEXT_LINK = "[Next]";
	
	private static final String PEN_NAME = GFProperties.getPropertyValue(GFProperties.TPP_PEN_NAME);

	
	static{
		try {
			URI U = new URI(TPP);
			addCookie(U,"level", "0"); 
			addCookie(U,"loggedin", "1");
			addCookie(U,"penname", PEN_NAME); 
			addCookie(U,"userskin", "GraphicLite"); 
			addCookie(U,"useruid", "22383");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		 	
	private static final int SUMMARY_ROW = 2;
	
	private Document toc = null;

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
	protected ArrayList<Chapter> getChapterList(Document doc) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
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
			String cUrl = option.attr(GFConstants.VALUE_ATTR);
			cUrl = startUrl.replace(startChapter, cUrl);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
		return list;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		getTOCPage();
		
		Elements h3s = toc.getElementsByTag(GFConstants.H3_TAG);
		
		String author = h3s.first().text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements divs = doc.getElementsByTag(GFConstants.DIV_TAG);
		Element div = divs.get(TITLE_DIV_INDEX);
		
		Elements bs = div.getElementsByTag(GFConstants.B_TAG);
		
		String title = bs.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected void extractChapter(Document page, Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");

		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);
		
		Elements tds = page.getElementsByTag(GFConstants.TD_TAG);
		Element td = tds.get(CHAPTER_BODY);
		
		td.tagName(GFConstants.SPAN_TAG);
		td.removeAttr(GFConstants.COLSPAN_ATTR);
		td.removeAttr(GFConstants.BGCOLOR_ATTR);
		Elements divs = td.getElementsByTag(GFConstants.DIV_TAG);
		for (Element div : divs){
			div.replaceWith(emptyNode);
		}
		
		body.appendChild(td);
		
		addChapterFooter(body);
		
		chap.setDoc(freshDoc);
	//	loc.addChapter(chap);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
	}

	@Override
	protected Chapter extractSummary(Document page) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Document freshDoc = initDocument();

		Chapter summary = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(freshDoc, summary);

		getTOCPage();

		addChapterFooter(body);
		
		summary.setDoc(freshDoc);
	//	loc.addChapter(summary);
		
		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return summary;
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
					authorUrl = baseUrl + a.attr(GFConstants.HREF_ATTR);
				} else {
					authorUrl = null;
				}
				aList = authorWorks.getElementsByAttributeValue(GFConstants.HREF_ATTR, storyRef);
				if (!aList.isEmpty()){
					Element table  = aList.first().parent().parent().parent().parent();
					Elements trList = table.getElementsByTag(GFConstants.TR_TAG);
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
			options = select.getElementsByTag(GFConstants.OPTION_TAG);
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
	
	/**
	 * Fetch table of contents page for story. It contains summary and list of chapters.
	 * 
	 * @return toc 
	 * @throws IOException
	 */
	private Document getTOCPage() throws Exception {
		if (null == toc) {
			int index = startUrl.lastIndexOf(SLASH);
			index = startUrl.lastIndexOf(SLASH, index);
			String tocUrl = startUrl.substring(0, index);
			toc = getPage(tocUrl);
		}
		return toc;
	}


}
