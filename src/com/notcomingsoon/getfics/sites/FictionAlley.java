/**
 * 
 */
package com.notcomingsoon.getfics.sites;

import java.util.ArrayList;

import org.jsoup.nodes.Document;

import com.notcomingsoon.getfics.Chapter;

/**
 * @author Winifred
 *
 */
public class FictionAlley extends Site {

	/**
	 * @param ficUrl
	 */
	public FictionAlley(String ficUrl) {
		super(ficUrl);
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getChapterList(org.jsoup.nodes.Document)
	 */
	@Override
	protected ArrayList<Chapter> getChapterList(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getAuthor(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getAuthor(Document doc) {
		
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#getTitle(org.jsoup.nodes.Document)
	 */
	@Override
	protected String getTitle(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#extractChapter(org.jsoup.nodes.Document, org.jsoup.nodes.Document, com.notcomingsoon.getfics.Chapter)
	 */
	@Override
	protected Document extractChapter(Document story, Document chapter, Chapter title) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.notcomingsoon.getfics.sites.Site#isOneShot(org.jsoup.nodes.Document)
	 */
	@Override
	protected boolean isOneShot(Document doc) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean isFictionAlley(String url) {
		boolean retVal = false;
		if (url.contains(FICTION_ALLEY)){
			retVal = true;
		}
		
		return retVal;
	}

}
