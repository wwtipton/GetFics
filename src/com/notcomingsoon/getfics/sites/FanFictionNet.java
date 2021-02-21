/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class FanFictionNet extends Site {
	
	private static final String A2A_KIT_A2A_DEFAULT_STYLE = "a2a_kit a2a_default_style";
	private static final Charset FFN_CHARSET = HTMLConstants.UTF_8;
	private static final int AUTHOR_ANCHOR = 0;
	private static final int CHAPTER_SELECT = 1;
	private static final String STORYTEXT = "storytext";
	private static final int CHAPTER_BODY = 0;
	private static final int SUMMARY = 6;
	
	private static  URI U = null;
	static{
		try {
			U = new URI("https://www.fanfiction.net");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		addCookie(U,"__gads","ID=1f75c52f56b20fc4-22e117f392c20095:T=1596330031:RT=1596330031:R:S=ALNI_MYIa9bthqfKNyxM_9UFnpqHaMdGxg");
	}
	
	/**
	 * @param ficUrl
	 * @throws IOException 
	 */
	public FanFictionNet(String ficUrl) throws IOException {
		super(ficUrl);
	//	super.cookieManager = FFN_COOKIES;
		siteCharset = FFN_CHARSET;
	//	login();
	//	ignoreHttpErrors = true;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		if (options != null){
			
			String urlSuffix = null;
			String urlPrefix = null;
			int slashIndex = startUrl.lastIndexOf(HTMLConstants.URL_DIVIDER);
			urlSuffix = startUrl.substring(slashIndex);
	
			slashIndex = startUrl.lastIndexOf(HTMLConstants.URL_DIVIDER, slashIndex-1);
			urlPrefix = startUrl.substring(0, slashIndex+1);
			logger.info("urlPrefix = " + urlPrefix); 
			logger.info("urlSuffix = " + urlSuffix); 
	
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(HTMLConstants.VALUE_ATTR);
				cUrl = urlPrefix + cUrl + urlSuffix;
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements selects = doc.getElementsByAttributeValue("title","chapter navigation");
		if (!selects.isEmpty()){
			Element select = selects.get(CHAPTER_SELECT);
			options = select.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Element div = doc.getElementById("profile_top");
		Elements as = div.getElementsByTag(HTMLConstants.A_TAG);
		
		String author = as.get(AUTHOR_ANCHOR).text();
		
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements bs = doc.getElementsByTag(HTMLConstants.B_TAG);
		String title = bs.get(5).text();
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
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.ID_ATTR, STORYTEXT);
		Element div = divs.get(CHAPTER_BODY);
		div.removeAttr(HTMLConstants.ID_ATTR);
		Elements subDivs = div.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, A2A_KIT_A2A_DEFAULT_STYLE);
		if (subDivs.size() > 0){
			Element subDiv = subDivs.first();
			subDiv.remove();
		}
		
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
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, "xcontrast_txt");
		Element div = divs.get(SUMMARY);
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (options == null){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	static public boolean isFFN(String url){
		boolean retVal = false;
		if (url.contains(FFN)){
			retVal = true;
		}
		
		return retVal;
	}
	 
	/*
	@Override
	Document getPage(String url) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getPage(" + url + ")");
		
		logger.info(this.getClass().getCanonicalName() + "\tgetPage(" + url + ")");
		
		Document doc = super.getPage(url);
		
		Map<String, String> cookieMap = conn.response().cookies();
		cookieMap.forEach((key, value) -> {
		    logger.info("Key : " + key + " Value : " + value);
		    Cookie c = new Cookie(key, value);
		    cookies[cookies.length] = c;
		});
	
	    logger.info("cookies : " + cookies);
		return doc;
	}


	private BiConsumer<? super String, ? super String> addCookie() {
		
		return null;
	}
		*/

	/*
	@Override
	void login() throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "login()");
		Connection conn = Jsoup.connect("https://www.fanfiction.net/login.php?cache=bust");
		conn.timeout(180000);
		conn.userAgent(USER_AGENT);
		Connection.Response resp = conn.execute();
		Document doc = resp.parse();
//		Elements elist = doc.getElementsByAttributeValue("name",AUTHENTICITY_TOKEN);
	//	String token = elist.last().attr("value");
		Map<String, String> cookies = resp.cookies();
		
		conn.method(Connection.Method.POST);
		conn.cookies(cookies);
	//	conn.data(PEN_NAME_KEY, PEN_NAME);
//		conn.data(PASSWORD_KEY, PASSWORD);
	//	conn.data(REMEMBER_ME_KEY, "1");
	//	conn.data(AUTHENTICITY_TOKEN, token);
//		conn.data("commit", "Log in");

		Connection.Response resp2 = null;
		try {
			resp2 = conn.execute();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		Document doc2 = resp2.parse();
		Map<String, String> cookies2 = resp2.cookies();
		Set<String> keys = cookies2.keySet();
//		AO3_COOKIES = new Cookie[keys.size()];
		
		int i = 0;
		for(String key : keys){
	//		AO3_COOKIES[i] = new Cookie(key, cookies2.get(key));
			i++;
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "login()");
	}
	*/


}
