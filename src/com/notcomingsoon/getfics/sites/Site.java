package com.notcomingsoon.getfics.sites;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpHeaders;
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
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;

import org.brotli.dec.BrotliInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.GFLogger;
import com.notcomingsoon.getfics.GFProperties;
import com.notcomingsoon.getfics.files.Chapter;
import com.notcomingsoon.getfics.files.Epub;
import com.notcomingsoon.getfics.files.EpubFiles;

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

	static final String SQUIDGE_WORLD = "squidgeworld.org"; //$NON-NLS-1$

	static final String MEDIA_MINER = "mediaminer.org"; //$NON-NLS-1$

	static final String WITCH_FICS = "witchfics.org"; //$NON-NLS-1$

	static final String HUNTING_HORCRUXES = "http://huntinghorcruxes.themaplebookshelf.com/"; //$NON-NLS-1$

	static final String SSHG_EXCHANGE = "sshg-exchange"; //$NON-NLS-1$

	static final String LJ = "https://www.livejournal.com/";

	static final String PIC = "image"; //$NON-NLS-1$

	private static final String PERIOD = "."; //$NON-NLS-1$

	static final String SLASH = "/"; //$NON-NLS-1$

	private static Random random = new Random();

	private static ArrayList<String> sites = new ArrayList<String>();
	static {
		sites.add(AFF);
		sites.add(DIGITAL_QUILL);
		// disabled for now sites.add(FFN);
		sites.add(SYCOPHANTEX);
		sites.add(TPP);
		sites.add(TTH);
		sites.add(THE_MASQUE);
		sites.add(AO3);
		sites.add(SQUIDGE_WORLD);
		sites.add(MEDIA_MINER);
		sites.add(FICTION_HUNT);
		sites.add(WITCH_FICS);
		sites.add(HUNTING_HORCRUXES);
		sites.add(SSHG_EXCHANGE);
		Collections.sort(sites, new SiteNameComparator());
	}

	protected String startUrl;

	protected Logger logger = GFLogger.getLogger();

	protected Charset siteCharset = GFConstants.UTF_8;

	private String siteName;

	Boolean ignoreHttpErrors = false;
	
	Epub loc = null;

	static CookieManager cookieManager = new CookieManager();
	static HttpClient client = null;
	static {
		CookieHandler.setDefault(cookieManager);
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

		client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).connectTimeout(Duration.ofSeconds(120))
				.cookieHandler(CookieHandler.getDefault()).build();
	}

	protected static final String SUMMARY_STRING = GFProperties.getString(GFProperties.SUMMARY); // $NON-NLS-1$

	protected static final String TAGS_STRING = GFProperties.getString(GFProperties.TAGS); // $NON-NLS-1$

	protected Document recode(Document doc, String url) {
		logger.entering(this.getClass().getSimpleName(), "recode(Document doc, String url)"); //$NON-NLS-1$

		Document utfDoc = doc;
		// need to recode?
		if (!siteCharset.equals(GFConstants.UTF_8)) {
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

	protected Chapter extractSummary(Document chapterDoc) throws Exception {
		return null;
	}

	String ageConsent(Document doc) {
		return null;
	}

	boolean ageConsentRequired(Document doc) {
		return false;
	}

	protected abstract void extractChapter(Document chapter, Chapter chap) throws UnsupportedEncodingException;

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
		StringBuilder builder = new StringBuilder();
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

	public Epub download() throws Exception {
		logger.entering(this.getClass().getSimpleName(), "download()"); //$NON-NLS-1$
		logger.info("Sending request to URL:" + startUrl); //$NON-NLS-1$

		Document page = getPage(startUrl);

		loc = Epub.createEpub(getAuthor(page), getTitle(page));
		loc.setStartUrl(startUrl);
		loc.setCharset(this.siteCharset);

		if (isOneShot(page)) {
			loc.setOneShot(true);
			Chapter oneShot = new Chapter(startUrl, loc.getOrigTitle());
			Chapter summary = extractSummary(page);
			if (null != summary) {
				loc.addChapter(summary);
			}
			extractChapter(page, oneShot);
			loc.addChapter(oneShot);
		} else {
			ArrayList<Chapter> chapterList = getChapterList(page);
			boolean firstChapter = true;

			Chapter summary = extractSummary(page);
			if (null != summary) {
				loc.addChapter(summary);
			}
			
			//Iterator<Chapter> cIter = chapterList.iterator();
			ArrayList<Chapter> newChapterList = findNewChapters(chapterList);
			Iterator<Chapter> cIter = newChapterList.iterator();
			
			while (cIter.hasNext()) {
				Chapter c = cIter.next();
				Document nextDoc;
				if (firstChapter && c.getUrl().contains(startUrl)) {
					nextDoc = page;
					firstChapter = false;
				} else {
					if (firstChapter) {
						nextDoc = getPage(c.getUrl());
						firstChapter = false;
					} else {
						nextDoc = getPage(c.getUrl());
					}
				}
				extractChapter(nextDoc, c);
				loc.addChapter(c);
			}
		}

		getImages();

		logger.exiting(this.getClass().getSimpleName(), "download()"); //$NON-NLS-1$
		return loc;
	}

	private ArrayList<Chapter> findNewChapters(ArrayList<Chapter> chapterList) throws UnsupportedEncodingException {
		for (Chapter c : chapterList) {
			boolean isExists = loc.doesChapterFileExist(c.getFilename());
			if (isExists) {
				c.setDoesChapterExist(isExists);
				loc.addChapter(c);
			} else {
				loc.addNewChapter(c);
			}
		}
		return loc.getNewChapters();
	}

	private void getImages() throws IOException {
		logger.entering(this.getClass().getSimpleName(), "getImages(Document story, Story loc)"); //$NON-NLS-1$

		ArrayList<Chapter> chapterList = loc.getNewChapters();
		
		Iterator<Chapter> cIter = chapterList.iterator();
		
		Elements images = new Elements();
		
		while (cIter.hasNext()) {
			Chapter c = cIter.next();
			Document story = c.getDoc();
			Elements els = story.getElementsByTag(GFConstants.IMG_TAG);
			
			for (int e = 0; e < els.size(); e++) {
				images.add(els.get(e));
			}
		}
		
		logger.info("images.size = " + images.size()); //$NON-NLS-1$
		for (int i = 0; i < images.size(); i++) {
			Element image = images.get(i);

			waitRandom();

			Thread ri = new Thread(new ReadImage(image, loc, i));
			ri.start();

			int seconds = 0;
			while (ri.isAlive()) {
				if (seconds >= 60000) {
					logger.info("Attempting interrupt. seconds = " + seconds); //$NON-NLS-1$
					try {
						ri.interrupt();
						wait1();
					} catch (Exception e) {
						String pathname = image.attr(GFConstants.SRC_ATTR);
						int idx = pathname.lastIndexOf(SLASH) + 1;
						String name = pathname.substring(idx);
						image.attr(GFConstants.SRC_ATTR, name);
						loc.addImageFailure(pathname + "\t" + e); //$NON-NLS-1$
					}
				} else {
					wait1();
					seconds++;
			//		logger.info("seconds = " + seconds); //$NON-NLS-1$
				}
			}
		}
		

		logger.exiting(this.getClass().getSimpleName(), "getImages(Document story, Story loc)"); //$NON-NLS-1$
	}

	Document initDocument() {
		logger.entering(this.getClass().getSimpleName(), "initDocument()"); //$NON-NLS-1$

		Document outDoc = new Document(getEpubDirName());
		Element html = outDoc.appendElement(GFConstants.HTML_TAG);
		outDoc = EpubFiles.setOutputType(outDoc);
		Element head = html.appendElement(GFConstants.HEAD_TAG);
		Comment title = new Comment(" " + startUrl + " "); //$NON-NLS-1$ //$NON-NLS-2$
		head.appendChild(title);
		html.appendElement(GFConstants.BODY_TAG);

		logger.exiting(this.getClass().getSimpleName(), "initDocument()"); //$NON-NLS-1$
		return outDoc;
	}

	private String getEpubDirName() {
		return loc.getEpubDir().getName();
	}

	/**
	 * @param body modified by method
	 */
	protected void addChapterFooter(Element body) {
		body.appendElement(GFConstants.HR_TAG);
		body.appendElement(GFConstants.HR_TAG);
	}

	/**
	 * @param body modified by method
	 */
	protected void addNotesFooter(Element body) {
		Element p = new Element(GFConstants.P_TAG);
		p.attr("style", "text-align:center"); //$NON-NLS-1$ //$NON-NLS-2$
		p.appendText(GFProperties.getString(GFProperties.NOTES_FOOTER)); // $NON-NLS-1$

		body.appendChild(p);
	}

	/**
	 * @param freshDoc
	 * @param chap
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	protected Element addChapterHeader(Document freshDoc, Chapter chap) throws UnsupportedEncodingException {
		Element body = freshDoc.body();
		Element h2 = body.appendElement(GFConstants.H2_TAG);
		h2.text(chap.getName());
		return body;
	}

	/**
	 * @param story
	 * @param title
	 * @return nothing but alters parameter
	 */
	protected void addTagsHeader(Element body) {
		Element h3 = body.appendElement(GFConstants.H3_TAG);
		h3.text(TAGS_STRING);
	}

	public static Epub getEpub(String url) throws Exception {
		Epub story = null;
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
			if (s.equals(SQUIDGE_WORLD) && SquidgeWorld.isSquidgeWorld(url)) {
				site = new SquidgeWorld(url);
				site.siteName = SQUIDGE_WORLD;
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

	static void wait1() {

		int ms = 1000; // random between 5000 and 15000

		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class ReadImage implements Runnable {

		Element image;
		Epub loc;
		int i;
		

		ReadImage(Element image, Epub loc, int i) {
			this.image = image;
			this.loc = loc;
			this.i = i;
		}

		@Override
		public void run() {
			String src = image.attr(GFConstants.SRC_ATTR);
			String origSrc = src;
			if (!(src.contains(GFConstants.HTTP) || src.contains(GFConstants.HTTPS))) {
				src = GFConstants.HTTP + siteName + SLASH + src;
			}
			int lastPeriod = src.lastIndexOf(PERIOD);
			int lastSlash = src.lastIndexOf('/');
			String type = src.substring(lastPeriod + 1);
			String imgName = src.substring(lastSlash + 1, lastPeriod);

			logger.info("href = " + src); //$NON-NLS-1$
			logger.info("type = " + type); //$NON-NLS-1$
			logger.info("name = " + imgName); //$NON-NLS-1$
			
			String existingFileName = wasPicDownloaded(imgName, type);
			if (null != existingFileName) {
				image.attr(GFConstants.SRC_ATTR, existingFileName);
				loc.addImage(existingFileName);
				return;
			}
			
			HttpRequest.Builder builder = getRequestBuilder(src);
			HttpRequest request = builder.build();

			try {
				HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
				logger.info("Status code:\t" + response.statusCode());
				HttpHeaders headers = response.headers();
				Optional<String> ct = headers.firstValue("Content-Type");
				boolean isImage = false;
				if (ct.isPresent()) {
					String contentType = ct.get();
					logger.info("contentType:\t" + contentType);
					if (contentType.contains("image")) {
						isImage = true;
						int slash = contentType.lastIndexOf('/');
						type = contentType.substring(slash+1);
					}
				}

				// Did we get a picture or a page?
				if (response.statusCode() == 200 && !isImage) {
					// gotta a web page so try again
					Document doc = parse(src, response);
					src = findImage(doc, imgName);
					if (origSrc.equals(src)) {
						Exception e = new Exception("Second address same as starting address!!!");
						e.fillInStackTrace();
						throw e; //$NON-NLS-1$																			
					}
					logger.info("href = " + src); //$NON-NLS-1$
					if (null == src) {
						Exception e = new Exception("No image in second document!!!");
						e.fillInStackTrace();
						throw e; //$NON-NLS-1$													
					}
					logger.info("Src2:\t" + src);
					HttpRequest.Builder builder2 = getRequestBuilder(src);
					HttpRequest request2 = builder2.build();
					HttpResponse<InputStream> response2 = client.send(request2, BodyHandlers.ofInputStream());
					logger.info("Status code2:\t" + response2.statusCode());
					HttpHeaders headers2 = response2.headers();
					Optional<String> ct2 = headers2.firstValue("Content-Type");
					isImage = false;
					if (ct2.isPresent()) {
						String contentType = ct2.get();
						logger.info("contentType2:\t" + contentType);
						if (contentType.contains("image")) {
							isImage = true;
							int slash = contentType.lastIndexOf('/');
							type = contentType.substring(slash+1);
						} else {
							Exception e = new Exception("Picture didn't download!!!");
							e.fillInStackTrace();
							throw e; //$NON-NLS-1$													
						}
					}
					response = response2;
				}
				logger.info("href = " + src); //$NON-NLS-1$
				logger.info("type = " + type); //$NON-NLS-1$
				logger.info("name = " + imgName); //$NON-NLS-1$

				InputStream is = decompress(response);
				logger.info("is available?\t" + is.available());
				FileCacheImageInputStream iis = new FileCacheImageInputStream(is, loc.getEpubDir());
				logger.info("iis length?\t" + iis.length());
				BufferedImage pic = ImageIO.read(iis);
				if (null == pic) {
					Exception e = new Exception("Picture didn't download!!!");
					e.fillInStackTrace();
					throw e; //$NON-NLS-1$													
				} else {
					try {
						logger.info("1. Make File");
						File outputFile = new File(loc.getEpubDir(), imgName + PERIOD + type);
						outputFile.createNewFile();
						logger.info("2. Change HTML "+ image.toString());
						image.attr(GFConstants.SRC_ATTR, outputFile.getName());
						logger.info("outputFile = " + outputFile); //$NON-NLS-1$
						ImageIO.write(pic, type, outputFile);
						loc.addImage(outputFile.getName());
					} catch (Exception e) {
						e.printStackTrace();
				//		image.remove();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				String pathname = image.attr(GFConstants.SRC_ATTR);
				int idx = pathname.lastIndexOf(SLASH) + 1;
				String name = pathname.substring(idx);
				image.attr(GFConstants.SRC_ATTR, name);
				loc.addImageFailure(pathname + "\t" + e); //$NON-NLS-1$
			}
		}

		private String findImage(Document doc, String imgName) {
			String src = null;
			Elements els = doc.getElementsByTag(GFConstants.IMG_TAG);
			if (els.size() == 1) {
				Element img = els.get(1);
				String href = img.attr(GFConstants.SRC_ATTR);
				if (null != href) {
					src = href;
				}
			} else {
				for (Element img : els) {
					String href = img.attr(GFConstants.SRC_ATTR);
					if (null != href && href.contains(imgName)) {
						src = href;
						break;
					}
				}
			}
			return src;
		}

		/**
		 * Image name and type may be changed.
		 * @param imgName
		 * @param imgType - jpg, gif, etc
		 * @return null if file does not exist
		 */
		private String wasPicDownloaded(String imgName, String imgType) {
			String fileName = null;

			File epubDir = loc.getEpubDir();
			if (epubDir.exists()) {
				File[] oldFiles = epubDir.listFiles();
				for (File f : oldFiles) {
					boolean isImageType = EpubFiles.isImageFile(f);
					if (isImageType) {
						String name = f.getName();
						if (name.contains(imgName)) {
							fileName = name;
							break;
						}
					}
				}
			}

			return fileName;
		}
	}

	class TimeOutTask extends TimerTask {
		private Thread thread;
		private Timer timer;

		public TimeOutTask(Thread thread, Timer timer) {
			this.thread = thread;
			this.timer = timer;
		}

		@Override
		public void run() {
			if (thread != null && thread.isAlive()) {
				thread.interrupt();
				timer.cancel();
			}
		}
	}
	
	static OutputStreamWriter getOSW(String dir, String filename)
			throws IOException, FileNotFoundException, UnsupportedEncodingException {
		File f = new File(dir, filename);
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		return osw;
	}
}