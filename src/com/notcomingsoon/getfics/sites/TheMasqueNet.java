package com.notcomingsoon.getfics.sites;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.files.Chapter;

public class TheMasqueNet extends Site {

	private static final Charset MASQUE_CHARSET = GFConstants.UTF_8;
	
	static{
		try {
			URI U = new URI(THE_MASQUE);
			addCookie(U,"jPKKerrGED_salt", "cfcd208495d565ef66e7dff9f98764da");
			addCookie(U,"jPKKerrGED_useruid", "28409");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final String STORY = "story";

	private static final String JUMP_ATTR = "jump";

	private static final int JUMP_FORM = 0;

	private static final String VIEWSTORY = "viewstory";
	
	private static final String VIEWUSER = "viewuser";
	
	private static final String PEN_NAME_KEY = "penname";
	
	private static final String PEN_NAME = "wwtipton";
	
	private static final String PASSWORD_KEY = "password";
	
	private static final String PASSWORD = "d6eath";
	
	private static final String COOKIE_CHECK = "cookiecheck";
	
	private static final String LOGIN_URL = "http://www.themasque.net/wiktt/efiction/user.php?action=login";
	
//	private static final String PHPSESSID = "PHPSESSID";

//	private static final String SALT = "jPKKerrGED_salt";

//	private static final String USERUID = "jPKKerrGED_useruid";

	private static final String ERRORTEXT = "errortext";
	
	private static final String AGE_CONSENT_REQUIRED = "Age Consent Required";

	private static final String MILD = "This story may contain mild sexual situations or violence.";
	
	private static boolean loggedIn;

	public TheMasqueNet(String ficUrl) throws Exception {
		super(ficUrl);
		siteCharset = MASQUE_CHARSET;
		if (!loggedIn) {
			login();
		}
	}

	@Override
	void login() throws Exception {
		logger.entering(this.getClass().getSimpleName(), "login()");

		waitRandom();
		
		Map<String, String> user = new HashMap<String, String>();
		user.put(PEN_NAME_KEY, PEN_NAME);
		user.put(PASSWORD_KEY, PASSWORD);
		user.put(COOKIE_CHECK, "1");
		user.put("submit", "Go");
		
	    HttpRequest.Builder builder = getRequestBuilder(LOGIN_URL);
	    builder.POST(ofFormData(user));

	    HttpRequest request = builder.build();
	    
		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
		
		if (response.statusCode()==200) {
			loggedIn = true;
		}

		
		logger.exiting(this.getClass().getSimpleName(), "login()");
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		int chapterIndex = startUrl.lastIndexOf('=') + 1;
		String baseUrl = startUrl.substring(0, chapterIndex) ;
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(GFConstants.VALUE_ATTR);
			cUrl = baseUrl + cUrl;
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
		return list;
	}

	private Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements forms = doc.getElementsByAttributeValue(GFConstants.NAME_ATTR, JUMP_ATTR);
		if (forms.size() > 0){
			Element form = forms.get(JUMP_FORM);
			options = form.getElementsByTag(GFConstants.OPTION_TAG);
		}
		return options;
	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(GFConstants.HREF_ATTR, VIEWUSER);
		
		String author = as.get(0).text();
		logger.info("author = " + author);
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements as = doc.getElementsByAttributeValueStarting(GFConstants.HREF_ATTR, VIEWSTORY);
		
		String title = as.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected void extractChapter(Document page,
			Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);
		
		Elements divs = page.getElementsByAttributeValue(GFConstants.ID_ATTR, STORY);
		Element div = divs.first();
		//div.removeAttr(HTMLConstants.ID_ATTR);
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		chap.setDoc(freshDoc);
//		loc.addChapter(chap);

		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
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
	Document getPage(String url) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getPage(String url)");

		String localUrl = url;
		Document doc = super.getPage(localUrl);
		if (ageConsentRequired(doc)){
			localUrl = localUrl + ageConsent(doc);
			doc = super.getPage(localUrl);
		}
		doc = recode(doc, localUrl);
		
		logger.exiting(this.getClass().getSimpleName(), "getPage(String url)");
		return doc;
	}

	@Override
	String ageConsent(Document doc) {
		Elements div = doc.getElementsByClass(ERRORTEXT);
		Elements as = div.get(0).getElementsByTag(GFConstants.A_TAG);
		Element a = as.first();
		String url = a.attr(GFConstants.HREF_ATTR);
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
