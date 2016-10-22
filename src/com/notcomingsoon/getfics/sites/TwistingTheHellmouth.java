package com.notcomingsoon.getfics.sites;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

public class TwistingTheHellmouth extends Site {

	private static final Charset TTH_CHARSET = HTMLConstants.UTF_8;

	private static final Cookie[] TTH_COOKIES = new Cookie[]{new Cookie("login", "4926%7C15572996591106354041835721568819406381120385835280%7C0")};

	private static final int AUTHOR_TABLE = 1;

	private static final int AUTHOR_CELL = 1;

	private static final int CHAPTER_SELECT_FORM = 2;

	private static final String STORY = "/story";

	private static final String STORYINNERBODY = "storyinnerbody";

	
	public TwistingTheHellmouth(String ficUrl) {
		super(ficUrl);
		super.cookies = TTH_COOKIES;
		siteCharset = TTH_CHARSET;
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		if (options != null){
			int storyIndex = startUrl.toLowerCase().indexOf(STORY);
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

		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;

	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements tables = doc.getElementsByTag(HTMLConstants.TABLE_TAG);
		Element table = tables.get(AUTHOR_TABLE);
		Elements tds = table.getElementsByTag(HTMLConstants.TD_TAG);
		Element td = tds.get(AUTHOR_CELL);
		
		String author = td.text();
		
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements h2s = doc.getElementsByTag(HTMLConstants.H2_TAG);
		Element h2 = h2s.first();
		String title = h2.text();
		
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.ID_ATTR, STORYINNERBODY);
		Element div = divs.first();
		div.removeAttr(HTMLConstants.ID_ATTR);
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;

	}

	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (null == options || options.isEmpty()){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	public static boolean isTTH(String url) {
		boolean retVal = false;
		if (url.contains(TTH)){
			retVal = true;
		}
		
		return retVal;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements forms = doc.getElementsByTag(HTMLConstants.FORM_TAG);
		if (forms.size() > CHAPTER_SELECT_FORM){
			Element form = forms.get(CHAPTER_SELECT_FORM);
			options = form.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}


}
