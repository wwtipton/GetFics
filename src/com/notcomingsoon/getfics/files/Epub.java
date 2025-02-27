package com.notcomingsoon.getfics.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import com.adobe.epubcheck.tool.EpubChecker;
import com.notcomingsoon.getfics.GFConstants;
import com.notcomingsoon.getfics.GFLogger;
import com.notcomingsoon.getfics.GFProperties;

public class Epub extends EpubFiles implements GFConstants {
	private Logger logger = GFLogger.getLogger();

	private static final String COMMA = ",";

	private static final String EPUB_EXT = "." + GFProperties.getPropertyValue(GFProperties.EPUB_EXTENSION_KEY);

	private static final String PUBLISH_DIR = GFProperties.getPropertyValue(GFProperties.PUBLISH_DIRECTORY_KEY);

	private static final String FAILURE_DIR = GFProperties.getPropertyValue(GFProperties.PUBLISH_ERROR_DIR_KEY);

	private static final String OUTPUT_ROOT_DIRECTORY = GFProperties.getPropertyValue(GFProperties.OUTPUT_ROOT_DIRECTORY_KEY);

	private static final String EPUB_DIRECTORY = GFProperties.getPropertyValue(GFProperties.EPUB_SUBDIRECTORY_ROOT_KEY);

	File epubFile;

	File authorDirFile;

	String ficPathStr;

	File ficPathFile;

	String fileAuthor;

	String fileTitle;

	String origAuthor;

	String origTitle;

	String delimitedAuthor;

	File titleDirFile;
	
	String startUrl;

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	private boolean isOneShot = false;

	ArrayList<Chapter> chapters = new ArrayList<Chapter>();
	
	ArrayList<Chapter> newChapters = new ArrayList<Chapter>();
	
	public ArrayList<Chapter> getNewChapters() {
		return newChapters;
	}

	public void addNewChapter(Chapter c) {
		this.newChapters.add(c);
	}

	HashMap<String, File> existingChapters = new HashMap<String, File>();

	ArrayList<String> tags = null;

	private ArrayList<String> imageFailures = new ArrayList<String>();
	
	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	Contents toc;

	PackageOPF opf;

	File epubDir;

	public File getEpubDir() {
		return epubDir;
	}

	private ArrayList<String> images = new ArrayList<String>();

	Charset charset;

	public static Epub createEpub(String author, String title) throws Exception {
		Epub s = new Epub(author, title);
		return s;
	}

	/**
	 * @param author
	 * @param title
	 * @throws UnsupportedEncodingException
	 */
	public Epub(String author, String title) throws UnsupportedEncodingException {
		super();
		setFileAuthor(author);
		setFileTitle(title);
		setOrigAuthor(author);
		setOrigTitle(title);
		setDelimitedAuthor(author);

		authorDirFile = createDirectory(filterBadChars(this.getOrigAuthor()), OUTPUT_ROOT_DIRECTORY);
		titleDirFile = createDirectory(filterBadChars(this.getOrigTitle()), authorDirFile.getPath());
		epubDir = new File(titleDirFile, EPUB_DIRECTORY);    
		initialize();
		epubDir.mkdir();
		
		
	}

	public boolean doesChapterFileExist(String filename) {
		boolean ifExists = false;
		
		File f = existingChapters.get(filename);
		if (null != f) {
			ifExists = true;
		}
		
		return ifExists;
	}
	
	private void initialize() {
		if (epubDir.exists()) {
			boolean canDelete = true;
			File[] oldFiles = epubDir.listFiles();
			for (File f : oldFiles) {
				//Getting images is hard so keep them.
				if (isImageFile(f)) {
					canDelete = false;
					continue;
				}
				if (Chapter.isChapterFile(f)) {
					canDelete = false;
					if (!Contents.isTOCFile(f) && !Chapter.isSummaryFile(f)) {
						existingChapters.put(f.getName(), f);
						continue;
					}
				}
				if (f.isDirectory()) {
					File[] meta = f.listFiles();
					for (File m : meta) {
						m.delete();
					}
				}
				f.delete();
			}
			if (canDelete) {
				epubDir.delete();
			}
		}

		File[] oldFiles = titleDirFile.listFiles();
		if (null != oldFiles) {
			for (File f : oldFiles) {
				String name = f.getName();
				if (name.endsWith(EPUB_EXT) || name.endsWith("xml")) {
					f.delete();
				}
			}
		}
	}

	public ArrayList<Chapter> getChapters() {
		return chapters;
	}

	public Contents getToc() {
		return toc;
	}

	public PackageOPF getOpf() {
		return opf;
	}

	public void writeEpub() throws Exception {
		epubDir.mkdir();

		Mimetype.writeMimetype(epubDir);
		Container.writeContainer(epubDir);

		for (Chapter c : chapters) {
			if (!c.doesChapterExist) {
				c.writeChapter(epubDir);
			}
		}

		opf.writePackage(epubDir);

		zipEpubDir(opf.getUniqueId());
	}

	private void zipEpubDir(String targetFilename) throws Exception {
		epubFile = new File(titleDirFile, targetFilename + EPUB_EXT);

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(epubFile));

		zipStoreFile(Mimetype.FILENAME, zos);
		zipFile(Container.DIR_NAME + "/" + Container.FILENAME, zos);
		zipFile(PackageOPF.FILENAME, zos);

		for (Chapter c : chapters) {
			zipFile(c.getFilename(), zos);
		}

		
		for (String s : images) {
			Path source = Path.of(s);
			zipFile(source.getFileName().toString(), zos);
		}

		zos.close();
	}

	/**
	 * Computes the CRC checksum for the given file.
	 *
	 * @param file The file to compute checksum for.
	 * @return A CRC32 checksum.
	 * @throws IOException If an I/O error occurs.
	 */
	private static long computeCrc(File file) throws IOException {
		CRC32 crc = new CRC32();
		InputStream in = new FileInputStream(file);

		try {
			byte[] buf = new byte[8192];
			int n = in.read(buf);
			while (n != -1) {
				crc.update(buf, 0, n);
				n = in.read(buf);
			}
		} finally {
			in.close();
		}
		return crc.getValue();
	}

	private void zipStoreFile(String filename, ZipOutputStream zos) throws IOException {
		ZipEntry ze = new ZipEntry(filename);
		File f = new File(epubDir + File.separator + filename);

		ze.setMethod(ZipEntry.STORED);
		ze.setSize(f.length());
		ze.setCrc(computeCrc(f));

		zos.putNextEntry(ze);

		Path p = f.toPath();
		Files.copy(p, zos);

		zos.closeEntry();
	}

	private void zipFile(String filename, ZipOutputStream zos) throws IOException {
		ZipEntry ze = new ZipEntry(filename);
		zos.putNextEntry(ze);

		Path p = Paths.get(epubDir.getPath(), filename);
		Files.copy(p, zos);

		zos.closeEntry();
	}

	public void build() throws IOException {
		toc = new Contents(chapters);
		chapters.add(0, toc);

		opf = new PackageOPF();
		getAuthorsAndTitle(opf);
		opf.setPublisher(getSite());
		opf.setSubjects(getTags());
		opf.setLastModified(titleDirFile.lastModified());
		opf.setImages(getImages());
		opf.setChapters(chapters);
		opf.buildDoc();
	}

	private String getSite() {
		String site = null;

		int idx = startUrl.indexOf("//");
		String s = startUrl.substring(idx + 2);
		idx = s.indexOf('/');
		site = s.substring(0, idx);

		return site;
	}

	private ArrayList<String> getImages() {
		return images;
	}

	public void addImage(String name) {
		if (!images.contains(name)) {
			images.add(name);
		}
	}

	private void getAuthorsAndTitle(PackageOPF opf) {
			opf.setTitle(origTitle);

			String[] auts = origAuthor.split(COMMA);
			opf.setAuthors(auts);
	}

	void fixStyles(Document story) {

		Elements empties = Selector.select("[*]", story.root());
		for (Element empty : empties) {
			List<Attribute> attrs = empty.attributes().asList();
			for (Attribute attr : attrs) {
				if (attr.getValue() == "") {
					empty.removeAttr(attr.getKey());
				}
			}

		}

		Elements elems = Selector.select("[align]", story.root());
		for (Element e : elems) {
			String value = e.attr("align");
			String newValue = "text-align:" + value;
			e.removeAttr("align");
			e.attr("style", newValue);
		}

		Elements centers = Selector.select("center", story.root());
		for (Element c : centers) {
			c.tagName("div");
			c.attr("style", "text-align:center");
		}

		Elements strikes = Selector.select("strike", story.root());
		for (Element s : strikes) {
			s.tagName("del");
		}

		Elements imgs = Selector.select("img", story.root());
		for (Element i : imgs) {
			String value = i.attr("src");
			int slashIdx = value.lastIndexOf('\\');
			String newValue = value.substring(slashIdx + 1);
			i.attr("src", newValue);
			i.removeAttr("onload");
		}

		Elements anchors = Selector.select(A_TAG, story.root());
		for (Element a : anchors) {
			String value = a.attr(HREF_ATTR);
			int slashIdx = value.indexOf('/');
			if (slashIdx == 0) {
				a.attr(HREF_ATTR, "");
			}
			a.removeAttr("rel");
			if (value.equals("about:blank")) {
				a.attr(HREF_ATTR, "");
			}
		}

		Elements rules = Selector.select("hr", story.root());
		for (Element r : rules) {
			r.removeAttr("size");
			r.removeAttr("noshade");
			r.removeAttr("width");
		}

		Elements metas = Selector.select("meta", story.root());
		for (Element m : metas) {
			String httpEquiv = m.attr("http-equiv");
			if ("Content-Type".equals(httpEquiv)) {
				m.attr("content", "text/html; charset=utf-8");
			} else {
				m.remove();
			}
		}

		Elements scripts = Selector.select("SCRIPT", story.root());
		for (Element s : scripts) {
			s.remove();
		}

		Elements links = Selector.select("link", story.root());
		for (Element l : links) {
			l.remove();
		}

	}

	Document loadStory(String ficPathStr) throws IOException {
		File input = new File(ficPathStr);

		Charset cs = Charset.defaultCharset();
		Document story = Jsoup.parse(input, cs.displayName());

		story = setOutputType(story);

		return story;
	}

	static class Mimetype {
		private static String VALUE = "application/epub+zip";
		private static String FILENAME = "mimetype";

		static void writeMimetype(File epubDir) throws IOException {
			OutputStreamWriter osw = getOSW(epubDir, FILENAME);
			osw.write(VALUE);

			osw.close();
		}

	}

	static class Container {
		private static String VALUE = "<?xml version=\"1.0\"?><container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\"><rootfiles><rootfile full-path=\"package.opf\" media-type=\"application/oebps-package+xml\" /></rootfiles></container>";
		private static String DIR_NAME = "META-INF";
		private static String FILENAME = "container.xml";

		static void writeContainer(File epubDir) throws IOException {
			File f = new File(epubDir, DIR_NAME);
			f.mkdir();

			OutputStreamWriter osw = getOSW(f, FILENAME);
			osw.write(VALUE);

			osw.close();
		}

	}

	public int validate() throws IOException, InterruptedException {
		String[] args = new String[] { epubFile.toString(), "-e", "--out" };
		EpubChecker checker = new EpubChecker();
		return checker.run(args);
	}

	public void publish() throws IOException {
		File publish = new File(PUBLISH_DIR, epubFile.getName());
		publish.createNewFile();
		logger.info("publish = " + publish.getCanonicalPath());

		if (epubFile.exists() && publish.exists()) {
			FileInputStream fis = new FileInputStream(epubFile);
			FileOutputStream fos = new FileOutputStream(publish);

			while (fis.available() > 0) {
				int i = fis.read();
				fos.write(i);
			}

			fis.close();
			fos.close();
		}

		logger.exiting(this.getClass().getCanonicalName(), "publish()");
	}

	public void publishFailure() throws IOException {
		File publish = new File(FAILURE_DIR, epubFile.getName());
		publish.createNewFile();
		logger.info("publish = " + publish.getCanonicalPath());

		if (epubFile.exists() && publish.exists()) {
			FileInputStream fis = new FileInputStream(epubFile);
			FileOutputStream fos = new FileOutputStream(publish);

			while (fis.available() > 0) {
				int i = fis.read();
				fos.write(i);
			}
			fis.close();
			fos.close();
		}

		logger.exiting(this.getClass().getCanonicalName(), "publish()");
	}

	public String getFileAuthor() {
		return fileAuthor;
	}

	public boolean isOneShot() {
		return isOneShot;
	}

	public void setOneShot(boolean isOneShot) {
		this.isOneShot = isOneShot;
	}

	public void setFileAuthor(String author) throws UnsupportedEncodingException {
		this.fileAuthor = urlFileName(author);
	}

	public String getFileTitle() {
		return fileTitle;
	}

	public void setFileTitle(String title) throws UnsupportedEncodingException {
		this.fileTitle = urlFileName(title);
	}

	public String getOrigAuthor() {
		return origAuthor;
	}

	public void setOrigAuthor(String origAuthor) {
		this.origAuthor = origAuthor;
	}

	public String getOrigTitle() {
		return origTitle;
	}

	public void setOrigTitle(String origTitle) {
		this.origTitle = origTitle;
	}

	public File getTitleDir() {
		return titleDirFile;
	}

	void setDelimitedAuthor(String author) {
		delimitedAuthor = author.replace(',', ';');
	}

	public String getDelimitedAuthor() {
		return delimitedAuthor;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}

	public void addChapter(Chapter c) {
		if (!c.doesChapterExist()) {
			fixStyles(c.getDoc());
		}
		chapters.add(c);
	}

	public void addImageFailure(String failure) {
		imageFailures.add(failure);
	}


	public ArrayList<String> getImageFailures() {
		return imageFailures;
	}
}
