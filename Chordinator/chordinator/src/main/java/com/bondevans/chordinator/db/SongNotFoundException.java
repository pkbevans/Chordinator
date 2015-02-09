package com.bondevans.chordinator.db;

import com.bondevans.chordinator.ChordinatorException;

public class SongNotFoundException extends ChordinatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6224827270157696790L;

	public SongNotFoundException(String message) {
		super(message);
	}
}
