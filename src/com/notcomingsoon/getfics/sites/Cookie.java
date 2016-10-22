/**
 * 
 */
package com.notcomingsoon.getfics.sites;

/**
 * @author Winifred Tipton
 *
 */
public class Cookie {
	
	private String name=null;
	
	private String value = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
	Cookie(String n, String v){
		this.name = n;
		this.value = v;
	}

}
