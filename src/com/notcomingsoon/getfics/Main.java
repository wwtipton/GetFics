/**
 * 
 */
package com.notcomingsoon.getfics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.notcomingsoon.getfics.mobi.ProjectFile;
import com.notcomingsoon.getfics.sites.Site;

/**
 * @author Winifred Tipton
 *
 */
public class Main {
	
	ArrayList<String> ficList = new ArrayList();
	
	private static String ficListFileName;
	
	private Logger logger = GFLogger.getLogger();

	private String mobigenPath;

	private String publishDirectory;

	private String mobiExtension;

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
		mobigenPath = GFProperties.getPropertyValue(GFProperties.MOBIGEN_PATH_KEY);
		System.out.println("mobigenPath="+mobigenPath);
		publishDirectory = GFProperties.getPropertyValue(GFProperties.PUBLISH_DIRECTORY_KEY);
		mobiExtension = GFProperties.getPropertyValue(GFProperties.MOBI_EXTENSION_KEY);
		
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
		
		Iterator ficIter = ficList.iterator();
		
		HashMap<String, ArrayList<String>> imageFailures = new HashMap<String, ArrayList<String>>();
		try {
			while (ficIter.hasNext())
			{
				String ficURL = (String) ficIter.next();
				logger.warning(GFLogger.NEW_LINE + GFLogger.NEW_LINE + "Starting: " + ficURL);
				Story story = Site.getStory(ficURL);

				if (null != story) {
					imageFailures.put(story.toString(), story.getImageFailures());
				}
				
				if (story != null && mobigenPath != null){
					ProjectFile projectFile = new ProjectFile(story);
					buildMobi(projectFile, story);
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

	private void buildMobi(ProjectFile projectFile, Story story) throws IOException, InterruptedException {
		logger.entering(this.getClass().getCanonicalName(), "buildMobi(ProjectFile projectFile, Story story)");	
		
		String mobiName = story.toString() + this.mobiExtension;
		
	//	ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", mobigenPath, projectFile.getProjectFile(),"-verbose", "-o", mobiName);
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", mobigenPath, projectFile.getProjectFile(),"-o", mobiName);
		pb.redirectErrorStream(true);
		logger.info("Command: " + pb.command());
		Process process = pb.start();
			
		InputStream is = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    while ((line = br.readLine()) != null) {
	      System.out.println(line);
	    }

		int exitCode = process.waitFor();
		logger.info("exitCode = " + exitCode);
		
		if (exitCode == 0 || exitCode == 1){
			moveMobi(mobiName, story);
		}

		logger.exiting(this.getClass().getCanonicalName(), "buildMobi(ProjectFile projectFile, Story story)");		
	}

	private void moveMobi(String mobiName, Story story) throws IOException {
		logger.entering(this.getClass().getCanonicalName(), "moveMobi(String mobiName)");
		
		File mobi = new File(story.getOutputDir(), mobiName);
		
		File publish = new File(publishDirectory, mobiName);
		publish.createNewFile();
		logger.info("publish = " + publish.getCanonicalPath());
		
		if (mobi.exists() && publish.exists()){
			FileInputStream fis = new FileInputStream(mobi);
			FileOutputStream fos = new FileOutputStream(publish);
			
			while (fis.available() > 0){
				int i = fis.read();
				fos.write(i);
			}
		}
		
		logger.exiting(this.getClass().getCanonicalName(), "moveMobi(String mobiName)");
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
