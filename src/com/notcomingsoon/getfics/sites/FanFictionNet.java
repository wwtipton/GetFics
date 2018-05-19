/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.notcomingsoon.getfics.Chapter;
import com.notcomingsoon.getfics.HTMLConstants;

/**
 * @author Winifred Tipton
 *
 */
public class FanFictionNet extends Site {
	
	private static final String A2A_KIT_A2A_DEFAULT_STYLE = "a2a_kit a2a_default_style";
	private static final Charset FFN_CHARSET = HTMLConstants.UTF_8;
	private static final int AUTHOR_ANCHOR = 0;
	private static final int CHAPTER_SELECT = 1;
	private static final String STORYTEXT = "storytext";
	private static final int CHAPTER_BODY = 0;
	private static final int SUMMARY = 6;
	

	/**
	 * @param ficUrl
	 */
	public FanFictionNet(String ficUrl) {
		super(ficUrl);

		siteCharset = FFN_CHARSET;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		
		ArrayList<Chapter> list = new ArrayList<Chapter>();
		
		Elements options = getChapterOptions(doc);
		
		if (options != null){
			
			String urlSuffix = null;
			String urlPrefix = null;
			int slashIndex = startUrl.lastIndexOf(HTMLConstants.URL_DIVIDER);
			urlSuffix = startUrl.substring(slashIndex);
	
			slashIndex = startUrl.lastIndexOf(HTMLConstants.URL_DIVIDER, slashIndex-1);
			urlPrefix = startUrl.substring(0, slashIndex+1);
			logger.info("urlPrefix = " + urlPrefix); 
			logger.info("urlSuffix = " + urlSuffix); 
	
			ListIterator<Element> lIter = options.listIterator();
			while (lIter.hasNext()){
				Element option = lIter.next();
				String title = option.text().trim();
				String cUrl = option.attr(HTMLConstants.VALUE_ATTR);
				cUrl = urlPrefix + cUrl + urlSuffix;
				Chapter c = new Chapter(cUrl, title);
				list.add(c);
			}
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "getChapterList(Document doc");
		return list;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected Elements getChapterOptions(Document doc) {
		Elements options = null;
		
		Elements selects = doc.getElementsByAttributeValue("title","chapter navigation");
		if (!selects.isEmpty()){
			Element select = selects.get(CHAPTER_SELECT);
			options = select.getElementsByTag(HTMLConstants.OPTION_TAG);
		}
		return options;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		
		Element div = doc.getElementById("profile_top");
		Elements as = div.getElementsByTag(HTMLConstants.A_TAG);
		
		String author = as.get(AUTHOR_ANCHOR).text();
		
		logger.exiting(this.getClass().getCanonicalName(), "getAuthor(Document doc)");
		return author;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		logger.entering(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		
		Elements bs = doc.getElementsByTag(HTMLConstants.B_TAG);
		String title = bs.get(5).text();
		logger.info("title = " + title);
		logger.exiting(this.getClass().getCanonicalName(), "getTitle(Document doc)");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected Document extractChapter(Document story, Document chapter,
			Chapter title) {
		logger.entering(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		
		Element body = addChapterHeader(story, title);
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.ID_ATTR, STORYTEXT);
		Element div = divs.get(CHAPTER_BODY);
		div.removeAttr(HTMLConstants.ID_ATTR);
		Elements subDivs = div.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, A2A_KIT_A2A_DEFAULT_STYLE);
		if (subDivs.size() > 0){
			Element subDiv = subDivs.first();
			subDiv.remove();
		}
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractChapter(Document doc)");
		return story;
	}

	@Override
	protected Chapter extractSummary(Document story, Document chapter) {
		logger.entering(this.getClass().getCanonicalName(), "extractSummary");
		
		Chapter title = new Chapter(this.startUrl, SUMMARY_STRING);
		Element body = addChapterHeader(story, title);
		
		Elements divs = chapter.getElementsByAttributeValue(HTMLConstants.CLASS_ATTR, "xcontrast_txt");
		Element div = divs.get(SUMMARY);
		
		body.appendChild(div);
		
		addChapterFooter(body);
		
		logger.exiting(this.getClass().getCanonicalName(), "extractSummary");
		return title;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document doc) {
		Elements options = getChapterOptions(doc);
		boolean isOneShot = false;
		if (options == null){
			isOneShot = true;
		}
		
		return isOneShot;
	}

	static public boolean isFFN(String url){
		boolean retVal = false;
		if (url.contains(FFN)){
			retVal = true;
		}
		
		return retVal;
	}

}
