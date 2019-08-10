package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;
public class ArchiveOfOurOwn extends Site {

	private static final String REMEMBER_ME_KEY = "user_session[remember_me]";

	private static final String USER_SESSIONS = "https://archiveofourown.org/user_sessions";

	private static final String AUTHENTICITY_TOKEN = "authenticity_token";

	private static final Charset AO3_CHARSET = HTMLConstants.UTF_8;
	
	private  Cookie[] AO3_COOKIES =  new Cookie[4];
	//private static final String USERS = "/users";
//	private static final String AUTHOR = "author";
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
	
	private static final String RESTRICTED = "?restricted=true";

	private static final String LOGIN_PROMPT = "user_session_login";
	
	private static final String PEN_NAME_KEY = "user[login]";
	
	private static final String PEN_NAME = "Ouatic7";
	
	private static final String PASSWORD_KEY = "user[password]";
	
	private static final String PASSWORD = "d6eath";

	private static final String LOGIN_URL = "https://archiveofourown.org/users/login";
	

	
	Connection conn;

	public ArchiveOfOurOwn(String ficUrl) throws IOException {
		super(ficUrl);
		logger.entering(this.getClass().getCanonicalName(), "ArchiveOfOurOwn(String ficUrl)");
		logger.finer("startUrl = " + startUrl);
		siteCharset = AO3_CHARSET;
		login();
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
		
		Elements as = doc.getElementsByClass(AUTHOR);
		
		String author = as.get(0).text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements h2S = doc.getElementsByClass("title heading");
		
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
		
		Chapter title = null;
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, SUMMARY_MODULE);
		Element div = divs.first();
		if (div != null){
			Element p = div.getElementsByTag(HTMLConstants.P_TAG).first();
			if (p != null){
				title = new Chapter(this.startUrl, SUMMARY_STRING);
				Element body = addChapterHeader(story, title);
				body.appendChild(p);
				addChapterFooter(body);	
			}
		}
		
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
	void login() throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "login()");
		conn = Jsoup.connect(LOGIN_URL);
		conn.timeout(180000);
		conn.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
		Connection.Response resp = conn.execute();
		Document doc = resp.parse();
		Elements elist = doc.getElementsByAttributeValue("name",AUTHENTICITY_TOKEN);
		String token = elist.last().attr("value");
		Map<String, String> cookies = resp.cookies();
		
		//conn = Jsoup.connect(USER_SESSIONS);
	//	conn = Jsoup.connect(LOGIN_URL);
		conn.method(Connection.Method.POST);
		conn.cookies(cookies);
		conn.data(PEN_NAME_KEY, PEN_NAME);
		conn.data(PASSWORD_KEY, PASSWORD);
		conn.data(REMEMBER_ME_KEY, "1");
	//	conn.data("utf8", "&#x2713;");
		conn.data(AUTHENTICITY_TOKEN, token);
		conn.data("commit", "Log in");

		Connection.Response resp2 = null;
		try {
			resp2 = conn.execute();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		Document doc2 = resp2.parse();
		Map<String, String> cookies2 = resp2.cookies();
		Set<String> keys = cookies2.keySet();
		AO3_COOKIES = new Cookie[keys.size()];
		
		int i = 0;
		for(String key : keys){
			AO3_COOKIES[i] = new Cookie(key, cookies2.get(key));
			i++;
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "login()");
	}

}
