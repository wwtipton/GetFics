package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;
public class ArchiveOfOurOwn extends Site {

	private static final Charset AO3_CHARSET = HTMLConstants.UTF_8;
	
	private  Cookie[] AO3_COOKIES =  new Cookie[]  {
		new Cookie("_otwarchive_session", "BAh7CUkiD3Nlc3Npb25faWQGOgZFRkkiJTY0YWVlZDdjZDdkNWQwZTM3YzZlOTA5MjBjNzRkMmI1BjsAVEkiDnJldHVybl90bwY7AEYiEi93b3Jrcy83NTQ5MDhJIhBfY3NyZl90b2tlbgY7AEZJIjFuV0w4OEVUQjVNRCtTK2pQVVg3bFdkbk9lUXNJZkJwWGl1bnV4SnlQWkFnPQY7AEZJIgphZHVsdAY7AEZU--16146c087112e40ee8c6b8546930624441753bbe")
	};
	//private static final String USERS = "/users";
	private static final String AUTHOR = "byline heading";
	
	private static final int CHAPTER_SELECT = 0;

	private static final String URL_DIVIDER = "/";

	private static final String TITLE = "title heading";

	private static final String CLASS = "class";

	private static final String USERSTUFF = "userstuff";

	private static final String USERSTUFF_MODULE = "userstuff module";

	private static final String SUMMARY_MODULE = "summary module";

	private static final String WORK = "work";
	
	private static final String ADULT = "?view_adult=true";
	
	Connection conn;

	public ArchiveOfOurOwn(String ficUrl) throws IOException {
		super(ficUrl);
		logger.entering(this.getClass().getCanonicalName(), "ArchiveOfOurOwn(String ficUrl)");
		logger.finer("startUrl = " + startUrl);
		if (!startUrl.contains(ADULT)){
			startUrl = startUrl + ADULT;
		}
		logger.finer("startUrl = " + startUrl);
		siteCharset = AO3_CHARSET;
		super.cookies = AO3_COOKIES;
		logger.exiting(this.getClass().getCanonicalName(), "ArchiveOfOurOwn(String ficUrl)");
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		if (options != null){
			
			String urlPrefix = null;
			int slashIndex = startUrl.lastIndexOf(URL_DIVIDER);
			urlPrefix = startUrl.substring(0, slashIndex+1);
			logger.info("urlPrefix = " + urlPrefix); 
	
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(HTMLConstants.VALUE_ATTR);
				cUrl = urlPrefix + cUrl + ADULT;
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	private Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements selects = doc.getElementsByAttributeValue("id","selected_id");
		if (!selects.isEmpty()){
			Element select = selects.get(CHAPTER_SELECT);
			options = select.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(CLASS, AUTHOR);
		
		String author = as.get(0).text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements h2S = doc.getElementsByAttributeValueStarting(CLASS, TITLE);
		
		String title = h2S.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);

		Element chapterText = null;
		if (isOneShot(chapter)){
			Elements divs = chapter.getElementsByTag(HTMLConstants.DIV_TAG);
			for (Element div : divs){
				if (div.hasClass(USERSTUFF)){
					chapterText = div;
					break;
				}
			}
		} else {
			Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, USERSTUFF_MODULE);
			chapterText = divs.first();
			Elements h3s = chapterText.getElementsByAttributeValue(HTMLConstants.ID_ATTR, WORK);
			Element h3 = h3s.first();
			h3.empty();
		}
		
		body.appendChild(chapterText);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
		Chapter title = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, title);
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, SUMMARY_MODULE);
		Element div = divs.first();
		Element p = div.getElementsByTag(HTMLConstants.P_TAG).first();
		
		body.appendChild(p);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (options == null || options.size() == 0){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	public static boolean isAO3(String url) {
		boolean retVal = false;
		if (url.contains(AO3)){
			retVal = true;
		}
		
		return retVal;
	}

	@Override
	Document getPage(String url) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getPage(String url)");
		conn = Jsoup.connect(url);
//		conn.timeout(15000);
		conn.timeout(0);
		
		conn = addCookies(conn);
		Document doc = conn.get();
		doc = recode(doc, url);
		
		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");
		return doc;
	}

}
