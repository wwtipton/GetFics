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
public class TheMasqueNet extends Site {

	private static final Charset MASQUE_CHARSET = HTMLConstants.UTF_8;
	
	private  Cookie[] MASQUE_COOKIES =  new Cookie[]  {
		new Cookie("PHPSESSID", "19d1319ded032a2ce278d957f54c3bcc"),
		new Cookie("jPKKerrGED_salt", "cfcd208495d565ef66e7dff9f98764da"),
		new Cookie("jPKKerrGED_useruid", "11496") };

	private static final String STORY = "story";

	private static final String JUMP_ATTR = "jump";

	private static final int JUMP_FORM = 0;

	private static final String VIEWSTORY = "viewstory";
	
	private static final String VIEWUSER = "viewuser";
	
	private static final String PEN_NAME_KEY = "penname";
	
	private static final String PEN_NAME = "Ouatic-7";
	
	private static final String PASSWORD_KEY = "password";
	
	private static final String PASSWORD = "d6eath";
	
	private static final String COOKIE_CHECK = "cookiecheck";
	
	private static final String LOGIN_URL = "http://www.themasque.net/wiktt/efiction/user.php?action=login";
	
	private static final String PHPSESSID = "PHPSESSID";

	private static final String SALT = "jPKKerrGED_salt";

	private static final String USERUID = "jPKKerrGED_useruid";

	private static final String ERRORTEXT = "errortext";
	
	private static final String AGE_CONSENT_REQUIRED = "Age Consent Required";

	private static final String MILD = "This story may contain mild sexual situations or violence.";
	
	Connection conn;

	public TheMasqueNet(String ficUrl) throws IOException {
		super(ficUrl);
		siteCharset = MASQUE_CHARSET;
		login();
		super.cookies = MASQUE_COOKIES;
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
		MASQUE_COOKIES[0] = new Cookie(PHPSESSID, resp.cookie(PHPSESSID));
		MASQUE_COOKIES[1] = new Cookie(SALT, resp.cookie(SALT) );
		MASQUE_COOKIES[2] = new Cookie(USERUID, resp.cookie(USERUID) );
		
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
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.ID_ATTR, STORY);
		Element div = divs.first();
		//div.removeAttr(HTMLConstants.ID_ATTR);
		
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

	public static boolean isMasque(String url) {
		boolean retVal = false;
		if (url.contains(THE_MASQUE)){
			retVal = true;
		}
		
		return retVal;
	}

	@Override
	Document getPage(String url) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getPage(String url)");

		String localUrl = url;
		Document doc = super.getPage(localUrl);
		if (ageConsentRequired(doc)){
			localUrl = localUrl + ageConsent(doc);
			doc = super.getPage(localUrl);
		}
		doc = recode(doc, localUrl);
		
		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");
		return doc;
	}

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
			isRequired = AGE_CONSENT_REQUIRED.equalsIgnoreCase(t) || MILD.equalsIgnoreCase(t);
		}
		
		return isRequired;
	}
}
