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
	/**
	 * 
	 */
	private static final long serialVersionUID = -5899234284994299244L;
	//TODO Put in root of project so we don't need to define
    static String propFilePath = "E:\\CodingProjects\\GetFics\\getfics.properties";
	public static final String FIC_LIST_FILE_KEY = "fic.list";
	public static final String LOG_FILE_KEY = "log.file";
	public static final String INPUT_DIRECTORY_KEY = "input.directory";
	public static final String OUTPUT_ROOT_DIRECTORY_KEY = "output.root.directory";
	public static final String PUBLISH_DIRECTORY_KEY = "publish.directory";
	public static final String DEVICE_DIRECTORY_KEY = "device.directory";
	public static final String REJECTS_DIRECTORY_KEY = "rejects.directory";
	public static final String PUBLISH_ERROR_DIR_KEY = "publish.error.directory";
	public static final String EPUB_SUBDIRECTORY_ROOT_KEY = "epub.subdirectory.root";
	public static final String EPUB_EXTENSION_KEY = "epub.extension";

	
	// Site
	public static final String USER_AGENT = "user.agent";
	public static final String SUMMARY = "summary";
	public static final String NOTES_FOOTER = "notes.footer";
	public static final String TAGS = "tags";
	
	// Archive of Our Owns
	public static final String AO3_PEN_NAME = "ao3.pen.name";
	public static final String AO3_PASSWORD = "ao3.password";
	
	// SquidgeWorld
	public static final String SQUIDGE_PEN_NAME = "squidge.pen.name";
	public static final String SQUIDGE_PASSWORD = "squidge.password";
	
	// Hunting Horcruxes
	public static final String HUNTING_HORCRUXES_PEN_NAME = "hunting.horcruxes.pen.name";
	public static final String HUNTING_HORCRUXES_PASSWORD = "hunting.horcruxes.password";
	
	// The Petulant Poetess
	public static final String TPP_PEN_NAME = "tpp.pen.name";
	public static final String TPP_USER_NAME = "tpp.user.name";
	public static final String TPP_PASSWORD = "tpp.password";
	
	// Ashwinder Sycophantex
	public static final String SYCOPHANTEX_PEN_NAME = "sycophantex.pen.name";
	
	// Twisting the Hellmouth
	public static final String TTH_PEN_NAME = "tth.pen.name";
	public static final String TTH_PASSWORD = "tth.password";
	
	
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
