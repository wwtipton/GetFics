package com.notcomingsoon.getfics.sites;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.GFLogger;
import com.notcomingsoon.getfics.HTMLConstants;
import com.notcomingsoon.getfics.Story;



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

	// Site is defunct static final String GRANGER_ENCHANTED = "grangerenchanted.com";

	static final String MEDIA_MINER = "mediaminer.org";

// Site is defunct	static final String FICTION_ALLEY = "fictionalley.org";

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
// Site is defunct		sites.add(GRANGER_ENCHANTED);
		sites.add(MEDIA_MINER);
		sites.add(FICTION_HUNT);
// Site is defunct		sites.add(FICTION_ALLEY);
		sites.add(WITCH_FICS);
		sites.add(HUNTING_HORCRUXES);
		sites.add(SSHG_EXCHANGE);
		Collections.sort(sites, new SiteNameComparator());
	}

	protected String startUrl;

	protected Logger logger = GFLogger.getLogger();
	
	protected Charset siteCharset = HTMLConstants.UTF_8;
	
	protected Cookie[] cookies;

	private String siteName;

	protected static final String SUMMARY_STRING = "Summary";
	
	static Document page;
	
    static final CookieStore cookieStore = new BasicCookieStore();

    static final RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(StandardCookieSpec.RELAXED)
            .build();

	
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

        final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                .useSystemProperties()
                // IMPORTANT uncomment the following method when running Java 9 or older
                // in order for ALPN support to work and avoid the illegal reflective
                // access operation warning
                /*
                .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
                    @Override
                    public TlsDetails create(final SSLEngine sslEngine) {
                        return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                    }
                })
                */
                .build();
        final PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .build();
        try (final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .setConnectionManager(cm)
                .setDefaultCookieStore(cookieStore)
               .build()) {

            client.start();

    		URI uri = new URI(url);
    		
    		final HttpHost target = new HttpHost(uri.getHost());
            final String requestUri = uri.getPath();
            
            final HttpClientContext clientContext = HttpClientContext.create();

            final SimpleHttpRequest request = SimpleHttpRequests.get(target, requestUri);
            final Future<SimpleHttpResponse> future = client.execute(
                    SimpleRequestProducer.create(request),
                    SimpleResponseConsumer.create(),
                    clientContext,
                    new FutureCallback<SimpleHttpResponse>() {

                        @Override
                        public void completed(final SimpleHttpResponse response) {
                            System.out.println(requestUri + "->" + response.getCode());
                            SimpleBody body = response.getBody();
                            System.out.println(body);
                            final SSLSession sslSession = clientContext.getSSLSession();
                            if (sslSession != null) {
                                System.out.println("SSL protocol " + sslSession.getProtocol());
                                System.out.println("SSL cipher suite " + sslSession.getCipherSuite());
                            }
                            
                            page = Jsoup.parse(body.getBodyText());
                            
                        }

                        @Override
                        public void failed(final Exception ex) {
                            System.out.println(requestUri + "->" + ex);
                        }

                        @Override
                        public void cancelled() {
                            System.out.println(requestUri + " cancelled");
                        }

                    });
            future.get();

            System.out.println("Shutting down");
            client.close(CloseMode.GRACEFUL);
        }
		


		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");

		return page;

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
			/* Site is defunct
			if (s.equals(GRANGER_ENCHANTED) && GrangerEnchanted.isGrangerEnchanted(url)){
				site = new GrangerEnchanted(url);
				site.siteName = GRANGER_ENCHANTED;
				break;
			}	
			*/	
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
			/* Site is defunct
			if (s.equals(FICTION_ALLEY) && FictionAlley.isFictionAlley(url)){
				site = new FictionAlley(url);
				site.siteName = FICTION_ALLEY;
				break;
			}
			*/
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
		BasicClientCookie c = new BasicClientCookie(key, value);
		
		c.setPath("/");
		c.setSecure(true);
		c.setDomain(u.getHost());
		
		cookieStore.addCookie(c);
		
//		c.setHttpOnly(true);
//		c.setVersion(0);
		//cookieManager.getCookieStore().add(u, c);
	}


	void login() throws IOException, InterruptedException {
		//Intentionally left empty.
	}


	public static void close() {
		// TODO Auto-generated method stub
	//	 client.close(CloseMode.GRACEFUL);
	//	 System.out.println("Client closed!!");
	}


		
	
}