package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.files.Chapter;

public class TwistingTheHellmouth extends Site {

	private static final Charset TTH_CHARSET = GFConstants.UTF_8;

	/*
	static{
		try {
			URI U = new URI(TTH);
			addCookie(U,"login", "login: 4926|79643601359745401948558400011123414202389861891824|1");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	private static final String LOGIN_URL = "https://tthfanfic.org/login.php";
	
	private static final String PEN_NAME_KEY = "urealname";
	
	private static final String PEN_NAME = GFProperties.getPropertyValue(GFProperties.TTH_PEN_NAME);
	
	private static final String LOGIN_KEY = "loginsubmit";
	
	private static final String LOGIN_ACTION = "Login";
	
	private static final String PASSWORD = GFProperties.getPropertyValue(GFProperties.TTH_PASSWORD);

	private static final String CTKN_ID = "ctkn";

	private static final String PASSWORD_ID = "password";



	private static final int AUTHOR_TABLE = 1;

	private static final int AUTHOR_CELL = 1;

	private static final int CHAPTER_SELECT_FORM = 2;

	private static final String STORY = "/story";

	private static final String STORYINNERBODY = "storyinnerbody";

	private static final String STORY_SUMMARY = "storysummary formbody defaultcolors";

	private static final String SUMMARY_COLON = SUMMARY_STRING + ": ";
	
	boolean loggedIn = false;
	
	public TwistingTheHellmouth(String ficUrl) throws IOException, InterruptedException {
		super(ficUrl);
		siteCharset = TTH_CHARSET;
		
		
		if (!loggedIn) {
			login();
		}
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		if (options != null){
			int storyIndex = startUrl.toLowerCase().indexOf(STORY);
			String startChapter = startUrl.substring(storyIndex);
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(GFConstants.VALUE_ATTR);
				cUrl = startUrl.replace(startChapter, cUrl);
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}

		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
		return list;

	}

	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements tables = doc.getElementsByTag(GFConstants.TABLE_TAG);
		Element table = tables.get(AUTHOR_TABLE);
		Elements tds = table.getElementsByTag(GFConstants.TD_TAG);
		Element td = tds.get(AUTHOR_CELL);
		
		String author = td.text();
		
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements h2s = doc.getElementsByTag(GFConstants.H2_TAG);
		Element h2 = h2s.first();
		String title = h2.text();
		
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected Document extractChapter(Document page,
			Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);
		
		Elements divs = page.getElementsByAttributeValue(GFConstants.ID_ATTR, STORYINNERBODY);
		Element div = divs.first();
		div.removeAttr(GFConstants.ID_ATTR);
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		chap.setDoc(freshDoc);
		loc.addChapter(chap);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		return freshDoc;

	}

	@Override
	protected Chapter extractSummary(Document page) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Document summaryDoc = initDocument();

		Chapter chap = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(summaryDoc, chap);
		
		Elements divs = page.getElementsByAttributeValue(GFConstants.CLASS_ATTR, STORY_SUMMARY);
		Element div = divs.first();
		Elements ps = div.getElementsByTag(GFConstants.P_TAG);
		
		String summary = null;
		int idx = 0;
		while (null == summary && idx < ps.size()){
			Element p = ps.get(idx);
			String text = p.text();
			if (text.startsWith(SUMMARY_COLON)){
				summary = text.substring(SUMMARY_COLON.length());
				p.text(summary);
				body.appendChild(p);
			}
			idx++;
		}
		
		addChapterFooter(body);
		
		chap.setDoc(summaryDoc);
		loc.addChapter(chap);

		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return chap;
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
		
		Elements forms = doc.getElementsByTag(GFConstants.FORM_TAG);
		if (forms.size() > CHAPTER_SELECT_FORM){
			Element form = forms.get(CHAPTER_SELECT_FORM);
			options = form.getElementsByTag(GFConstants.OPTION_TAG);
		}
		return options;
	}

	@Override
	void login() throws IOException, InterruptedException {
		logger.entering(this.getClass().getSimpleName(), "login()");

		waitRandom();
		
		HttpRequest.Builder builder = getRequestBuilder(LOGIN_URL);

	    HttpRequest request = builder.build();
	    
		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

	    Document doc = parse(LOGIN_URL, response);

		if (request.uri().toString().equals(LOGIN_URL)) {

			Elements elist = doc.getElementsByAttributeValue("name", CTKN_ID);
			String ctkn = elist.last().attr("value");

			Element el = doc.getElementById(PASSWORD_ID);
			String passwordKey = el.attr("name");

			waitRandom();

			Map<String, String> formMap = new HashMap<>();
			formMap.put(PEN_NAME_KEY, PEN_NAME);
			formMap.put(passwordKey, PASSWORD);
			formMap.put(CTKN_ID, ctkn);
			formMap.put(LOGIN_KEY, LOGIN_ACTION);

			builder.POST(ofFormData(formMap));
			
			HttpRequest request2 = builder.build();

			try {
				HttpResponse<InputStream> resp2 = client.send(request2, BodyHandlers.ofInputStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		loggedIn = true;
		
		logger.exiting(this.getClass().getSimpleName(), "login()");
	}


}
