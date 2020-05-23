/**
 * 
 */
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
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class HuntingHorcruxes extends Site {

	private static final Charset HH_CHARSET = HTMLConstants.ISO_8859_1;

	private static final int PRINT_BUTTON = 0;
	private static final int CHAPTER_BODY = 0;
	
	private  Cookie[] HH_COOKIES =  new Cookie[]{ new Cookie("catkey5_useruid", "1182")};

	
	private static final String CLASS = "class";
	private static final String CONTENT = "content";
	private static final String PAGETITLE = "pagetitle";
	private static final String ID = "id"; 
	private static final String BY = " by "; 

	private static final String PEN_NAME_KEY = "penname";
	
	private static final String PEN_NAME = "Ouatic-7";
	
	private static final String PASSWORD_KEY = "password";
	
	private static final String PASSWORD = "d6eath";
	
	private static final String LOGIN_URL = "http://www.huntinghorcruxes.themaplebookshelf.com/user.php?action=login";
	
	private static final String CONTINUE_TEXT = "Continue";

	Connection conn;
	private String warning4 = "&warning=4";
	private static final String ERRORTEXT = "errortext";
	private static final String VIEW_USER = "viewuser";
	private static final String VIEW_STORY = "viewstory";	
	
	/**
	 * @param ficUrl
	 */
	public HuntingHorcruxes(String ficUrl)  throws IOException {
		super(ficUrl);
		login();

		super.cookies = HH_COOKIES;
		siteCharset = HH_CHARSET;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();

		Elements options = getChapterOptions();
		
		int storyIndex = startUrl.indexOf(VIEW_STORY);
		String baseUrl = startUrl.substring(0, storyIndex);
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(HTMLConstants.HREF_ATTR);
			cUrl = baseUrl.concat(cUrl);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Elements es = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEW_USER);
		Element a = es.first();
		String author = a.text();
		
		logger.info("author = " + author);
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements es = doc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEW_STORY);
		Element s = es.first();
		String title = s.text();

		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);

		Elements tables = chapter.getElementsByAttributeValue(ID, CONTENT);
		Element table = tables.first();
		Elements tds = table.getElementsByTag(HTMLConstants.TD_TAG);
		Element td = tds.get(CHAPTER_BODY);
		
		body.appendChild(td);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected Chapter extractSummary(Document story, Document chapter)  {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
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
		
		Chapter title = null;
		if (null != summary ){
			title = new Chapter(this.startUrl, SUMMARY_STRING);
			Element body = addChapterHeader(story, title);
			body.appendText(summary);
			addChapterFooter(body);
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	/**
	 * Fetch table of contents page for story. It contains summary and list of chapters.
	 * 
	 * @return toc 
	 * @throws IOException
	 */
	private Document getTOCPage() throws IOException {
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
			options = toc.getElementsByAttributeValueStarting(HTMLConstants.HREF_ATTR, VIEW_STORY);
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
	void login() throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "login()");
		conn = Jsoup.connect(LOGIN_URL);
		conn.timeout(180000);
		conn.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0");
		Connection.Response resp = conn.execute();
		Document doc = resp.parse();
		Map<String, String> cookies = resp.cookies();
		
		//conn = Jsoup.connect(USER_SESSIONS);
	//	conn = Jsoup.connect(LOGIN_URL);
		conn.method(Connection.Method.POST);
		conn.cookies(cookies);
		conn.data(PEN_NAME_KEY, PEN_NAME);
		conn.data(PASSWORD_KEY, PASSWORD);
//		conn.data(REMEMBER_ME_KEY, "1");
	//	conn.data("utf8", "&#x2713;");
	//	conn.data(AUTHENTICITY_TOKEN, token);
		conn.data("submit", "Submit");
		Connection.Response resp2 = conn.execute();
		Document doc2 = resp2.parse();

		Set<String> keys = cookies.keySet();
		HH_COOKIES = new Cookie[keys.size()];
		
		int i = 0;
		for(String key : keys){
			HH_COOKIES[i] = new Cookie(key, cookies.get(key));
			i++;
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "login()");
	}

	@Override
	Document getPage(String url) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getPage(String url)");

		String localUrl = url;
		Document doc = super.getPage(localUrl);
		if (ageConsentRequired(doc)){
			localUrl = localUrl + warning4;
			doc = super.getPage(localUrl);
		}
		doc = recode(doc, localUrl);
		
		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");
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
