package com.notcomingsoon.getfics.sites;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

public class MediaMiner extends Site {

	private static final int SUMMARY_TEXT_NODE = 2;
	private static final String VIEW_CHAPTER = "view_ch";
	private static final String USER_INFO = "user_info.php"; 
	private static final int AUTHOR_ANCHOR = 0;
	private static final String POST_TITLE = "post-title";
	private static final char ANGLE_BRACKET = '\u276f';
	private static final Charset MM_CHARSET = HTMLConstants.UTF_8;
	private static final int MULTI_CHAPTER = 2; 
	private static final int STORY_FORM = 0;
	private static final String SUMMARY_CLASS = "post-meta clearfix ";

	
	public MediaMiner(String ficUrl) {
		super(ficUrl);
		siteCharset = MM_CHARSET;
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		Elements options = getChapterOptions(doc);
		
		int chIndex = startUrl.lastIndexOf(HTMLConstants.SEPARATOR) + 1;
		String startChapter = startUrl.substring(chIndex);
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

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueContaining(HTMLConstants.HREF_ATTR, USER_INFO);
		
		String author = as.get(AUTHOR_ANCHOR).text();

		logger.info("author = " + author);
		
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Element header = doc.getElementById(POST_TITLE);
		String info = header.text();
		int stIndex = info.indexOf(ANGLE_BRACKET) + 1;
		int endIndex = info.lastIndexOf(ANGLE_BRACKET);
		String title = info.substring(stIndex, endIndex).trim();
		
		logger.info("title = " + title);

		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		Element div = chapter.getElementById("fanfic-text");
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
		Chapter title = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, title);
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, SUMMARY_CLASS);
		Element div = divs.first();
		List<TextNode> textNodes = div.textNodes();
		String t = textNodes.get(SUMMARY_TEXT_NODE).text();
		
		body.appendText(t);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (options.size() < MULTI_CHAPTER){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	public static boolean isMediaMiner(String url) {
		boolean retVal = false;
		if (url.contains(MEDIA_MINER)){
			retVal = true;
		}
		
		return retVal;
	}

	/**
	 * @param doc
	 * @return
	 */
	private Elements getChapterOptions(Document doc) {
		Elements options = new Elements();
		Elements forms = doc.getElementsByAttributeValue(HTMLConstants.NAME_ATTR, VIEW_CHAPTER);
		if (!forms.isEmpty()){
			Element form = forms.get(STORY_FORM);
			options = form.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}

}
