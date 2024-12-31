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
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
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

	private static final String OUTPUT_DIRECTORY = GFProperties.getPropertyValue(GFProperties.OUTPUT_DIRECTORY_KEY);

	private static final String EPUB_DIRECTORY = GFProperties.getPropertyValue(GFProperties.EPUB_SUBDIRECTORY_ROOT_KEY);

	File epubFile;

	File parentDirFile;

	String ficPathStr;

	File ficPathFile;

	String fileAuthor;

	String fileTitle;

	String origAuthor;

	String origTitle;

	String delimitedAuthor;

	File outputDir;
	
	String startUrl;

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	private boolean isOneShot = false;

	ArrayList<Chapter> chapters = new ArrayList<Chapter>();

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

		parentDirFile = createDirectory(this.getFileAuthor(), OUTPUT_DIRECTORY);
		File ficPathFile = createDirectory(this.getFileTitle(), parentDirFile.getPath());
		epubDir = createDirectory(EPUB_DIRECTORY, ficPathFile.getPath());

		setOutputDir(ficPathFile);

		initialize();
	}

	/*
	 * public Epub(String ficPath) { ficPathStr = ficPath; ficPathFile = new
	 * File(ficPathStr); parentDirFile = ficPathFile.getParentFile(); epubDir =
	 * parentDirFile.getPath() + File.separator +
	 * EFProperties.getPropertyValue(EFProperties.EPUB_SUBDIRECTORY_ROOT_KEY) +
	 * File.separator; initialize(); }
	 */

	private void initialize() {
		if (epubDir.exists()) {
			File[] oldFiles = epubDir.listFiles();
			for (File f : oldFiles) {
				if (f.isDirectory()) {
					File[] meta = f.listFiles();
					for (File m : meta) {
						m.delete();
					}
				}
				f.delete();
			}
			epubDir.delete();
		}

		File[] oldFiles = outputDir.listFiles();
		for (File f : oldFiles) {
			String name = f.getName();
			if (name.endsWith(EPUB_EXT) || name.endsWith("xml")) {
				f.delete();
			}
		}

	}

	public File getParentDirectory() {
		return parentDirFile;
	}

	public void setParentDirectory(File parentDirectory) {
		this.parentDirFile = parentDirectory;
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
			c.writeChapter(epubDir);
		}

		//copyImages();

		opf.writePackage(epubDir);

		zipEpubDir(opf.getUniqueId());
	}

	private void zipEpubDir(String targetFilename) throws Exception {
		epubFile = new File(outputDir, targetFilename + EPUB_EXT);

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

	/*
	private void copyImages() throws IOException {
		Path targetDir = epubDir.toPath();

		for (String s : imagePaths) {
			File source = new File(parentDirFile, s);
			Path sourcePath = source.toPath();
			Path targetFile = targetDir.resolve(sourcePath.getFileName());
			Files.copy(sourcePath, targetFile, StandardCopyOption.COPY_ATTRIBUTES);
		}

	}
	*/

	public void build() throws IOException {
	//	Document s = loadStory(ficPathStr);

	//	makeChapters(s, ficPathStr);
		toc = new Contents(chapters);
		chapters.add(0, toc);

		opf = new PackageOPF();
		getAuthorsAndTitle(opf);
		opf.setPublisher(getSite());
		opf.setSubjects(getTags());
		opf.setLastModified(outputDir.lastModified());
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
		images.add(name);
	}

	private void getAuthorsAndTitle(PackageOPF opf) {
			opf.setTitle(origTitle);

			String[] auts = origAuthor.split(COMMA);
			opf.setAuthors(auts);
	}

	/*
	 * private ArrayList<String> getTags(Document story) { ArrayList<String>
	 * subjects = new ArrayList<String>(); String[] tags = null;
	 * 
	 * // Elements h3Refs = Selector.select(H3_TAG + ":contains(" + "\"" +"Tags" +
	 * "\"" + ")", story.root()); Elements h3Refs =
	 * Selector.select("h3:contains(Tags)", story.root()); if (h3Refs.size() > 0) {
	 * Element h3Ref = h3Refs.first(); Node n = h3Ref.nextSibling(); if (n
	 * instanceof TextNode) { TextNode t = (TextNode) n; String text = t.text();
	 * tags = text.split(COMMA); } }
	 * 
	 * if (null != tags) { for (String str : tags) { subjects.add(str.trim()); } }
	 * 
	 * return subjects; }
	 */

	/*
	 * void makeChapters(Document story, String ficPath) throws
	 * UnsupportedEncodingException { fixStyles(story);
	 * 
	 * Elements aRefs = Selector.select(A_TAG + "[" + NAME_ATTR + "]",
	 * story.root()); Elements someRefs = new Elements();
	 * 
	 * 
	 * if (null == aRefs || aRefs.size() <= 2) { //Summary plus chapter File f = new
	 * File(ficPath); String name = f.getName(); int dot = name.lastIndexOf('.');
	 * name = name.substring(0, dot); Chapter c = new Chapter(name, story);
	 * chapters.add(c); return; }
	 * 
	 * 
	 * int size = aRefs.size(); for (int i = 0; i < size; i++) { Element e =
	 * aRefs.get(i); Attributes attrs = e.attributes(); if (attrs.size() == 1) {
	 * someRefs.add(e); } }
	 * 
	 * int someSize = someRefs.size();
	 * 
	 * if (size == 0) { // no refs String name = parentDirFile.getName();
	 * 
	 * Element head = Selector.select(HEAD_TAG, story.root()).first(); if (null ==
	 * head) { head = new Element(HEAD_TAG); story.root().appendChild(head); }
	 * 
	 * Element titleTag = Selector.select(TITLE_TAG, story.root()).first(); if (null
	 * == titleTag) { titleTag = new Element(TITLE_TAG); titleTag.text(name);
	 * head.appendChild(titleTag); } else { titleTag.text(name); }
	 * 
	 * Chapter c = new Chapter(name, story); chapters.add(c);
	 * 
	 * } else { for (int i = 0; i < someSize; i++) { Element e = someRefs.get(i);
	 * 
	 * String name = e.attr(NAME_ATTR); if (name != null && name.contains("&amp;"))
	 * { name.replace("&amp;", "&"); } Document doc = story.clone(); Element body =
	 * doc.body(); body.remove(); doc.body();
	 * 
	 * int j = i + 1; Element nextRef = null; if (j < someSize) { nextRef =
	 * someRefs.get(j); }
	 * 
	 * // copyElements(story, doc, e, nextRef);
	 * 
	 * Element titleTag = Selector.select(TITLE_TAG, doc.root()).first(); if (null
	 * == titleTag) { Element head = Selector.select(HEAD_TAG, doc.root()).first();
	 * titleTag = new Element(TITLE_TAG); titleTag.text(name);
	 * head.appendChild(titleTag); } else { titleTag.text(name); }
	 * 
	 * Chapter c = new Chapter(name, doc); chapters.add(c);
	 * 
	 * } }
	 * 
	 * 
	 * 
	 * //fix names for (Chapter c : chapters) { Document d = c.getDoc(); Elements
	 * refs = Selector.select(A_TAG + "[" + NAME_ATTR + "]", d.root()); Element ref
	 * = refs.first(); if (null != ref) { ref.remove(); // String name =
	 * ref.attr(NAME_ATTR); // ref.attr(NAME_ATTR, urlFileName(name)); } }
	 * 
	 * return; }
	 */
	
	void fixStyles(Document story) {

		/*
		 * Elements styles = Selector.select("[style]", story.root()); for (Element
		 * s:styles) { String value = s.attr("style"); if (value.contains(": ")) {
		 * String newValue = value.replace(": ", ":"); s.attr("style", newValue); } }
		 */
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
		private static String DIR_NAME = "META-INF";// + File.separator;
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
//		Checker.main(args);
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

	public File getOutputDir() {
		return outputDir;
	}

	void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
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
		fixStyles(c.getDoc());
		chapters.add(c);
	}

	public void addImageFailure(String failure) {
		imageFailures.add(failure);
	}


	public ArrayList<String> getImageFailures() {
		// TODO Auto-generated method stub
		return imageFailures;
	}
}
