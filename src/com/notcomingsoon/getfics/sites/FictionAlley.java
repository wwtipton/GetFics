/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred
 *
 */
public class FictionAlley extends Site {
	
	private static final int FOOTER_STARTS = 4;
	private static final String GETINFO = "getinfo";
	private static final String CHAPTERLINK = "chapterlink";
	private static final String TITLE_CLASS = "title";
	private static final String TITLE_TEXT = "Title:";
	private static final String SUMMARY_TEXT = "Summary:";
	private static final String BY = " by ";

	private static final Cookie[] FICTION_ALLEY_COOKIES = new Cookie[]{new Cookie("fauser", "wizard")};

	private static final String BODY_TOP = "~~~~~~~~~~~~~";

	Boolean IS_ONE_SHOT = null;

	/**
	 * @param ficUrl
	 */
	public FictionAlley(String ficUrl) {
		super(ficUrl);
		super.cookies = FICTION_ALLEY_COOKIES;

	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		if (options != null && options.size() > 0){
	
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(HTMLConstants.HREF_ATTR);
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}
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
		String author = null;
		
		if (isOneShot(doc)){
			Elements es = doc.getElementsByAttributeValueContaining(HTMLConstants.HREF_ATTR, GETINFO);
			author = es.first().text();
		} else {
			Elements es = doc.getElementsByClass(TITLE_CLASS);
			
			String t = es.get(es.size()-1).text();
			String[] parts = t.split(BY);
			author = parts[parts.length -1];
		}
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;		
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		String title = null;
		
		if (isOneShot(doc)){
			Elements es = doc.getElementsContainingOwnText(TITLE_TEXT);
			Element e = es.first();
			e = e.nextElementSibling();
			title = e.text();
		} else {
			Elements es = doc.getElementsByClass(TITLE_CLASS);
			
			String t = es.get(es.size()-1).text();
			String[] parts = t.split(BY);
			title = parts[0];
		}
		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
	
		Elements pBreaks = chapter.getElementsContainingOwnText(BODY_TOP);
		Element pBreak = pBreaks.first();
		Elements siblings = pBreak.siblingElements();
		for (int i = pBreak.elementSiblingIndex(); i < siblings.size() - FOOTER_STARTS; i++){
			Element s = siblings.get(i);
			body.appendChild(s);
		}
		
		addChapterFooter(body);
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;

	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document doc) {
		if (IS_ONE_SHOT == null){
			IS_ONE_SHOT = Boolean.FALSE;
			
			Elements options = getChapterOptions(doc);
			if (options == null || options.size() <= 1){
				IS_ONE_SHOT = Boolean.TRUE;
			}
		}
		return IS_ONE_SHOT;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements uls = doc.getElementsByTag(HTMLConstants.UL_TAG);
		if (null != uls && uls.size() > 0){
			Element ul = uls.first();
			options = ul.getElementsByClass(CHAPTERLINK);
		}
		return options;
	}	
	
	public static boolean isFictionAlley(String url) {
		boolean retVal = false;
		if (url.contains(FICTION_ALLEY)){
			retVal = true;
		}
		
		return retVal;
	}

	@Override
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
		Chapter title = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, title);
		
		if (isOneShot(chapter)){
			Elements es = chapter.getElementsContainingOwnText(SUMMARY_TEXT);
			Element e = es.first();
			TextNode t = (TextNode) e.nextSibling();

			body.appendChild(t);
			
		} else {
			Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, "summary");
			Element div = divs.first();
			body.appendChild(div);
		}
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}


}
