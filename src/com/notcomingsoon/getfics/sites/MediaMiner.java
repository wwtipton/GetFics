package com.notcomingsoon.getfics.sites;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.files.Chapter;

public class MediaMiner extends Site {

	private static final int SUMMARY_TEXT_NODE = 2;
	private static final String VIEW_CHAPTER = "view_ch";
	private static final String USER_INFO = "user_info.php"; 
	private static final int AUTHOR_ANCHOR = 0;
	private static final String POST_TITLE = "post-title";
	private static final char ANGLE_BRACKET = '\u276f';
	private static final Charset MM_CHARSET = GFConstants.UTF_8;
	private static final int MULTI_CHAPTER = 2; 
	private static final int STORY_FORM = 0;
	private static final String SUMMARY_CLASS = "post-meta clearfix ";

	
	public MediaMiner(String ficUrl) {
		super(ficUrl);
		siteCharset = MM_CHARSET;
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		Elements options = getChapterOptions(doc);
		
		int chIndex = startUrl.lastIndexOf(GFConstants.SEPARATOR) + 1;
		String startChapter = startUrl.substring(chIndex);
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

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueContaining(GFConstants.HREF_ATTR, USER_INFO);
		
		String author = as.get(AUTHOR_ANCHOR).text();

		logger.info("author = " + author);
		
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Element header = doc.getElementById(POST_TITLE);
		String info = header.text();
		int stIndex = info.indexOf(ANGLE_BRACKET) + 1;
		int endIndex = info.lastIndexOf(ANGLE_BRACKET);
		String title = info.substring(stIndex, endIndex).trim();
		
		logger.info("title = " + title);

		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected void extractChapter(Document page, Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);
		Element div = page.getElementById("fanfic-text");
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		chap.setDoc(freshDoc);
//		loc.addChapter(chap);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
	}

	@Override
	protected Chapter extractSummary(Document page) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Document freshDoc = initDocument();
		Chapter summary = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(freshDoc, summary);
		
		Elements divs = page.getElementsByAttributeValue(GFConstants.CLASS_ATTR, SUMMARY_CLASS);
		Element div = divs.first();
		List<TextNode> textNodes = div.textNodes();
		String t = textNodes.get(SUMMARY_TEXT_NODE).text();
		
		body.appendText(t);
		
		addChapterFooter(body);
	
		summary.setDoc(freshDoc);
	//	loc.addChapter(summary);
				
		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return summary;
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
		Elements forms = doc.getElementsByAttributeValue(GFConstants.NAME_ATTR, VIEW_CHAPTER);
		if (!forms.isEmpty()){
			Element form = forms.get(STORY_FORM);
			options = form.getElementsByTag(GFConstants.OPTION_TAG);
		}
		return options;
	}

}
