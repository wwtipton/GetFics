/**
 * 
 */
package com.notcomingsoon.getfics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.notcomingsoon.getfics.files.Epub;
import com.notcomingsoon.getfics.sites.Site;

/**
 * @author Winifred Tipton
 *
 */
public class Main {
	
	ArrayList<String> ficList = new ArrayList<String>();
	
	private static String ficListFileName;
	
	private Logger logger = GFLogger.getLogger();

//	private String publishDirectory;

	ArrayList<String> failures = new ArrayList<String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	throws Exception
	{
		Main m = new Main();
		
		try {
			m.doAll();
		} catch (Exception e){
			System.out.print(e);
		}
		
		Runtime.getRuntime().exit(0);
	}
	
	public void doAll()
	throws Exception
	{
		logger.entering(this.getClass().getCanonicalName(), "doAll()");
		ficListFileName = GFProperties.getPropertyValue(GFProperties.FIC_LIST_FILE_KEY);
		//publishDirectory = GFProperties.getPropertyValue(GFProperties.PUBLISH_DIRECTORY_KEY);
		
		if (ficListFileName.length() > 0)
		{
			CookieManager cm = new CookieManager();
			CookieHandler.setDefault(cm);
			
			readFicList();
			getFics();
		}

		logger.exiting(this.getClass().getCanonicalName(), "doAll()");
	}

	private void getFics() throws Exception 
	{
		logger.entering(this.getClass().getCanonicalName(), "getFics()");
		
		Iterator<String> ficIter = ficList.iterator();
		
		HashMap<String, ArrayList<String>> imageFailures = new HashMap<String, ArrayList<String>>();
		try {
			while (ficIter.hasNext())
			{
				String ficURL = (String) ficIter.next();
				logger.warning(GFLogger.NEW_LINE + GFLogger.NEW_LINE + "Starting: " + ficURL);
				Epub epub = Site.getEpub(ficURL);

				if (null != epub) {
					epub.build();
					epub.writeEpub();
					int code = epub.validate();
					if (code != 0) {
						failures.add(ficURL);
						epub.publishFailure();
					} else {
						epub.publish();
					}
				}
				
				if (null != epub) {
					imageFailures.put(epub.getOpf().getUniqueId(), epub.getImageFailures());
				}
				
				logger.warning("Done: " + ficURL);
			}
		} catch (Exception e){				
		//	System.out.println(e);
			e.printStackTrace(System.out);
		}			

		for (String k : imageFailures.keySet()) {
			ArrayList<String> failures = imageFailures.get(k);
			for (int i = 0; i < failures.size(); i++) {
				if (i == 0) {
					logger.warning(GFLogger.NEW_LINE + GFLogger.NEW_LINE + k + " had at least one picture failure.");
				}
				logger.warning(failures.get(i));
			}
		}
		logger.exiting(this.getClass().getCanonicalName(), "getFics()");
	}

	private void readFicList() 
	throws Exception
	{
		logger.entering(this.getClass().getCanonicalName(), "readFicList()");
		BufferedReader in
		   = new BufferedReader(new FileReader(ficListFileName));

		
		while (in.ready())
		{
			String fic = in.readLine();
			if (!fic.startsWith("#")) {
				ficList.add(fic);
			}
		}
		
		in.close();
		logger.exiting(this.getClass().getCanonicalName(), "readFicList()");
	}



}
