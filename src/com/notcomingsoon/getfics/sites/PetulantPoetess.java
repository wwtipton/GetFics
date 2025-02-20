/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.files.Chapter;

/**
 * @author Winifred Tipton
 *
 */
public class PetulantPoetess extends Site {

	private static final Charset TPP_CHARSET = GFConstants.UTF_8_CHARSET;
	
	private static final String PEN_NAME = GFProperties.getPropertyValue(GFProperties.TPP_USER_NAME);
	
	private static final String LOGIN_URL = "https://thepetulantpoetess.com/users/sign_in";

	private static final String AUTHENTICITY_TOKEN = "authenticity_token";

	private static final String PEN_NAME_KEY = "user[email]";
	
	private static final String PASSWORD_KEY = "user[password]";
	
	private static final String PASSWORD = GFProperties.getPropertyValue(GFProperties.TPP_PASSWORD);

	private static final String REMEMBER_ME_KEY = "user[remember_me]";
	private static final String STORIES = "stories";

	static boolean loggedIn = false;
	
	private static HexFormat HEX_FORMAT = HexFormat.of();
	private static String C296 = "C296";
	private static String NDASH = "–";
	private static byte[] C296_BYTES = HEX_FORMAT.parseHex(C296);
	private static byte[] NDASH_BYTES = NDASH.getBytes();
	
	static String fixBadChars(String text) {
//		byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
		byte[] textBytes = text.getBytes();
		ArrayList<Byte> newTextBytes = new ArrayList<Byte>();

		for (int i = 0; i < textBytes.length; i++) {
			byte b = textBytes[i];
			if (b == C296_BYTES[0]) {
				int j = i + 1;
				if (j < textBytes.length && C296_BYTES[1] == textBytes[j]) {
						newTextBytes.add(NDASH_BYTES[0]);
						newTextBytes.add(NDASH_BYTES[1]);
						newTextBytes.add(NDASH_BYTES[2]);
						i = j;
						continue;
				}
			}
			newTextBytes.add(b);
		}

		byte[] temp = new byte[newTextBytes.size()];
		for (int i = 0; i < newTextBytes.size(); i++) {
			temp[i] = newTextBytes.get(i).byteValue();
		}
		
//		String newText = new String(temp, StandardCharsets.UTF_8);
		String newText = new String(temp);
		return newText;

	}



	/**
	 * @param ficUrl
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public PetulantPoetess(String ficUrl) throws IOException, InterruptedException {
		super(ficUrl);
		siteCharset = TPP_CHARSET;
		
		if (!loggedIn) {
			login();
		}

	}

	@Override
	void login() throws IOException, InterruptedException {
		logger.entering(this.getClass().getSimpleName(), "login()");

		waitRandom();
		
		HttpRequest.Builder builder = getRequestBuilder(LOGIN_URL);

	    HttpRequest request = builder.build();
	    
		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

	    Document doc = parse(LOGIN_URL, response);

		Elements elist = doc.getElementsByAttributeValue("name",AUTHENTICITY_TOKEN);
		String token = elist.last().attr("value");

		if (request.uri().toString().equals(LOGIN_URL)) {

			waitRandom();

			Map<String, String> formMap = new HashMap<>();
			formMap.put(PEN_NAME_KEY, PEN_NAME);
			formMap.put(PASSWORD_KEY, PASSWORD);
			formMap.put(REMEMBER_ME_KEY, "1");
			formMap.put(AUTHENTICITY_TOKEN, token);
			formMap.put("commit", "Log In");

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


	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document page) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document page");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		Elements options = getChapterOptions(page);
		
		/*
		String startChapter = startUrl.substring(storyIndex);
		ListIterator<Element> lIter = options.listIterator();
		while (lIter.hasNext()){
		for(Element opt : options)
			Element option = lIter.next();
			String title = option.text().trim();
			String cUrl = option.attr(GFConstants.VALUE_ATTR);
			cUrl = startUrl.replace(startChapter, cUrl);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);
		}
		*/
		
		int storyIndex = startUrl.indexOf(STORIES) - 1;
		String startChapter = startUrl.substring(0, storyIndex);
		for (Element a: options) {
			String title = a.wholeText().trim();
			String cUrl = startChapter + a.attr(GFConstants.HREF_ATTR);
			Chapter c = new Chapter(cUrl, title);
			list.add(c);			
		}
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document page");
		return list;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document page) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements headers = page.getElementsByTag("header");
		Element header = headers.getFirst();
		Elements as = header.getElementsByTag(GFConstants.A_TAG);
		
		Element a = as.get(1);
		
		String author = a.text();
		
		logger.info("author = " + author);
		
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document page) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document page)");
		
		Elements headers = page.getElementsByTag("header");
		Element header = headers.getFirst();
	
		Elements as = header.getElementsByTag(GFConstants.A_TAG);
		Element a = as.getFirst();
		String title = a.text();

		logger.info("title = " + title);
		
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document page)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected void extractChapter(Document page, Chapter chap) throws UnsupportedEncodingException {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document page, Chapter chap)");

		Document freshDoc = initDocument();
		Element body = addChapterHeader(freshDoc, chap);
		
		Elements divs = page.getElementsByAttributeValue("id", "chapter-text-container");
		Element div = divs.getFirst();
		div.removeAttr("style");
		
		List<TextNode> textNodes = div.selectXpath("//text()", TextNode.class);
		for (TextNode tn : textNodes) {
			String newText = fixBadChars(tn.text());
			tn.text(newText);
		}
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		chap.setDoc(freshDoc);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document page, Chapter chap)");
	}

	@Override
	protected Chapter extractSummary(Document page) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Document freshDoc = initDocument();

		Chapter summary = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(freshDoc, summary);

		Elements divs = page.getElementsByClass("summary");
		Element div = divs.getFirst();
		Elements ps = div.getElementsByTag("p");
		Element p = ps.get(1);
		body.appendText(p.wholeText());
		
		ArrayList<String> tagList = extractTags(div);
		if (null != tagList) {
			loc.setTags(tagList);
			addTagsHeader(body);
			String textTags = tagList.toString();
			textTags = textTags.substring(1, textTags.length() - 1);
			body.appendText(textTags);
		}
		
		addChapterFooter(body);
		
		summary.setDoc(freshDoc);
		
		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return summary;
	}

	protected ArrayList<String> extractTags(Element div) {
		Elements tags = div.getElementsByTag(GFConstants.A_TAG);
		
		ArrayList<String> tagList = null;
		
		if (!tags.isEmpty()) {
			tagList = (ArrayList<String>) tags.eachText();
		}
		
//		tagList.removeLast();
		
		return tagList;
	}

//	private String searchAuthor(Tag storyTag, String baseUrl, String userRef, String storyRef) throws Exception {
//		Element summary = new Element(storyTag, "");
//		String summaryText = null;
//		String authorUrl = baseUrl + userRef;
//		while (summaryText == null && authorUrl != null){
//			try {
//				Document authorWorks = getPage(authorUrl);
//				Elements aList = authorWorks.getElementsContainingText(NEXT_LINK);
//				Element a = aList.last();
//				if (a != null){
//					authorUrl = baseUrl + a.attr(GFConstants.HREF_ATTR);
//				} else {
//					authorUrl = null;
//				}
//				aList = authorWorks.getElementsByAttributeValue(GFConstants.HREF_ATTR, storyRef);
//				if (!aList.isEmpty()){
//					Element table  = aList.first().parent().parent().parent().parent();
//					Elements trList = table.getElementsByTag(GFConstants.TR_TAG);
//					Element tr = trList.get(SUMMARY_ROW);
//					Elements eList = tr.getElementsContainingText(SUMMARY_STRING);
//					Element e = eList.get(SUMMARY_TEXT_NODE);
//					int childCnt = e.childNodeSize();
//					summaryText = e.childNode(childCnt - 1).toString();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return summaryText;
//	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document page) {
		Elements options = getChapterOptions(page);
		boolean isOneShot = false;
		if (options == null || options.size() == 1){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	/**
	 * 
	 * @param page
	 * @return options consists of <a> Elements
	 */
	protected Elements getChapterOptions(Document page) {
		Elements options = null;
		
		Elements divs = page.getElementsByClass("offcanvas-body px-0");
		Element div = divs.first();
		options = div.getElementsByTag(GFConstants.A_TAG);

		return options;
	}	

	static public boolean isTPP(String url){
		boolean retVal = false;
		if (url.contains(TPP)){
			retVal = true;
		}
		
		return retVal;
	}
	
//	/**
//	 * Fetch table of contents page for story. It contains summary and list of chapters.
//	 * 
//	 * @return toc 
//	 * @throws IOException
//	 */
//	private Document getTOCPage() throws Exception {
//		if (null == toc) {
//			int index = startUrl.lastIndexOf(SLASH);
//			index = startUrl.lastIndexOf(SLASH, index);
//			String tocUrl = startUrl.substring(0, index);
//			toc = getPage(tocUrl);
//		}
//		return toc;
//	}


}
