/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred
 *
 */
public class SSHGExchange extends Site {
	
	private static final String AUTHOR_KEY = "Author";
	private static final String TITLE_KEY = "Title";
	private static final String BODYWRAPPER = "b-singlepost-bodywrapper";
	private static final String ENTRY_CONTENT = "entry-content ";
	private static final Charset LJ_CHARSET = HTMLConstants.UTF_8;
	
	//private Chapter summary = null;
	
	Connection conn;

	static{
		try {
			URI U = new URI(LJ);
			addCookie(U,"adult_explicit", "1");
//			addCookie(U,"path", "/");
	//		addCookie(U,"domain", ".livejournal.com"):
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private ArrayList<TextNode> allTextNodes = new ArrayList<TextNode>();
	
	private ArrayList<Chapter> chapters = new ArrayList<Chapter>();

	

	/**
	 * @param ficUrl
	 * @throws IOException 
	 */
	public SSHGExchange(String ficUrl) throws IOException {
		super(ficUrl);
		siteCharset = LJ_CHARSET;
	}

	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		
		if (chapters.isEmpty()) {
			Elements links = doc.getElementsByAttributeValueContaining(HTMLConstants.HREF_ATTR, "sshg");
			
			for (Element link : links) {
				Chapter c = new Chapter(link.attr(HTMLConstants.HREF_ATTR), link.text());
				chapters.add(c);
				link.remove();
			}
		}
		
		return chapters;
		
	}

	@Override
	protected String getAuthor(Document doc) {
		
		String author = getTextValue(doc, AUTHOR_KEY);
		
		return author;
	}

	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getSimpleName(), "getTitle(Document doc)");
		
		String title = getTextValue(doc, TITLE_KEY);
		
		return title;
	}
	
	protected String getTextValue(Document doc, String key) {
		String text = "";
		
		if (allTextNodes.isEmpty()) {
			allTextNodes = (ArrayList<TextNode>) gatherTextNodes(doc);
		}
		
		for (int i = 1; i < allTextNodes.size(); i++) {
			TextNode node = allTextNodes.get(i);
			if (node.text().startsWith(key)) {
			// we want the next non-blank node
				for (int j = i+1; j < allTextNodes.size(); j++) {
					TextNode next = allTextNodes.get(j);
					String nextText = next.text().trim();
					if (nextText.length() >0) {
						text = nextText;
						break;
					}
				}
				break;
			}
		}
		
		return text;
	}
	

	private List<TextNode> gatherTextNodes(Node inNode) {
		ArrayList<TextNode> textNodes = new ArrayList<TextNode>();
		
		List<Node> children = inNode.childNodes();
		
		for (Node child : children) {
			if (child.getClass().equals(TextNode.class)) {
				textNodes.add((TextNode)child);
				continue;
			}
			textNodes.addAll(gatherTextNodes(child));
		}
		
		return textNodes;
	}

	@Override
	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		logger.entering(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		
		if (!isOneShot(chapter)){//One shot body included with summary
			Element body = addChapterHeader(story, title);
			revealWarnings(chapter);
			Element chapterText = null;
			chapterText = chapter.body();
			Elements links = chapterText.getElementsByTag(HTMLConstants.A_TAG);
			links.remove();
			Elements divs = chapterText.getElementsByAttributeValueMatching(HTMLConstants.CLASS_ATTR, "ljtags");
			divs.remove();
			body.appendChild(chapterText);
			addChapterFooter(body);
		}

		
		logger.exiting(this.getClass().getSimpleName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected boolean isOneShot(Document doc) {
		boolean isOneShot = true;
		
		if (getChapterList(doc).size() > 0) {
			isOneShot = false;
		}

		return isOneShot;
	}
	
	@Override
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getSimpleName(), "extractSummary");
		
		Chapter summary = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, summary);
		
		//Supposed to remove all the malarkey about lj users
		Elements img = chapter.getElementsByTag(HTMLConstants.IMG_TAG);
		img.remove();
		revealWarnings(chapter);
		Elements wbr = chapter.getElementsByTag("wbr");
		wbr.remove();
		
		body.appendChild(chapter.body());
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getSimpleName(), "extractSummary");
		return summary;
	}

	void revealWarnings(Document chapter) {
		Elements hidden = chapter.getElementsByAttributeValueContaining("style", "color");
		for (Element e : hidden) {
			e.attr("style", "");
		}
	}

	public static boolean isSSHGExchange(String url) {
		boolean retVal = false;
		if (url.contains(SSHG_EXCHANGE)){
			retVal = true;
		}
		
		return retVal;
	}
	
	@Override
	Document getPage(String url) throws Exception {
		logger.entering(this.getClass().getSimpleName(), "getPage(String url)");
		
		String localUrl = url;
		Document doc = super.getPage(localUrl);
		Document local = Document.createShell(localUrl);


		Elements divs = doc.getElementsByClass(BODYWRAPPER);
		if (divs.isEmpty()) {
			divs = doc.getElementsByClass(ENTRY_CONTENT);
		}
			
		local.body().appendChild(divs.first());
		
		logger.exiting(this.getClass().getSimpleName(), "getPage(String url)");
		return local;
	}

}
