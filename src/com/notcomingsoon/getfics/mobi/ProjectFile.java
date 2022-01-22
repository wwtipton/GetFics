/**
 * 
 */
package com.notcomingsoon.getfics.mobi;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.GFLogger;
import com.notcomingsoon.getfics.HTMLConstants;
import com.notcomingsoon.getfics.Story;

/**
 * @author Winifred Tipton
 *
 */
public class ProjectFile {

	private static Logger logger = GFLogger.getLogger();

	static final String PROJECT_EXTENSION = "opf";

	Document project = null;

	private Story story;
	
	static final String PACKAGE_TAG = "package";
	static final String METADATA_TAG = "metadata";
	static final String X_METADATA_TAG = "x-metadata";
	static final String DC_METADATA_TAG = "dc-metadata";
	static final String XMLNS_DC_ATTR = "xmlns:dc";
	static final String XMLNS_DC_ATTR_VALUE = "http://purl.org/metadata/dublin_core";
	static final String XMLNS_OEBPACKAGE_ATTR = "xmlns:oebpackage";
	static final String XMLNS_OEBPACKAGE_ATTR_VALUE = "http://openebook.org/namespaces/oeb-package/1.0/";
	static final String DC_TITLE_TAG = "dc:Title";
	static final String DC_LANGUAGE_TAG = "dc:Language";
	static final String DC_LANGUAGE_TAG_VALUE = "en-us";	
	static final String DC_CREATOR_TAG = "dc:Creator";
	static final String OUTPUT_TAG = "output";
	static final String ENCODING_ATTR = "encoding";
	static final String MANIFEST_TAG = "manifest";	
	static final String ITEM_TAG = "item";
	static final String ID_ATTR = "id";
	static final String ID_PREFIX = "item";
	static final String MEDIA_TYPE_ATTR = "media-type";
//	static final String MEDIA_TYPE_ATTR_VALUE = "text/x-oeb1-document";	
	static final String MEDIA_TYPE_ATTR_VALUE = "application/xhtml+xml";	
	static final String HREF_ATTR = "href";
	static final String SPINE_TAG = "spine";
	static final String ITEMREF_TAG = "itemref";
	static final String IDREF_ATTR = "idref";
	static final String TOURS_TAG = "tours";
	static final String REFERENCE_TAG = "reference";
	static final String TYPE_ATTR = "type";
	static final String TYPE_ATTR_VALUE = "toc";	
	static final String REF_TITLE_ATTR = "title";
	static final String REF_TITLE_ATTR_VALUE = Chapter.TOC;

	static final String GUIDE_TAG = "guide";	
	
	static final String ENCODING = "UTF-8";
	
	static final String EXTENSION =  ".opf";
	
	private String projectFile = null;
	
	/**
	 * @throws ParserConfigurationException 
	 * @throws DOMException 
	 * @throws IOException 
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * 
	 */
	public ProjectFile(Story story) throws ParserConfigurationException, DOMException, IOException, TransformerFactoryConfigurationError, TransformerException {
		super();
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "ProjectFile(Story story)");
		this.story = story;
		buildProjectDom();
		writeProjectFile();
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "ProjectFile(Story story)");
	}

	private void writeProjectFile() throws TransformerFactoryConfigurationError, TransformerException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "writeProjectFile()");

		File dir = story.getOutputDir();
		//projectFile = story.toString() + EXTENSION;
		File f = new File(dir, story.toString() + EXTENSION);
		
		projectFile = f.toString();
		logger.info("f: " + projectFile);	
		
	    try {
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(project);

	        // Prepare the output file
	        Result result = new StreamResult(f);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.transform(source, result);
	    } finally{
	    	logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "writeProjectFile()");
	    }
	}

	public String getProjectFile() {
		return projectFile;
	}

	private void buildProjectDom() throws ParserConfigurationException, DOMException, IOException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "buildProjectDom()");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation impl = builder.getDOMImplementation();

		project = impl.createDocument(null, PACKAGE_TAG, null);
		project.setXmlStandalone(true);

		buildMetadata();
		
		buildManifestAndSpine();
		
		buildGuide();

		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "buildProjectDom()");
	}

	/**
	 * @throws DOMException
	 * @throws IOException 
	 */
	protected void buildGuide() throws DOMException, IOException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "buildGuide()");
		
		Element guide = project.createElement(GUIDE_TAG);
		
		if (!story.isOneShot()){
			Element ref = project.createElement(REFERENCE_TAG);
			ref.setAttribute(TYPE_ATTR, TYPE_ATTR_VALUE);
			ref.setAttribute(REF_TITLE_ATTR, REF_TITLE_ATTR_VALUE);
			ref.setAttribute(HREF_ATTR, encodeFilename(story.getContentsFileName()));
			
			guide.appendChild(ref);
		}

		project.getFirstChild().appendChild(guide);
		
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "buildGuide()");		
	}

	/**
	 * @throws DOMException
	 * @throws IOException 
	 */
	protected void buildManifestAndSpine() throws DOMException, IOException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "buildManifestAndSpine()");
		
		Element manifest = project.createElement(MANIFEST_TAG);
		Element spine = project.createElement(SPINE_TAG);
		
		int itemIndex = 1;
		if (!story.isOneShot()){
			String itemID = ID_PREFIX + itemIndex;
			Element item = project.createElement(ITEM_TAG);
			Element itemref = project.createElement(ITEMREF_TAG);
			item.setAttribute(ID_ATTR, itemID);
			itemref.setAttribute(IDREF_ATTR, itemID);
			item.setAttribute(MEDIA_TYPE_ATTR, MEDIA_TYPE_ATTR_VALUE);
			String contents = encodeFilename(story.getContentsFileName());
			item.setAttribute(HREF_ATTR, contents);
			item.setAttribute("properties", "nav");
			
			manifest.appendChild(item);
			spine.appendChild(itemref);
			
			itemIndex++;
		}
		
		String itemID = ID_PREFIX + itemIndex;
		Element item = project.createElement(ITEM_TAG);
		Element itemref = project.createElement(ITEMREF_TAG);
		item.setAttribute(ID_ATTR, itemID);
		itemref.setAttribute(IDREF_ATTR, itemID);
		item.setAttribute(MEDIA_TYPE_ATTR, MEDIA_TYPE_ATTR_VALUE);
		String text = encodeFilename(story + HTMLConstants.HTML_EXTENSION);
		item.setAttribute(HREF_ATTR, text);
		
		manifest.appendChild(item);
		spine.appendChild(itemref);		
		
		Node root = project.getFirstChild();
		root.appendChild(manifest);
		root.appendChild(spine);
		
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "buildManifestAndSpine()");

	}

	String encodeFilename(String filename) throws IOException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "writeProjectFile()");
		
	//	String path = story.getOutputDir().getCanonicalPath() + File.separator + filename;
//		logger.info("path = "+path);
	//	String firstPass = URLEncoder.encode(path, ENCODING);
		String firstPass = URLEncoder.encode(filename, ENCODING);
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < firstPass.length(); i++){
			char c = firstPass.charAt(i);
			if (c == '+'){
				sb.append("%20");
			} else {
				sb.append(c);
			}
		}
		
		String encoded = sb.toString();
		logger.info("encoded = "+encoded);
		
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "encodeFilename(String filename)");
		
		return encoded;
	}

	/**
	 * @throws DOMException
	 */
	protected void buildMetadata() throws DOMException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "buildMetadata()");
		
		Element metadata = project.createElement(METADATA_TAG);
		buildDCMetadata(metadata);
		buildXMetadata(metadata);
		project.getFirstChild().appendChild(metadata);
		
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "buildMetadata()");

	}

	/**
	 * @param metadata
	 * @throws DOMException
	 */
	protected void buildXMetadata(Element metadata) throws DOMException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "initXMetadata(Element metadata)");
		
		Element xMetadata = project.createElement(X_METADATA_TAG);
		

			
		// complete x-metadata
		Element output = project.createElement(OUTPUT_TAG);
		output.setAttribute(ENCODING_ATTR, story.getCharset().name());
		xMetadata.appendChild(output);	
		metadata.appendChild(xMetadata);
		
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "initXMetadata(Element metadata)");		
	}

	/**
	 * @param metadata 
	 * @return
	 * @throws DOMException
	 */
	protected void buildDCMetadata(Element metadata) throws DOMException {
		logger.entering("com.notcomingsoon.getfics.mobi.ProjectFile", "initDCMetadata()");
		
		// dc-metadata
		Element dcMetadata = project.createElement(DC_METADATA_TAG);
		dcMetadata.setAttribute(XMLNS_DC_ATTR, XMLNS_DC_ATTR_VALUE);
		dcMetadata.setAttribute(XMLNS_OEBPACKAGE_ATTR, XMLNS_OEBPACKAGE_ATTR_VALUE);
		Element dcTitle = project.createElement(DC_TITLE_TAG);
		// 20200920 Added timestamp to more easily differentiate between version of story 
		// when looking at story list on Kindle.
		dcTitle.setTextContent(story.getOrigTitle() + story.getTimestamp()); 
		Element dcLang = project.createElement(DC_LANGUAGE_TAG);
		dcLang.setTextContent(DC_LANGUAGE_TAG_VALUE);
		Element dcCreator = project.createElement(DC_CREATOR_TAG);
		dcCreator.setTextContent(story.getDelimitedAuthor());
		dcMetadata.appendChild(dcTitle);
		dcMetadata.appendChild(dcLang);
		dcMetadata.appendChild(dcCreator);
		
		metadata.appendChild(dcMetadata);
		
		logger.exiting("com.notcomingsoon.getfics.mobi.ProjectFile", "initDCMetadata()");

	}
	
	
}
