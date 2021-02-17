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
public class GrangerEnchanted extends Site {

	private static final Charset GE_CHARSET = HTMLConstants.ISO_8859_1;
	
	private static final String STORY = "story1";

	private static final String NOTES = "notes";

	private static final String JUMP_ATTR = "jump";

	private static final int JUMP_FORM = 0;

	private static final String VIEWSTORY = "viewstory";
	
	private static final String VIEWUSER = "viewuser";
	
	private static final String PEN_NAME_KEY = "penname";
	
	private static final String PEN_NAME = "Ouatic-7";
	
	private static final String PASSWORD_KEY = "password";
	
	private static final String PASSWORD = "my$73ry";
	
	private static final String COOKIE_CHECK = "cookiecheck";
	
	private static final String LOGIN_URL = "http://www.grangerenchanted.com/enchant/user.php?action=login";
	
	private static final String PHPSESSID = "PHPSESSID";

	private static final String SALT = "ZA2S5PQ6xD_salt";

	private static final String USERUID = "ZA2S5PQ6xD_useruid";

	private  Cookie[] GE_COOKIES =  new Cookie[]  {
			new Cookie(PHPSESSID, "b485ca6e0b36b97f27be544179aee5e6"),
			new Cookie(USERUID, "99545"),
			new Cookie(SALT, "cfcd208495d565ef66e7dff9f98764da") };


	private static final String ERRORTEXT = "errortext";
	
	private static final String WARNING = "The fic you are trying to access";

	
	Connection conn;

	public GrangerEnchanted(String ficUrl) throws IOException {
		super(ficUrl);
		siteCharset = GE_CHARSET;
		login();
		super.cookies = GE_COOKIES;
	}

	@Override
	void login() throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "login()");
		conn = Jsoup.connect(LOGIN_URL);
		conn.timeout(10000);
		
		conn.method(Connection.Method.POST);
		conn.data(PEN_NAME_KEY, PEN_NAME);
		conn.data(PASSWORD_KEY, PASSWORD);
		conn.data(COOKIE_CHECK, "1");
		conn.data("submit", "Go");

		Connection.Response resp = conn.execute();
		GE_COOKIES[0] = new Cookie(PHPSESSID, resp.cookie(PHPSESSID));
		GE_COOKIES[1] = new Cookie(SALT, resp.cookie(SALT) );
		GE_COOKIES[2] = new Cookie(USERUID, resp.cookie(USERUID) );
		
		logger.exiting(this.getClass().getCanonicalName(), "login()");
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		int chapterIndex = startUrl.lastIndexOf('=') + 1;
		String baseUrl = startUrl.substring(0, chapterIndex) ;
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(HTMLConstants.VALUE_ATTR);
			cUrl = baseUrl + cUrl;
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	private Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements forms = doc.getElementsByAttributeValue(HTMLConstants.NAME_ATTR, JUMP_ATTR);
		if (forms.size() > 0){
			Element form = forms.get(JUMP_FORM);
			options = form.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWUSER);
		
		String author = as.get(0).text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEWSTORY);
		
		String title = as.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);

		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, NOTES);
		if (divs.size() > 0){
			Element div = divs.first();
			body.appendChild(div);
		}
		
		divs = chapter.getElementsByAttributeValue(HTMLConstants.ID_ATTR, STORY);
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
		if (options == null || options.size() == 0){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	public static boolean isGrangerEnchanted(String url) {
		boolean retVal = false;
		if (url.contains(GRANGER_ENCHANTED)){
			retVal = true;
		}
		
		return retVal;
	}

	/*
	@Override
	Document getPage(String url) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getPage(String url)");
		conn = Jsoup.connect(url);
		
		conn = addCookies(conn);
		Document doc = conn.get();
		if (ageConsentRequired(doc)){
			String newUrl = url + ageConsent(doc);
			conn = Jsoup.connect(newUrl);
			conn = addCookies(conn);
			doc = conn.get();
		}
		doc = recode(doc, url);
		
		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");
		return doc;
	}
	*/

	@Override
	String ageConsent(Document doc) {
		Elements div = doc.getElementsByClass(ERRORTEXT);
		Elements as = div.get(0).getElementsByTag(HTMLConstants.A_TAG);
		Element a = as.first();
		String url = a.attr(HTMLConstants.HREF_ATTR);
		int ampersand = url.indexOf('&');
		url = url.substring(ampersand);
		
		return url;
	}

	@Override
	boolean ageConsentRequired(Document doc) {
		boolean isRequired = false;
		
		Elements div = doc.getElementsByClass(ERRORTEXT);
		if (null != div && div.size() > 0){
			String t = div.first().ownText();
			isRequired = t.contains(WARNING);
		}
		
		return isRequired;
	}
}
