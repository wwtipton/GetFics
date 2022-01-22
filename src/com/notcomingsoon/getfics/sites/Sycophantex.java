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
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class Sycophantex extends Site {

	private static final String COOKIE_USER_NAME_KEY = "uname";
	private static final String COOKIE_USER_NAME_VALUE = GFProperties.getPropertyValue(GFProperties.SYCOPHANTEX_PEN_NAME);
	
	private static final String COOKIE_USER_PASSWORD_KEY = "upass";
	private static final String COOKIE_USER_PASSWORD_VALUE = "b2c198e49284d1fcae577ee91601a5fe";
	
	private static final String VIEWSTORY = "viewstory";
	private static final String VIEWUSER = "viewuser";

	private static final int FIRST_FORM = 0;
	
	private static final Charset SYCOPHANTEX_CHARSET = HTMLConstants.WIN_1252;

	static{
		try {
			URI U = new URI(SYCOPHANTEX);
			addCookie(U,COOKIE_USER_NAME_KEY, COOKIE_USER_NAME_VALUE);
			addCookie(U,COOKIE_USER_PASSWORD_KEY, COOKIE_USER_PASSWORD_VALUE);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public Sycophantex(String ficURL) {
		super(ficURL);
		
		this.siteCharset = SYCOPHANTEX_CHARSET;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		
		Elements spans = chapter.getElementsByTag(HTMLConstants.SPAN_TAG);
		body.appendChild(spans.first());
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWUSER);
		
		String author = as.get(0).text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
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
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
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

	

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWSTORY);
		
		String title = as.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}
	
	@Override
	protected boolean isOneShot(Document doc) {
		return doc.getElementsByTag(HTMLConstants.FORM_TAG).isEmpty();
	}


	static public boolean isSycophantex(String url){
		boolean retVal = false;
		if (url.contains(SYCOPHANTEX)){
			retVal = true;
		}
		
		return retVal;
	}	

}
