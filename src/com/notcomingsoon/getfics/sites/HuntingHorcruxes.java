/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.io.IOException;
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

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.files.Chapter;

/**
 * @author Winifred Tipton
 *
 */
public class HuntingHorcruxes extends Site {

	private static final Charset HH_CHARSET = GFConstants.ISO_8859_1;

	//private static final int PRINT_BUTTON = 0;
	private static final int CHAPTER_BODY = 0;
	
	private static final String CLASS = "class";
	private static final String CONTENT = "content";
	//private static final String PAGETITLE = "pagetitle";
	private static final String ID = "id"; 
	//private static final String BY = " by "; 

	private static final String PEN_NAME_KEY = "penname";
	
	private static final String PEN_NAME = GFProperties.getPropertyValue(GFProperties.HUNTING_HORCRUXES_PEN_NAME);
	
	private static final String PASSWORD_KEY = "password";
	
	private static final String PASSWORD = GFProperties.getPropertyValue(GFProperties.HUNTING_HORCRUXES_PASSWORD);
	
	private static final String LOGIN_URL = "http://www.huntinghorcruxes.themaplebookshelf.com/user.php?action=login";
	
	private static final String CONTINUE_TEXT = "Continue";

	Connection conn;
	private String warning4 = "&warning=4";
	private static final String ERRORTEXT = "errortext";
	private static final String VIEW_USER = "viewuser";
	private static final String VIEW_STORY = "viewstory";	
	
	private static boolean hhLoggedIn = false;
	
	static{
		try {
			URI U = new URI(HUNTING_HORCRUXES);
			addCookie(U,"catkey5_useruid","1182");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	
	/**
	 * @param ficUrl
	 */
	public HuntingHorcruxes(String ficUrl)  throws Exception {
		super(ficUrl);
		if (!hhLoggedIn) {
			login();
		}

		siteCharset = HH_CHARSET;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();

		Elements options = getChapterOptions();
		
		int storyIndex = startUrl.indexOf(VIEW_STORY);
		String baseUrl = startUrl.substring(0, storyIndex);
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(GFConstants.HREF_ATTR);
			cUrl = baseUrl.concat(cUrl);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
		return list;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements es = doc.getElementsByAttributeValueStarting(GFConstants.HREF_ATTR, VIEW_USER);
		Element a = es.first();
		String author = a.text();
		
		logger.info("author = " + author);
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements es = doc.getElementsByAttributeValueStarting(GFConstants.HREF_ATTR, VIEW_STORY);
		Element s = es.first();
		String title = s.text();

		logger.info("title = " + title);
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected Document extractChapter(Document page,
			Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);

		Elements tables = page.getElementsByAttributeValue(ID, CONTENT);
		Element table = tables.first();
		Elements tds = table.getElementsByTag(GFConstants.TD_TAG);
		Element td = tds.get(CHAPTER_BODY);
		
		body.appendChild(td);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		return freshDoc;
	}

	@Override
	protected Chapter extractSummary(Document page) throws UnsupportedEncodingException  {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Document freshDoc = initDocument();

		Document toc = null;
		String summary = null;
		try{
			toc = getTOCPage();
			Elements summaryDivs = toc.getElementsByAttributeValue(CLASS, CONTENT);
			Element summaryDiv = summaryDivs.first();
			summary = summaryDiv.text();
		}catch (Exception e){
			// purposefully left blank
		}
		
		Chapter newCh = null;
		if (null != summary ){
			newCh = new Chapter(this.startUrl, SUMMARY_STRING);
			Element body = addChapterHeader(freshDoc, newCh);
			body.appendText(summary);
			addChapterFooter(body);
		}
		
		newCh.setDoc(freshDoc);
		loc.addChapter(newCh);

		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return newCh;
	}

	/**
	 * Fetch table of contents page for story. It contains summary and list of chapters.
	 * 
	 * @return toc 
	 * @throws IOException
	 */
	private Document getTOCPage() throws Exception {
		String tocUrl = startUrl.replace("chapter", "index");
		Document toc = getPage(tocUrl);
		return toc;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions();
		boolean isOneShot = false;
		if (options != null && options.size() == 1){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	protected Elements getChapterOptions() {
		Elements options = null;
		Document toc = null;
		try {
			toc = getTOCPage();
			options = toc.getElementsByAttributeValueStarting(GFConstants.HREF_ATTR, VIEW_STORY);
			if (!options.isEmpty()){
				options.remove(0); // print button
				options.remove(0); //header
			}
		} catch (Exception e){
			// purposefully left empty
		}
		
		return options;
	}	

	static public boolean isHuntingHorcruxes(String url){
		boolean retVal = false;
		if (url.contains(HUNTING_HORCRUXES)){
			retVal = true;
		}
		
		return retVal;
	}

	@Override
	void login() throws Exception {
		logger.entering(this.getClass().getSimpleName(), "login()");

		waitRandom();
		
		Map<String, String> user = new HashMap<String, String>();
		user.put(PEN_NAME_KEY, PEN_NAME);
		user.put(PASSWORD_KEY, PASSWORD);
		user.put("submit", "Submit");
		
	    HttpRequest.Builder builder = getRequestBuilder(LOGIN_URL);
	    builder.POST(ofFormData(user));

	    HttpRequest request = builder.build();
	    
		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
		
		if (response.statusCode()==200) {
			hhLoggedIn = true;
		}
		
		logger.exiting(this.getClass().getSimpleName(), "login()");
	}

	@Override
	Document getPage(String url) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getPage(String url)");

		String localUrl = url;
		Document doc = super.getPage(localUrl);
		if (ageConsentRequired(doc)){
			localUrl = localUrl + warning4;
			doc = super.getPage(localUrl);
		}
		doc = recode(doc, localUrl);
		
		logger.exiting(this.getClass().getSimpleName(), "getPage(String url)");
		return doc;
	}

	@Override
	boolean ageConsentRequired(Document doc) {
		boolean isContinue = false;
		
		Elements div = doc.getElementsByClass(ERRORTEXT);
		if (null != div && div.size() > 0){
			String t = div.first().text();
			isContinue = CONTINUE_TEXT.equalsIgnoreCase(t);
		}
		
		return isContinue;
	}
}
