package com.notcomingsoon.getfics.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import com.notcomingsoon.getfics.GFConstants;

public class PackageOPF 
extends EpubFiles
implements GFConstants
{
	static final String FILENAME = "package.opf";
	
	/** package  */
	private final static String PACKAGE_TAG = "package";
	private static final String PACKAGE_NAMESPACE = "http://www.idpf.org/2007/opf";
	private static final String DIR_ATTR = "dir";
	private static final String DIR_VALUE = "ltr";
	private static final String LANG_ATTR = "xml:lang";
	private static final String VERSION_VALUE = "3.0";
	private static final String UNIQUE_ID_ATTR = "unique-identifier";
	private static final String UNIQUE_ID_VALUE = "book-id";

	
	/** metadata  */
	private final static String METADATA_TAG = "metadata";
	private static final String METADATA_NAMESPACE = "http://purl.org/dc/elements/1.1/";
	
	private final static String TITLE_TAG = "dc:title";

	private final static String LANG_TAG = "dc:language";
	
	private final static String CREATOR_TAG = "dc:creator";

	private final static String META_TAG = "meta";
	private static final String PROP_ATTR = "property";
	private static final String PROP_ROLE_VALUE = "role";
	private static final String REFINES_ATTR = "refines";
	private static final String SCHEME_ATTR = "scheme";
	private static final String SCHEME_VALUE = "marc:relators";
	private static final String META_ROLE_TEXT = "AUT";
	private static final String PROP_SEQ_VALUE = "display-seq";
	private static final String TERMS_MODIFIED_VALUE = "dcterms:modified";
	
	
	private final static String IDENTIFIER_TAG = "dc:identifier";

	private final static String DATE_TAG = "dc:date";
	
	private final static String SUBJECT_TAG = "dc:subject";
	
	private static final String COVER_VALUE = "cover";
	private static final String CONTENT_ATTR = "content";
	
	private final static String PUBLISHER_TAG = "dc:publisher";
	
	
	/** manifest */
	private final static String MANIFEST_TAG = "manifest";

	private final static String ITEM_TAG = "ITEM";
	private static final String MEDIA_TYPE_ATTR = "media-type";
	private static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";
	private static final String MEDIA_TYPE_PNG = "image/png";
	private static final String MEDIA_TYPE_JPEG = "image/jpeg";
	private static final String MEDIA_TYPE_GIF = "image/gif";
	
	private static final String PROPERTIES_ATTR = "properties";
	private static final String PROPERTIES_NAV_VALUE = "nav";
	private static final String PROPERTIES_COVER_VALUE = "cover-image";

	/** spine */
	private final static String SPINE_TAG = "spine";
	private static final String PAGE_DIR_ATTR = "page-progression-direction";
	
	private final static String ITEMREF_TAG = "itemref";
	private static final String IDREF_ATTR = "idref";
	
	ArrayList<String> subjects = new ArrayList<String>();
	ArrayList<Author> authors = new ArrayList<Author>();
	ArrayList<Chapter> chapters = new ArrayList<Chapter>();
	private static final String ITEM_VALUE = "item";
	
	ArrayList<String> images = new ArrayList<String>();
	private static final String PIC_VALUE = "pic";
	private static final String PNG = "png";
	private static final String GIF = "gif";
	
	String title;
	String publisher;
	
	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	Date lastModifiedDate;
	Date now = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	Document opf;
	
	public String getFormattedLastModified() {
		return sdf.format(lastModifiedDate);
	}

	public String getFormattedNow() {
		return sdf.format(now);
	}


	public void setLastModified(long lastModified) {
		this.lastModifiedDate = new Date(lastModified);
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}

	public String getUniqueId() {
		StringBuffer sb = new StringBuffer();
		
		for (Author a : authors) {
			sb.append(a.getAuthorName());
			if (a.getCount() < authors.size()) {
				sb.append(", ");
			} else {
				sb.append("-");				
			}
		}
		
		sb.append(title);
		
		return sb.toString();
	}

	public ArrayList<String> getSubjects() {
		return subjects;
	}


	public void setSubjects(ArrayList<String> subjects) {
		this.subjects = subjects;
	}


	public ArrayList<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(String[] auts) {
		for (int i = 0; i < auts.length; i++) {
			int j = i + 1;
			Author a = new Author(auts[i], j);
			authors.add(a);
		}
	}


	public ArrayList<Chapter> getChapters() {
		return chapters;
	}


	public void setChapters(ArrayList<Chapter> chapters) {
		this.chapters = chapters;
	}


	public ArrayList<String> getImages() {
		return images;
	}


	public void setImages(ArrayList<String> images) {
		this.images = images;
	}


	class Author {
		String authorName;
		String refId;
		int count;
		
		private static final String REF_ID_PREFIX = "author";
		
		public Author(String authorName, int idx) {
			super();
			setAuthorName(authorName);
			setRefId(idx);
		}

		public String getAuthorName() {
			return authorName;
		}
		
		public void setAuthorName(String authorName) {
			this.authorName = authorName;
		}
		
		public String getRefId() {
			return refId;
		}
		
		public void setRefId(int idx) {
			count = idx;
			this.refId = REF_ID_PREFIX + idx;
		}

		public int getCount() {
			return count;
		}

		
	}


	public void writePackage(File epubDir) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		OutputStreamWriter osw = getOSW(epubDir, FILENAME);
		String content = opf.html();
		osw.write(content);

		osw.close();
	}

	public void buildDoc() throws UnsupportedEncodingException {
		opf = new Document(Parser.NamespaceXml);
		setOutputType(opf, true);
		
		Element p = buildPackageTag();
		opf.root().appendChild(p);
		
		Element metadata = buildMetadata();
		p.appendChild(metadata);

		Element manifest = buildManifest();
		p.appendChild(manifest);

		Element spine = buildSpine();
		p.appendChild(spine);

	}

	private Element buildSpine() {
		Element spine = opf.createElement(SPINE_TAG);
		spine.attr(PAGE_DIR_ATTR, DIR_VALUE);
		
		for (int i = 1; i <= chapters.size(); i++) {
			Element itemref = spine.appendElement(ITEMREF_TAG);
			itemref.attr(IDREF_ATTR, ITEM_VALUE + i);
		}
		
		return spine;
	}

	private Element buildManifest() throws UnsupportedEncodingException {
		Element manifest = opf.createElement(MANIFEST_TAG);
		
		int idx = 0;
		for (Chapter c : chapters) {
			idx++;
			Element item = manifest.appendElement(ITEM_TAG);
			item.attr(HREF_ATTR, c.getFilename());
			item.attr(ID_ATTR, ITEM_VALUE + idx);
			item.attr(MEDIA_TYPE_ATTR, MEDIA_TYPE_XHTML);
			if (c.isTOC() || chapters.size() == 1) {
				item.attr(PROPERTIES_ATTR, PROPERTIES_NAV_VALUE);
			}
		}

		idx = 0;
		for (String image : images) {
			idx++;
			
			File img = new File(image);
			String name = img.getName();

			Element item = manifest.appendElement(ITEM_TAG);
			String id = "A" + name;
			item.attr(ID_ATTR, id);
			item.attr(HREF_ATTR, name);
			
			if (isPNG(name)) {
				item.attr(MEDIA_TYPE_ATTR, MEDIA_TYPE_PNG);
			} else {
				if (isGIF(name)) {
					item.attr(MEDIA_TYPE_ATTR, MEDIA_TYPE_GIF);					
				} else {
					item.attr(MEDIA_TYPE_ATTR, MEDIA_TYPE_JPEG);					
				}
			}

			if (idx == 1) {
				item.attr(PROPERTIES_ATTR, PROPERTIES_COVER_VALUE);
			}
		}
				
		return manifest;
	}

	private boolean isPNG(String filename) {
		boolean isPng = false;
		
		int period = filename.lastIndexOf(".");
		String ext = filename.substring(period + 1);
		if (PNG.equalsIgnoreCase(ext)) {
			isPng = true;
		}
		return isPng;
	}

	private boolean isGIF(String filename) {
		boolean isGif = false;
		
		int period = filename.lastIndexOf(".");
		String ext = filename.substring(period + 1);
		if (GIF.equalsIgnoreCase(ext)) {
			isGif = true;
		}
		return isGif;
	}

	private Element buildMetadata() {
		Element metadata = opf.createElement(METADATA_TAG);
		metadata.attr(NAMESPACE_ATTR + ":dc", METADATA_NAMESPACE);
		
		Element t = metadata.appendElement(TITLE_TAG);
		t.text(title);
		
		if (null != publisher && publisher != "") {
			Element p = metadata.appendElement(PUBLISHER_TAG);
			p.text(publisher);
		}
		
		Element l = metadata.appendElement(LANG_TAG);
		l.text(LANGUAGE);
		
		buildAuthors(metadata);
		
		Element id = metadata.appendElement(IDENTIFIER_TAG);
		id.attr(ID_ATTR, UNIQUE_ID_VALUE);
		id.text(getUniqueId());
		
		Element d = metadata.appendElement(DATE_TAG);
		d.text(getFormattedNow());
		
		buildSubjects(metadata);

		Element m1 = metadata.appendElement(META_TAG);
		m1.attr(PROP_ATTR, TERMS_MODIFIED_VALUE);
		m1.text(getFormattedLastModified());

		
		if (!images.isEmpty()) {
			Element m2 = metadata.appendElement(META_TAG);
			m2.attr(NAME_ATTR, COVER_VALUE);
			File img = new File (images.get(0));
			String name = img.getName();
			m2.attr(CONTENT_ATTR, name);
		}		
		return metadata;
	}

	private void buildSubjects(Element metadata) {
		for(String s : subjects) {
			Element subject = metadata.appendElement(SUBJECT_TAG);
			subject.text(s);
		}
	}

	private void buildAuthors(Element metadata) {
		for (Author a : authors) {
			
			Element c = metadata.appendElement(CREATOR_TAG);
			c.attr(ID_ATTR, a.refId);
			c.text(a.getAuthorName());
			
			Element m1 = metadata.appendElement(META_TAG);
			m1.attr(PROP_ATTR, PROP_ROLE_VALUE);
			m1.attr(REFINES_ATTR, "#"+a.refId);
			m1.attr(SCHEME_ATTR, SCHEME_VALUE);
			m1.text(META_ROLE_TEXT);
			
			Element m2 = metadata.appendElement(META_TAG);
			m2.attr(PROP_ATTR, PROP_SEQ_VALUE);
			m2.attr(REFINES_ATTR, "#"+a.refId);
			m2.text("" + a.getCount());
		}
	}

	private Element buildPackageTag() {
		Element p = opf.createElement(PACKAGE_TAG);
		p.attr(NAMESPACE_ATTR, PACKAGE_NAMESPACE);
		p.attr(DIR_ATTR, DIR_VALUE);
		p.attr(LANG_ATTR, LANGUAGE);
		p.attr(VERSION_ATTR, VERSION_VALUE);
		p.attr(UNIQUE_ID_ATTR, UNIQUE_ID_VALUE);
		
		return p;
	}


}
