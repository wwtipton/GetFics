package com.notcomingsoon.getfics.sites;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.GFLogger;
import com.notcomingsoon.getfics.HTMLConstants;
import com.notcomingsoon.getfics.Story;

import okhttp3.Cookie;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



@SuppressWarnings("unchecked")
public abstract class Site {

	static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0";

	static final String FFN = "fanfiction.net";

	static final String FICTION_HUNT = "fictionhunt.com";

	static final String AFF = "adult-fanfiction.org";

	static final String TPP = "thepetulantpoetess.com";

	static final String DIGITAL_QUILL = "digital-quill.org";

	static final String SYCOPHANTEX = "sycophanthex.com";
	
	static final String TTH = "tthfanfic.org";
	
	static final String THE_MASQUE = "themasque.net";
	
	static final String AO3 = "archiveofourown.org";

	static final String MEDIA_MINER = "mediaminer.org";

	static final String WITCH_FICS = "witchfics.org";
	
	static final String HUNTING_HORCRUXES = "huntinghorcruxes";	

	static final String SSHG_EXCHANGE = "sshg-exchange";	

	static final String PIC = "image";

	private static final String JPEG = "jpg";
	
	private static final String PERIOD = ".";
	
	private static final String SLASH = "/";
	
	private static ArrayList<String> sites = new ArrayList<String>();
	static{
		sites.add(AFF);
		sites.add(DIGITAL_QUILL);
		sites.add(FFN);
		sites.add(SYCOPHANTEX);
		sites.add(TPP);
		sites.add(TTH);
		sites.add(THE_MASQUE);
		sites.add(AO3);
		sites.add(MEDIA_MINER);
		sites.add(FICTION_HUNT);
		sites.add(WITCH_FICS);
		sites.add(HUNTING_HORCRUXES);
		sites.add(SSHG_EXCHANGE);
		Collections.sort(sites, new SiteNameComparator());
	}

	protected String startUrl;

	protected Logger logger = GFLogger.getLogger();
	
	protected Charset siteCharset = HTMLConstants.UTF_8;
	
//	protected Cookie[] cookies;

	private String siteName;

//	Connection conn;
	
	Boolean ignoreHttpErrors = false;

	static OkHttpClient client = null;
	static CookieManager cookieManager = new CookieManager();
	static JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);
	static{
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
		
	    client = new OkHttpClient.Builder()
	            .cookieJar(cookieJar)
	            .build();
	}

	
	protected static final String SUMMARY_STRING = "Summary";

	protected Document recode(Document doc, String url) {
		logger.entering(this.getClass().getCanonicalName(), "recode(Document doc, String url)");

		Document utfDoc = doc;
		//need to recode?
		if (!siteCharset.equals(HTMLConstants.UTF_8)){
		//	doc.outputSettings().charset(siteCharset);
			String unicode = doc.toString();

			byte[] b = unicode.getBytes(siteCharset);
			
			String site = new String(b, siteCharset);
			
			utfDoc = Jsoup.parse(site, url);	
			utfDoc.charset(siteCharset);
			logger.log(Level.ALL, this.getClass().getCanonicalName() + "recode(Document doc, String url) \tcharset:" + utfDoc.charset().displayName());
			
		}

		logger.exiting(this.getClass().getCanonicalName(), "recode(Document doc)");
		return utfDoc;
	}


	protected abstract ArrayList<Chapter> getChapterList(Document doc) throws Exception;

	protected abstract String getAuthor(Document doc);

	protected abstract String getTitle(Document doc);
	
	protected Chapter extractSummary(Document story, Document chapter) throws Exception {
		return null;
	}

	String ageConsent(Document doc) {
		return null;
	}

	boolean ageConsentRequired(Document doc) {
		return false;
	}

	protected abstract Document extractChapter(Document story, Document chapter,
			Chapter title);

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getPage(java.lang.String)
	 */
	Document getPage(String url) throws Exception {
		logger.entering(this.getClass().getCanonicalName(), "getPage(" + url + ")");
		
		logger.info(this.getClass().getCanonicalName() + "\tgetPage(" + url + ")");	
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    Request.Builder builder = getRequestBuilder(url);

	    Request request = builder.build();
	    
		Response response = client.newCall(request).execute();

		Document doc = Jsoup.parse(response.body().byteStream(), siteCharset.name(), url);
		
		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");
		return doc;
	}


	/**
	 * @param url
	 * @return
	 */
	Request.Builder getRequestBuilder(String url) {
		Request.Builder builder = new Request.Builder()
			   	.url(url);
		/*
			   	.timeout(Duration.ofSeconds(120))
			   	.setHeader("User-Agent", USER_AGENT)
			   	.setHeader("upgrade-insecure-requests", "1")
			   	*/
			//   	.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			/*
				.setHeader("accept-encoding", "gzip")
			   	.setHeader("sec-fetch-site", "none")
			   	.setHeader("sec-fetch-mode", "navigate")
			   	.setHeader("sec-fetch-dest", "document")
			   	.setHeader("sec-fetch-user", "?1")
			   	.GET();
			   	*/
		return builder;
	}
	
    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

	protected abstract boolean isOneShot(Document doc) throws Exception;

	public Site(String ficUrl) {
		super();
		this.startUrl = ficUrl;
	}

	public Story download() throws Exception {
		logger.entering(this.getClass().getCanonicalName(), "download()");
		logger.info("Sending request to URL:" + startUrl);	
		
		Document doc = getPage(startUrl);
	
		Story loc = Story.createStory(getAuthor(doc), getTitle(doc));
		loc.setCharset(this.siteCharset);
		
		Document story = initStory(loc.getOutputDir());
		
		
		if (isOneShot(doc)){
			loc.setOneShot(true);
			Chapter title = new Chapter(this.startUrl, loc.getOrigTitle());
			extractSummary(story, doc);
			extractChapter(story, doc, title);
		} else {
			ArrayList<Chapter> chapterList = getChapterList(doc);
			boolean firstChapter = true;
			
			Iterator<Chapter> cIter = chapterList.iterator();
			Chapter summary = extractSummary(story, doc);
			while (cIter.hasNext()){
				Chapter c = cIter.next();
				Document nextDoc;
				if (firstChapter && c.getUrl().contains(startUrl)){
					nextDoc = doc;
					firstChapter = false;
				} else {
					if (firstChapter){
						nextDoc = getPage(c.getUrl());
						firstChapter = false;
					} else {
						nextDoc = getPage(c.getUrl());
					}
				}
				extractChapter(story, nextDoc, c);
			}
			if (null != summary){
				chapterList.add(0, summary);
			}
			Chapter.writeContents(loc, chapterList, doc.outputSettings().charset());
		}
		
		getImages(story, loc);
		writeStory(story, loc);
	
		logger.exiting(this.getClass().getCanonicalName(), "download()");
		return loc;
	}

	private void getImages(Document story, Story loc) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getImages(Document story, Story loc)");
	
		Elements images = story.getElementsByTag(HTMLConstants.IMG_TAG);
		 
		logger.info("images.size = " + images.size());
		for (int i = 0; i < images.size(); i++){
			Element image = images.get(i);
			String src = image.attr(HTMLConstants.SRC_ATTR);
			if (!(src.contains(HTMLConstants.HTTP) || src.contains(HTMLConstants.HTTPS))){
				src= HTMLConstants.HTTP + this.siteName + SLASH + src;
			}
			int lastPeriod = src.lastIndexOf(PERIOD);
			String type = src.substring(lastPeriod+1);
			Iterator<ImageReader> ri = ImageIO.getImageReadersBySuffix(type);
			if (!ri.hasNext()){
				type = JPEG;
			}
			logger.info("href = " + src);
			try {
				URL source = new URL(src);
				BufferedImage pic = ImageIO.read(source);
				if (null == pic) {
					throw new Exception("Picture diddn't download!!!");
				} else {
					try {
						File outputFile = new File(loc.getOutputDir(), PIC + i
								+ PERIOD + type);
						image.attr(HTMLConstants.SRC_ATTR, outputFile.getPath());
						logger.info("outputFile = " + outputFile);
						ImageIO.write(pic, type, outputFile);
					} catch (Exception e) {
						image.remove();
					}
				}
			} catch (Exception e) {
				String pathname = image.attr(HTMLConstants.SRC_ATTR);
				int idx = pathname.lastIndexOf(SLASH) + 1;
				String name = pathname.substring(idx);
				image.attr(HTMLConstants.SRC_ATTR, name);
				loc.addImageFailure(pathname + "\t" + e);
			}
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getImages(Document story, Story loc)");
	}


	@SuppressWarnings("deprecation")
	private Document initStory(File dir) {
		logger.entering(this.getClass().getCanonicalName(), "initStory()");
		
		Document outDoc = new Document(dir.getName());
		outDoc.outputSettings().charset(siteCharset);
	
		Element html = outDoc.appendElement(HTMLConstants.HTML_TAG);
		Element head = html.appendElement(HTMLConstants.HEAD_TAG);
		Comment title = new Comment(" " + startUrl + " ");
		head.appendChild(title);
		html.appendElement(HTMLConstants.BODY_TAG);
		
		logger.exiting(this.getClass().getCanonicalName(), "initStory()");
		return outDoc;
	}

	private void writeStory(Document story, Story loc) throws Exception {
		logger.entering(this.getClass().getCanonicalName(), "writeStory(Document doc, File dir)");
		
		File dir = loc.getOutputDir();
		File f = new File(dir, loc.toString() + HTMLConstants.HTML_EXTENSION);
	
		logger.info("f: " + dir.getParent());	
		
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		logger.log(Level.ALL, this.getClass().getCanonicalName() + "gwriteStory(Document story, Story loc) \tcharset:" + story.charset().displayName());
		
		String content = story.html();		
		byte[] b = content.getBytes();
		
		fos.write(b);
		fos.close();
		logger.exiting(this.getClass().getCanonicalName(), "writeStory(Document doc, File dir)");
		
	}


	/**
	 * @param body modified by method
	 */
	protected void addChapterFooter(Element body) {
		body.appendElement(HTMLConstants.HR_TAG);
		body.appendElement(HTMLConstants.HR_TAG);
	}


	/**
	 * @param body modified by method
	 */
	protected void addNotesFooter(Element body) {
		Element p = new Element(HTMLConstants.P_TAG);
		p.attr("style", "text-align:center");
		p.appendText("~^~^~^~");
		
		body.appendChild(p);
	}


	/**
	 * @param story
	 * @param title
	 * @return
	 */
	protected Element addChapterHeader(Document story, Chapter title) {
		Element body = story.body();
		Element a = body.appendElement(HTMLConstants.A_TAG);
		a.attr(HTMLConstants.NAME_ATTR, title.getFileTitle());
		Element h2 =body.appendElement(HTMLConstants.H2_TAG);
		h2.text(title.getOrigTitle());
		return body;
	}
	
	public static Story getStory(String url) throws Exception{
		Story story = null;
		Site site = null;
		
		for (String s : sites){
			if (s.equals(AFF) && AdultFanFiction.isAFF(url)){
				site = new AdultFanFiction(url);
				site.siteName = AFF;
				break;
			}
			if (s.equals(DIGITAL_QUILL) && DigitalQuill.isDigitalQuill(url)){
				site = new DigitalQuill(url);
				site.siteName = DIGITAL_QUILL;
				break;			
			}
			if (s.equals(SYCOPHANTEX) && Sycophantex.isSycophantex(url)){
				site = new Sycophantex(url);
				site.siteName = SYCOPHANTEX;
				break;
			}			
			if (s.equals(FFN) && FanFictionNet.isFFN(url)){
				site = new FanFictionNet(url);
				site.siteName = FFN;
				break;
			}			
			if (s.equals(THE_MASQUE) && TheMasqueNet.isMasque(url)){
				site = new TheMasqueNet(url);
				site.siteName = THE_MASQUE;
				break;
			}		
			if (s.equals(TPP) && PetulantPoetess.isTPP(url)){
				site = new PetulantPoetess(url);
				site.siteName = TPP;
				break;
			}		
			if (s.equals(TTH) && TwistingTheHellmouth.isTTH(url)){
				site = new TwistingTheHellmouth(url);
				site.siteName = TTH;
				break;
			}		
			if (s.equals(AO3) && ArchiveOfOurOwn.isAO3(url)){
				site = new ArchiveOfOurOwn(url);
				site.siteName = AO3;
				break;
			}	
			if (s.equals(MEDIA_MINER) && MediaMiner.isMediaMiner(url)){
				site = new MediaMiner(url);
				site.siteName = MEDIA_MINER;
				break;
			}		
			if (s.equals(FICTION_HUNT) && FictionHunt.isFictionHunt(url)){
				site = new FictionHunt(url);
				site.siteName = FICTION_HUNT;
				break;
			}		
			// Defunct 		if (s.equals(FICTION_ALLEY) && FictionAlley.isFictionAlley(url)){
			// Defunct site = new FictionAlley(url);
			// Defunct site.siteName = FICTION_ALLEY;
			// Defunct break;
			// Defunct }
			if (s.equals(WITCH_FICS) && WitchFics.isWitchFics(url)){
				site = new WitchFics(url);
				site.siteName = WITCH_FICS;
				break;
			}		
			if (s.equals(HUNTING_HORCRUXES) && HuntingHorcruxes.isHuntingHorcruxes(url)){
				site = new HuntingHorcruxes(url);
				site.siteName = HUNTING_HORCRUXES;
				break;
			}		
			if (s.equals(SSHG_EXCHANGE) && SSHGExchange.isSSHGExchange(url)){
				site = new SSHGExchange(url);
				site.siteName = SSHG_EXCHANGE;
				break;
			}		
		}
		if (site != null){
			story = site.download();
		}
		return story;
	}

	protected static void addCookie(URI u, String key, String value) {
		HttpCookie c = new HttpCookie(key, value);
		
		c.setPath("/");
		c.setSecure(true);
		c.setHttpOnly(true);
		c.setVersion(0);
		cookieManager.getCookieStore().add(u, c);
	}


	void login() throws IOException, InterruptedException {
		//Intentionally left empty.
	}


		
	
}