package com.bondevans.chordinator;

import java.io.Serializable;
import java.util.Comparator;

public class FileNameComparator implements Comparator<Object>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2061477481464170907L;

	@Override
	public int compare(Object object1, Object object2) {
		String str1 = (String)object1;
		String str2 = (String)object2;
		return str1.compareToIgnoreCase(str2);
	}

}
