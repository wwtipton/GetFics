package com.notcomingsoon.getfics.sites;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.brotli.dec.BrotliInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.GFLogger;
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.HTMLConstants;
import com.notcomingsoon.getfics.Story;

@SuppressWarnings("unchecked")
public abstract class Site {

	static final String USER_AGENT = GFProperties.getString(GFProperties.USER_AGENT);

	static final String FFN = "fanfiction.net"; //$NON-NLS-1$

	static final String FICTION_HUNT = "fictionhunt.com"; //$NON-NLS-1$

	static final String AFF = "https://adult-fanfiction.org/"; //$NON-NLS-1$

	static final String TPP = "http://www.thepetulantpoetess.com/"; //$NON-NLS-1$

	static final String DIGITAL_QUILL = "digital-quill.org"; //$NON-NLS-1$

	static final String SYCOPHANTEX = "http://ashwinder.sycophanthex.com/"; //$NON-NLS-1$

	static final String TTH = "https://www.tthfanfic.org/"; //$NON-NLS-1$

	static final String THE_MASQUE = "https://www.themasque.net"; //$NON-NLS-1$

	static final String AO3 = "archiveofourown.org"; //$NON-NLS-1$

	static final String MEDIA_MINER = "mediaminer.org"; //$NON-NLS-1$

	static final String WITCH_FICS = "witchfics.org"; //$NON-NLS-1$

	static final String HUNTING_HORCRUXES = "http://huntinghorcruxes.themaplebookshelf.com/"; //$NON-NLS-1$

	static final String SSHG_EXCHANGE = "sshg-exchange"; //$NON-NLS-1$

	static final String LJ = "https://www.livejournal.com/";

	static final String PIC = "image"; //$NON-NLS-1$

	private static final String JPEG = "jpg"; //$NON-NLS-1$

	private static final String PERIOD = "."; //$NON-NLS-1$

	static final String SLASH = "/"; //$NON-NLS-1$

	private static Random random = new Random();

	private static ArrayList<String> sites = new ArrayList<String>();
	static {
		sites.add(AFF);
		sites.add(DIGITAL_QUILL);
		// disabled for now sites.add(FFN);
		sites.add(SYCOPHANTEX);
		// disabled for now sites.add(TPP);
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

	private String siteName;

	Boolean ignoreHttpErrors = false;

	static CookieManager cookieManager = new CookieManager();
	static HttpClient client = null;
	static {
		CookieHandler.setDefault(cookieManager);
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

		client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).connectTimeout(Duration.ofSeconds(120))
				.cookieHandler(CookieHandler.getDefault()).build();
	}

	protected static final String SUMMARY_STRING = GFProperties.getString(GFProperties.SUMMARY); // $NON-NLS-1$

	protected Document recode(Document doc, String url) {
		logger.entering(this.getClass().getSimpleName(), "recode(Document doc, String url)"); //$NON-NLS-1$

		Document utfDoc = doc;
		// need to recode?
		if (!siteCharset.equals(HTMLConstants.UTF_8)) {
			// doc.outputSettings().charset(siteCharset);
			String unicode = doc.toString();

			byte[] b = unicode.getBytes(siteCharset);

			String site = new String(b, siteCharset);

			utfDoc = Jsoup.parse(site, url);
			utfDoc.charset(siteCharset);
			logger.log(Level.ALL, this.getClass().getSimpleName() + "recode(Document doc, String url) \tcharset:" //$NON-NLS-1$
					+ utfDoc.charset().displayName());

		}

		logger.exiting(this.getClass().getSimpleName(), "recode(Document doc)"); //$NON-NLS-1$
		return utfDoc;
	}

	protected abstract ArrayList<Chapter> getChapterList(Document doc) throws Exception;

	protected abstract String getAuthor(Document doc) throws Exception;

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

	protected abstract Document extractChapter(Document story, Document chapter, Chapter title);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.notcomingsoon.getfics.sites.Site#getPage(java.lang.String)
	 */
	Document getPage(String url) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getPage", url); //$NON-NLS-1$ //$NON-NLS-2$

		waitRandom();

		HttpRequest.Builder builder = getRequestBuilder(url);

		HttpRequest request = builder.build();

		HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

		logger.info("Status code: " + response.statusCode()); //$NON-NLS-1$

		Document doc = parse(url, response);

		// logger.info(doc.wholeText());

		logger.exiting(this.getClass().getSimpleName(), ".getPage", url); //$NON-NLS-1$
		return doc;
	}

	/**
	 * @param url
	 * @param response
	 * @return
	 * @throws IOException
	 */
	Document parse(String url, HttpResponse<InputStream> response) throws IOException {
		InputStream is = decompress(response);

		Document doc = Jsoup.parse(is, siteCharset.name(), url);
		return doc;
	}

	/**
	 * @param response
	 * @return
	 * @throws IOException
	 */
	static InputStream decompress(HttpResponse<InputStream> response) throws IOException {
		String encoding = response.headers().firstValue("Content-Encoding").orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
		InputStream is = null;
		System.out.println("Encoding:\t" + encoding);
		if (encoding.equals("gzip")) { //$NON-NLS-1$
			is = new GZIPInputStream(response.body());
		} else if (encoding.equals("br")) { //$NON-NLS-1$
			is = new BrotliInputStream(response.body());
		} else if (encoding.equals("deflate")) {
			is = new InflaterInputStream(response.body());
		} else {
			is = response.body();
		}
		return is;
	}

	/**
	 * @param url
	 * @return
	 */
	HttpRequest.Builder getRequestBuilder(String url) {
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(120))
				.setHeader("User-Agent", USER_AGENT) //$NON-NLS-1$
				.setHeader("upgrade-insecure-requests", "1") //$NON-NLS-1$ //$NON-NLS-2$
				.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8") // Firefox //$NON-NLS-1$ //$NON-NLS-2$
				.setHeader("accept-language", "en-US,en;q=0.5") //$NON-NLS-1$ //$NON-NLS-2$
				.setHeader("accept-encoding", "gzip, deflate, br") //$NON-NLS-1$ //$NON-NLS-2$
				.GET();
		return builder;
	}

	// Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
	HttpRequest.BodyPublisher ofFormData(Map<String, String> data) {
		var builder = new StringBuilder();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (builder.length() > 0) {
				builder.append("&"); //$NON-NLS-1$
			}
			builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
			builder.append("="); //$NON-NLS-1$
			builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
		}
		return HttpRequest.BodyPublishers.ofString(builder.toString());
	}

	protected abstract boolean isOneShot(Document doc) throws Exception;

	public Site(String ficUrl) {
		super();
		this.startUrl = ficUrl;
	}

	public Story download() throws Exception {
		logger.entering(this.getClass().getSimpleName(), "download()"); //$NON-NLS-1$
		logger.info("Sending request to URL:" + startUrl); //$NON-NLS-1$

		Document doc = getPage(startUrl);

		Story loc = Story.createStory(getAuthor(doc), getTitle(doc));
		loc.setCharset(this.siteCharset);

		Document story = initStory(loc.getOutputDir());

		if (isOneShot(doc)) {
			loc.setOneShot(true);
			Chapter title = new Chapter(this.startUrl, loc.getOrigTitle());
			extractSummary(story, doc);
			extractChapter(story, doc, title);
		} else {
			ArrayList<Chapter> chapterList = getChapterList(doc);
			boolean firstChapter = true;

			Iterator<Chapter> cIter = chapterList.iterator();
			Chapter summary = extractSummary(story, doc);
			while (cIter.hasNext()) {
				Chapter c = cIter.next();
				Document nextDoc;
				if (firstChapter && c.getUrl().contains(startUrl)) {
					nextDoc = doc;
					firstChapter = false;
				} else {
					if (firstChapter) {
						nextDoc = getPage(c.getUrl());
						firstChapter = false;
					} else {
						nextDoc = getPage(c.getUrl());
					}
				}
				extractChapter(story, nextDoc, c);
			}
			if (null != summary) {
				chapterList.add(0, summary);
			}
			Chapter.writeContents(loc, chapterList, doc.outputSettings().charset());
		}

		getImages(story, loc);
		writeStory(story, loc);

		logger.exiting(this.getClass().getSimpleName(), "download()"); //$NON-NLS-1$
		return loc;
	}

	private void getImages(Document story, Story loc) throws IOException {
		logger.entering(this.getClass().getSimpleName(), "getImages(Document story, Story loc)"); //$NON-NLS-1$

		Elements images = story.getElementsByTag(HTMLConstants.IMG_TAG);

		logger.info("images.size = " + images.size()); //$NON-NLS-1$
		for (int i = 0; i < images.size(); i++) {
			Element image = images.get(i);
			String src = image.attr(HTMLConstants.SRC_ATTR);
			if (!(src.contains(HTMLConstants.HTTP) || src.contains(HTMLConstants.HTTPS))) {
				src = HTMLConstants.HTTP + this.siteName + SLASH + src;
			}
			int lastPeriod = src.lastIndexOf(PERIOD);
			String type = src.substring(lastPeriod + 1);
			Iterator<ImageReader> ri = ImageIO.getImageReadersBySuffix(type);
			if (!ri.hasNext()) {
				type = JPEG;
			}
			logger.info("href = " + src); //$NON-NLS-1$
			logger.info("type = " + type); //$NON-NLS-1$

			waitRandom();

			HttpRequest.Builder builder = getRequestBuilder(src);

			HttpRequest request = builder.build();

			try {
				HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
				InputStream is = decompress(response);
				MemoryCacheImageInputStream iis = new MemoryCacheImageInputStream(is);
				BufferedImage pic = ImageIO.read(iis);
				if (null == pic) {
					throw new Exception("Picture didn't download!!!"); //$NON-NLS-1$
				} else {
					try {
						File outputFile = new File(loc.getOutputDir(), PIC + i + PERIOD + type);
						image.attr(HTMLConstants.SRC_ATTR, outputFile.getPath());
						logger.info("outputFile = " + outputFile); //$NON-NLS-1$
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
				loc.addImageFailure(pathname + "\t" + e); //$NON-NLS-1$
			}

		}

		logger.exiting(this.getClass().getSimpleName(), "getImages(Document story, Story loc)"); //$NON-NLS-1$
	}

	private Document initStory(File dir) {
		logger.entering(this.getClass().getSimpleName(), "initStory()"); //$NON-NLS-1$

		Document outDoc = new Document(dir.getName());
		outDoc.outputSettings().charset(siteCharset);

		Element html = outDoc.appendElement(HTMLConstants.HTML_TAG);
		Element head = html.appendElement(HTMLConstants.HEAD_TAG);
		Comment title = new Comment(" " + startUrl + " "); //$NON-NLS-1$ //$NON-NLS-2$
		head.appendChild(title);
		html.appendElement(HTMLConstants.BODY_TAG);

		logger.exiting(this.getClass().getSimpleName(), "initStory()"); //$NON-NLS-1$
		return outDoc;
	}

	private void writeStory(Document story, Story loc) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "writeStory(Document doc, File dir)"); //$NON-NLS-1$

		File dir = loc.getOutputDir();
		File f = new File(dir, loc.toString() + HTMLConstants.HTML_EXTENSION);

		logger.info("f: " + dir.getParent()); //$NON-NLS-1$

		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		logger.log(Level.ALL, this.getClass().getSimpleName() + "gwriteStory(Document story, Story loc) \tcharset:" //$NON-NLS-1$
				+ story.charset().displayName());

		String content = story.html();
		byte[] b = content.getBytes();

		fos.write(b);
		fos.close();
		logger.exiting(this.getClass().getSimpleName(), "writeStory(Document doc, File dir)"); //$NON-NLS-1$

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
		p.attr("style", "text-align:center"); //$NON-NLS-1$ //$NON-NLS-2$
		p.appendText(GFProperties.getString(GFProperties.NOTES_FOOTER)); // $NON-NLS-1$

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
		Element h2 = body.appendElement(HTMLConstants.H2_TAG);
		h2.text(title.getOrigTitle());
		return body;
	}

	public static Story getStory(String url) throws Exception {
		Story story = null;
		Site site = null;

		for (String s : sites) {
			if (s.equals(AFF) && AdultFanFiction.isAFF(url)) {
				site = new AdultFanFiction(url);
				site.siteName = AFF;
				break;
			}
			if (s.equals(SYCOPHANTEX) && Sycophantex.isSycophantex(url)) {
				site = new Sycophantex(url);
				site.siteName = SYCOPHANTEX;
				break;
			}
			if (s.equals(FFN) && FanFictionNet.isFFN(url)) {
				site = new FanFictionNet(url);
				site.siteName = FFN;
				break;
			}
			if (s.equals(THE_MASQUE) && TheMasqueNet.isMasque(url)) {
				site = new TheMasqueNet(url);
				site.siteName = THE_MASQUE;
				break;
			}
			if (s.equals(TPP) && PetulantPoetess.isTPP(url)) {
				site = new PetulantPoetess(url);
				site.siteName = TPP;
				break;
			}
			if (s.equals(TTH) && TwistingTheHellmouth.isTTH(url)) {
				site = new TwistingTheHellmouth(url);
				site.siteName = TTH;
				break;
			}
			if (s.equals(AO3) && ArchiveOfOurOwn.isAO3(url)) {
				site = new ArchiveOfOurOwn(url);
				site.siteName = AO3;
				break;
			}
			if (s.equals(MEDIA_MINER) && MediaMiner.isMediaMiner(url)) {
				site = new MediaMiner(url);
				site.siteName = MEDIA_MINER;
				break;
			}
			if (s.equals(FICTION_HUNT) && FictionHunt.isFictionHunt(url)) {
				site = new FictionHunt(url);
				site.siteName = FICTION_HUNT;
				break;
			}
			if (s.equals(WITCH_FICS) && WitchFics.isWitchFics(url)) {
				site = new WitchFics(url);
				site.siteName = WITCH_FICS;
				break;
			}
			if (s.equals(HUNTING_HORCRUXES) && HuntingHorcruxes.isHuntingHorcruxes(url)) {
				site = new HuntingHorcruxes(url);
				site.siteName = HUNTING_HORCRUXES;
				break;
			}
			if (s.equals(SSHG_EXCHANGE) && SSHGExchange.isSSHGExchange(url)) {
				site = new SSHGExchange(url);
				site.siteName = SSHG_EXCHANGE;
				break;
			}
		}
		if (site != null) {
			story = site.download();
		}
		return story;
	}

	protected static void addCookie(URI u, String key, String value) {
		HttpCookie c = new HttpCookie(key, value);

		c.setPath("/"); //$NON-NLS-1$
		c.setVersion(0);

		cookieManager.getCookieStore().add(u, c);
	}

	void login() throws Exception {
		// Intentionally left empty.
	}

	static void waitRandom() {

		int ms = 5000 + random.nextInt(10000); // random between 5000 and 15000

		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}