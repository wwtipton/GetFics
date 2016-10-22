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
	//static String propFilePath = "Z:\\WD.Share\\WWT\\GetFics\\getfics.properties";
	//static String propFilePath = "F:\\ioSafe\\WD.Share\\WWT\\GetFics\\getfics.properties";
    static String propFilePath = "c:\\GetFics\\getfics.properties";
	public static final String FIC_LIST_FILE_KEY = "fic.list";
	public static final String LOG_FILE_KEY = "log.file";
	public static final String OUTPUT_DIRECTORY_KEY = "output.directory";
	public static final String MOBIGEN_PATH_KEY = "mobigen.path";
	public static final String PUBLISH_DIRECTORY_KEY = "publish.directory";
	public static final String MOBI_EXTENSION_KEY = "mobi.extension";	
	
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
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}	

	public static String getPropertyValue(String key){
		if (props.isEmpty()){
			readProperties(props);
		}
		
		return props.getProperty(key, null);
	}
}
