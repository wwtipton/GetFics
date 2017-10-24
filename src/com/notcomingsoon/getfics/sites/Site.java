package com.notcomingsoon.getfics.sites;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jsoup.Connection;
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

	static final String FFN = "fanfiction.net";

	static final String AFF = "adult-fanfiction.org";

	static final String TPP = "thepetulantpoetess.com";

	static final String DIGITAL_QUILL = "digital-quill.org";

	static final String SYCOPHANTEX = "sycophanthex.com";
	
	static final String TTH = "tthfanfic.org";
	
	static final String THE_MASQUE = "themasque.net";
	
	static final String AO3 = "archiveofourown.org";

	static final String GRANGER_ENCHANTED = "grangerenchanted.com";

	static final String MEDIA_MINER = "mediaminer.org";

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
		sites.add(GRANGER_ENCHANTED);
		sites.add(MEDIA_MINER);
		Collections.sort(sites, new SiteNameComparator());
	}

	protected String startUrl;

	protected Logger logger = GFLogger.getLogger();
	
	protected Charset siteCharset;
	
	protected Cookie[] cookies;

	private String siteName;
	
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


	protected abstract ArrayList<Chapter> getChapterList(Document doc);

	protected abstract String getAuthor(Document doc);

	protected abstract String getTitle(Document doc);
	
	protected Chapter extractSummary(Document story, Document chapter){
		return null;
	}

	protected abstract Document extractChapter(Document story, Document chapter,
			Chapter title);

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getPage(java.lang.String)
	 */
	Document getPage(String url) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "getPage(String url)");
		Connection conn = Jsoup.connect(url);
		conn.timeout(10000);
		
		conn = addCookies(conn);
		conn.method(Connection.Method.GET);
		Connection.Response response = conn.execute();
		Document doc = Jsoup.parse(new ByteArrayInputStream(response.bodyAsBytes()), siteCharset.name(), url);
		
		logger.exiting(this.getClass().getCanonicalName(), "getPage(String url)");
		return doc;
	}

	protected Connection addCookies(Connection conn) {
		
		if (cookies != null){
			for (int i = 0; i < cookies.length; i++){
				Cookie cookie = cookies[i];
				conn.cookie(cookie.getName(), cookie.getValue());
			}
		}
		return conn;
	}


	protected abstract boolean isOneShot(Document doc);

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
			
			Iterator<Chapter> cIter = chapterList.iterator();
			Chapter summary = null;
			while (cIter.hasNext()){
				Chapter c = cIter.next();
				Document nextDoc;
				if (c.getUrl().equalsIgnoreCase(startUrl)){
					nextDoc = doc;
					summary = extractSummary(story, nextDoc);
				} else {
					nextDoc = getPage(c.getUrl());
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
		// TODO Auto-generated method stub
		Elements images = story.getElementsByTag(HTMLConstants.IMG_TAG);
		 
		logger.info("images.size = " + images.size());
		for (int i = 0; i < images.size(); i++){
			Element image = images.get(i);
			String src = image.attr(HTMLConstants.SRC_ATTR);
			if (!src.contains(HTMLConstants.HTTP)){
				src= HTMLConstants.HTTP + this.siteName + SLASH + src;
			}
			int lastPeriod = src.lastIndexOf(PERIOD);
			String type = src.substring(lastPeriod+1);
			Iterator ri = ImageIO.getImageReadersBySuffix(type);
			if (!ri.hasNext()){
				type = JPEG;
			}
			logger.info("href = " + src);
			try {
				URL source = new URL(src);
				BufferedImage pic = ImageIO.read(source);
				if (null == pic) {
					image.remove();
				} else {
					// String filename = source.getFile();
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
				image.remove();
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
		Comment title = new Comment(startUrl, dir.getName());
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
	 * @param body
	 */
	protected void addChapterFooter(Element body) {
		body.appendElement(HTMLConstants.HR_TAG);
		body.appendElement(HTMLConstants.HR_TAG);
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
			if (s.equals(GRANGER_ENCHANTED) && GrangerEnchanted.isGrangerEnchanted(url)){
				site = new GrangerEnchanted(url);
				site.siteName = GRANGER_ENCHANTED;
				break;
			}		
			if (s.equals(MEDIA_MINER) && MediaMiner.isMediaMiner(url)){
				site = new MediaMiner(url);
				site.siteName = MEDIA_MINER;
				break;
			}		
		}
		if (site != null){
			story = site.download();
		}
		return story;
	}
	
}