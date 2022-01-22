/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class AdultFanFiction extends Site {
	
	private static final int AUTHOR_ANCHOR = 5;

	private static final int MAIN_TABLE = 2;

	private static final String MEMBERS_ATTR = "members";
	
	public static final String TITLE_TAG = "title";
	
	public static final String STORY_PREFIX = "Story: ";

	private static final String STORY = "story";
	
	private static final Charset AFF_CHARSET = HTMLConstants.WIN_1252;
	
	private static final String AFF_SHORT = "adult-fanfiction.org";

	static{
		try {
			URI U = new URI(AFF);
			addCookie(U,"bdv","10%2F25%2F1960");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	/**
	 * @param ficUrl)
	 */
	public AdultFanFiction(String ficUrl) 
	{
		super(ficUrl);
		siteCharset = AFF_CHARSET;
	}

	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		
		Element table = getMainTable(chapter);
		Elements trs = table.getElementsByTag(HTMLConstants.TR_TAG);
		Element tr = trs.get(5);
		Elements tds = tr.getElementsByTag(HTMLConstants.TD_TAG);
		Element td = tds.get(0);
		
		
		body.appendChild(td);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		return story;
	}

	
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueContaining(HTMLConstants.HREF_ATTR, MEMBERS_ATTR);
		
		String author = as.get(AUTHOR_ANCHOR).text();

		logger.info("author = " + author);
		
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;

	}

	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		Elements options = getChapterOptions(doc);
		
		int storyIndex = startUrl.indexOf(STORY);
		String startChapter = startUrl.substring(storyIndex);
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(HTMLConstants.HREF_ATTR);
			cUrl = startUrl.replace(startChapter, cUrl);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
		return list;
	}

	/**
	 * @param doc
	 * @return
	 */
	private Elements getChapterOptions(Document doc) {
		Elements divs = doc.getElementsByClass("dropdown-content");
		Element div = divs.first();
		Elements options = div.getElementsByTag(HTMLConstants.A_TAG);
		return options;
	}

	/**
	 * @param doc
	 * @return
	 */
	private Element getMainTable(Document doc) {
		Elements forms = doc.getElementsByTag(HTMLConstants.TABLE_TAG);
		Element form = forms.get(MAIN_TABLE);
		return form;
	}

	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Element mTable = getMainTable(doc);
		Elements as = mTable.getElementsByAttributeValueContaining(HTMLConstants.HREF_ATTR, STORY);
		String title = as.get(0).text();
		
		logger.info("title = " + title);

		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (options.size() == 1){
			isOneShot = true;
		}
		
		return isOneShot;
	}


	static public boolean isAFF(String url){
		boolean retVal = false;
		if (url.contains(AFF_SHORT)){
			retVal = true;
		}
		
		return retVal;
	}
}
