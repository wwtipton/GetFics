package com.notcomingsoon.getfics;

import java.nio.charset.Charset;
import java.util.SortedMap;

public class HTMLConstants {
	private static SortedMap charsets = Charset.availableCharsets();
	
	public static final Charset ISO_8859_1 = (Charset) charsets.get("ISO-8859-1");
	public static final Charset UTF_8 = (Charset) charsets.get("UTF-8");
	public static final Charset WIN_1252 = (Charset) charsets.get("windows-1252");
	public static final Charset UTF_16 = (Charset) charsets.get("UTF-16");
	
	public static final String HTML_EXTENSION = ".html";

	public static final String FORM_TAG = "form";

	public static final String TD_TAG = "td";

	public static final String HTML_TAG = "html";
	public static final String BODY_TAG = "body";

	public static final String TABLE_TAG = "table";

	public static final String TR_TAG = "tr";

	public static final String A_TAG = "a";

	public static final String OPTION_TAG = "option";

	public static final String VALUE_ATTR = "value";

	public static final String SELECT_TAG = "select";

	public static final String HREF_ATTR = "href";

	public static final String H2_TAG = "h2";
	
	public static final String DL_TAG = "dl";

	public static final String DD_TAG = "dd";

	public static final String SELECTED_ATTR = "selected";

	public static final String SPAN_TAG = "span";
	
	public static final String HR_TAG = "hr";

	public static final String NAME_ATTR = "name";

	public static final String COLSPAN_ATTR = "colspan";
	
	public static final String BGCOLOR_ATTR = "bgcolor";

	public static final String HEAD_TAG = "head";

	public static final String IMG_TAG = "img";

	public static final String DIV_TAG = "div";

	public static final String B_TAG = "b";

	public static final String ID_ATTR = "id";

	public static final String CLASS_ATTR = "class";

	public static final String SRC_ATTR = "src";

	public static final CharSequence HTTP = "http://";

	public static final String UL_TAG = "ul";
	
	public static final String SEPARATOR = "/";
	
	public static final String TARGET = "#";

	public static final String NAV_TAG = "nav";

	public static final String OL_TAG = "ol";

	public static final String LI_TAG = "li";

	public static final String P_TAG = "p";

	public static final CharSequence HTTPS = "https://";

	public static final String URL_DIVIDER = "/";

	public static final String FRAME_TAG = "frame";

	public static final String FONT_TAG = "font";
	
	public static final String AMPERSAND = "&";

	public static final String TITLE_TAG = "title";

	public static final String BLOCKQUOTE_TAG = "blockquote";

	public static final String CENTER_TAG = "center";


	
}