package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.HTMLConstants;
public class SquidgeWorld extends Site {

	private static final String REMEMBER_ME_KEY = "user_session[remember_me]";

	private static final String AUTHENTICITY_TOKEN = "authenticity_token";

	private static final Charset AO3_CHARSET = HTMLConstants.UTF_8;
	
	private static final String AUTHOR = "byline heading";
	
	private static final int CHAPTER_SELECT = 0;

	private static final String URL_DIVIDER = "/";

	private static final String USERSTUFF = "userstuff";

	private static final String USERSTUFF_MODULE = "userstuff module";

	private static final String SUMMARY_MODULE = "summary module";

	private static final String WORK = "work";
	
	private static final String ADULT = "?view_adult=true";
	
	private static final String PEN_NAME_KEY = "user[login]";
	
	private static final String PEN_NAME = GFProperties.getPropertyValue(GFProperties.SQUIDGE_PEN_NAME);
	
	private static final String PASSWORD_KEY = "user[password]";
	
	private static final String PASSWORD = GFProperties.getPropertyValue(GFProperties.SQUIDGE_PASSWORD);

	private static final String LOGIN_URL = "https://SquidgeWorld.org/users/login";

	private static final String NOTES_MODULE = "notes module";

	private static final String END_NOTES_MODULE = "end notes module";

	private static final String TAG = "tag";
	
	boolean loggedIn = false;
	

	
	//Connection conn;

	public SquidgeWorld(String ficUrl) throws Exception {
		super(ficUrl);
		logger.entering(this.getClass().getSimpleName(), "SquidgeWorld(String ficUrl)");
		logger.finer("startUrl = " + startUrl);
		siteCharset = AO3_CHARSET;
		
		if (!loggedIn) {
			login();
		}
		logger.exiting(this.getClass().getSimpleName(), "SquidgeWorld(String ficUrl)");
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getChapterList(Document doc");
		
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
		
		logger.exiting(this.getClass().getSimpleName(), "getChapterList(Document doc");
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
		logger.entering(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		
		Elements h3s = doc.getElementsByClass(AUTHOR);
		Element a = h3s.first().firstElementChild();
		String author = a.text();
		
		logger.info("author = " + author);
		logger.exiting(this.getClass().getSimpleName(), "getAuthor(Document doc)");
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		Elements h2S = doc.getElementsByClass("title heading");
		
		String title = h2S.get(0).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getSimpleName(), "getTitle(Document doc)");
		return title;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		
		extractNotes(story, chapter, body);

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

		extractAfterNotes(story, chapter, body);

		addChapterFooter(body);
		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		boolean writeChapter = false;
		
		Chapter title = null;
		Element body = null;
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, SUMMARY_MODULE);
		Element div = divs.first();
		if (div != null){
			Element p = div.getElementsByTag(HTMLConstants.BLOCKQUOTE_TAG).first();
			if (p != null){
				title = new Chapter(this.startUrl, SUMMARY_STRING);
				body = addChapterHeader(story, title);
				body.appendChild(p);
				writeChapter = true;
			}
		}
	
		List<String> tagList = extractTags(story, chapter);
		if (null != tagList) {
			if (!writeChapter) {
				title = new Chapter(this.startUrl, TAGS_STRING);
				body = addChapterHeader(story, title);
			} else {
				addTagsHeader(body);
			}
			writeChapter = true;
			String textTags = tagList.toString();
			textTags = textTags.substring(1, textTags.length() - 1);
			body.appendText(textTags);
		}
		
		if (writeChapter) {
			addChapterFooter(body);
		}
		
		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return title;
	}

	protected List<String> extractTags(Document story, Document chapter) {
		Elements tags = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, TAG);
		
		List<String> tagList = null;
		
		if (!tags.isEmpty()) {
			tagList = tags.eachText();
		}
		
		return tagList;
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

	public static boolean isSquidgeWorld(String url) {
		boolean retVal = false;
		if (url.contains(SQUIDGE_WORLD)){
			retVal = true;
		}
		
		return retVal;
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
			formMap.put("commit", "Log in");

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

	/**
	 * 
	 * @param story
	 * @param chapter
	 * @param body modified by method
	 */
	void extractAfterNotes(Document story, Document chapter, Element body) {
		logger.entering(this.getClass().getSimpleName(), "extractAfterNotes");

		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, END_NOTES_MODULE);
		if (!divs.isEmpty()) {
			addNotesFooter(body);
			Element chapterText = divs.first();
			body.appendChild(chapterText);
		}
			
		logger.exiting(this.getClass().getSimpleName(), "extractAfterNotes");
	}

	/**
	 * 
	 * @param story
	 * @param chapter
	 * @param body modified by method
	 */
	void extractNotes(Document story, Document chapter, Element body) {
		logger.entering(this.getClass().getSimpleName(), "extractNotes");

		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, NOTES_MODULE);
		if (!divs.isEmpty()) {
			Element chapterText = divs.first();
			body.appendChild(chapterText);
			addNotesFooter(body);
		}
		
		logger.exiting(this.getClass().getSimpleName(), "extractNotes");
	}


	

}
