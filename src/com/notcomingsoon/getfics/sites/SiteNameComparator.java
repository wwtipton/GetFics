package com.notcomingsoon.getfics.sites;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Winifred Tipton
 *
 */
public class SiteNameComparator implements Comparator, Serializable{

	@Override
	public int compare(Object arg0, Object arg1) {
		int result = 0;
		String s0 = (String) arg0;
		String s1 = (String) arg1;
		if(s0.length() < s1.length()){
			result = 1;
		} else {
			if (s1.length() < s0.length()){
				result = -1;
			} else {
				result = 0;
			}
		}

		return result;
	}
	
}