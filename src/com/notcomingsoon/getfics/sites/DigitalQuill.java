/**
 * 
 */
package com.notcomingsoon.getfics.sites;

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
public class DigitalQuill extends Site {

	private static final Charset DIGITAL_QUILL_CHARSET = HTMLConstants.WIN_1252;
	private static final String VIEWUSER = "viewuser";
	private static final String VIEWSTORY = "viewstory";

	private static final int FIRST_FORM = 0;
	private static final int TITLE_TABLE = 1;
	private static final String JAVASCRIPT = "javascript";
	private static final int MAIN_TABLE = 0;
	private static final int MAIN_TD = 0;
	private static final int IMAGE = 0;
	

	/**
	 * @param ficUrl
	 */
	public DigitalQuill(String ficUrl) {
		super(ficUrl);
		
		this.siteCharset = DIGITAL_QUILL_CHARSET;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Element form = getFirstForm(doc);
		Elements options = form.getElementsByAttributeValueStarting(HTMLConstants.VALUE_ATTR, VIEWSTORY);
		
		//first element is "story index" text rather than chapter
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



	/**
	 * @param doc
	 * @return
	 */
	private Element getFirstForm(Document doc) {
		Elements forms = doc.getElementsByTag(HTMLConstants.FORM_TAG);
		Element form = forms.get(FIRST_FORM);
		return form;
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
		
		Elements as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, JAVASCRIPT);
		if (as.isEmpty()){
			as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWSTORY);
		}
		
		String title = as.get(0).text();
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
		
		Elements tables = chapter.getElementsByTag(HTMLConstants.TABLE_TAG);
		for (int i=0; i < tables.size(); i++){
			if (i != MAIN_TABLE){
				Element table = tables.get(i);
				table.remove();
			}
		}
		Element mainTable = chapter.getElementsByTag(HTMLConstants.TABLE_TAG).get(MAIN_TABLE);
		Element td = mainTable.getElementsByTag(HTMLConstants.TD_TAG).get(MAIN_TD);
		td.tagName(HTMLConstants.SPAN_TAG);
		td.removeAttr(HTMLConstants.BGCOLOR_ATTR);		
		
		Elements imgs = td.getElementsByTag(HTMLConstants.IMG_TAG);
		Element img = imgs.get(IMAGE);
		img.remove();

		Elements divs = td.getElementsByTag(HTMLConstants.DIV_TAG);
		Element div = divs.get(IMAGE);
		div.remove();	
		
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
		return doc.getElementsByTag(HTMLConstants.FORM_TAG).isEmpty();
	}


	static public boolean isDigitalQuill(String url){
		boolean retVal = false;
		if (url.contains(DIGITAL_QUILL)){
			retVal = true;
		}
		
		return retVal;
	}
}
