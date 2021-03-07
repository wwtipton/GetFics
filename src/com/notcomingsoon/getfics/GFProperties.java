/**
 * 
 */
package com.notcomingsoon.getfics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Winifred Tipton
 *
 */
public class GFProperties extends Properties {
	//TODO Put in root of project so we don't need to define
    static String propFilePath = "C:\\CodingProjects\\GetFics\\getfics.properties";
	public static final String FIC_LIST_FILE_KEY = "fic.list";
	public static final String LOG_FILE_KEY = "log.file";
	public static final String INPUT_DIRECTORY_KEY = "input.directory";
	public static final String OUTPUT_DIRECTORY_KEY = "output.directory";
	public static final String MOBIGEN_PATH_KEY = "mobigen.path";
	public static final String PUBLISH_DIRECTORY_KEY = "publish.directory";
	public static final String MOBI_EXTENSION_KEY = "mobi.extension";	
	
	// Site
	public static final String USER_AGENT = "user.agent";
	public static final String SUMMARY = "summary";
	public static final String NOTES_FOOTER = "notes.footer";
	
	// Archive of Our Owns
	public static final String AO3_PEN_NAME = "ao3.pen.name";
	public static final String AO3_PASSWORD = "ao3.password";
	
	// Hunting Horcruxes
	public static final String HUNTING_HORCRUXES_PEN_NAME = "hunting.horcruxes.pen.name";
	public static final String HUNTING_HORCRUXES_PASSWORD = "hunting.horcruxes.password";
	
	// The Petulant Poetess
	public static final String TPP_PEN_NAME = "tpp.pen.name";
	
	// Ashwinder Sycophantex
	public static final String SYCOPHANTEX_PEN_NAME = "sycophantex.pen.name";
	
	
	static GFProperties props = new GFProperties();
	
	
	public static void readProperties(GFProperties gfp)
{
	File f = new File(propFilePath);
	
	FileReader fr;
	try {
		fr = new FileReader(f);
		gfp.load(fr);
		fr.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
}	

	public static String getPropertyValue(String key){
		if (props.isEmpty()){
			readProperties(props);
		}
		
		return props.getProperty(key, null);
	}

	public static String getString(String key) {
		// TODO Auto-generated method stub
		return getPropertyValue(key);
	}
}
