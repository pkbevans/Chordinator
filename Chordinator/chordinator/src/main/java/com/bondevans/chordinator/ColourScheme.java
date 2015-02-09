package com.bondevans.chordinator;

import android.graphics.Color;

/**
 *
 * @author Paul
 */
public class ColourScheme {
	public final static int LIGHT = 0;
	public final static int DARK = 1;
	private int background = Color.WHITE;
	private int title = Color.BLUE;
	private int freeText = title;
	private int tab = Color.GREEN;
	private int chords = Color.RED;
	private int lyrics = Color.BLACK;
	private int grids = lyrics;
	private int arrow = Color.BLUE;
	private int timer = Color.GRAY;
	/**
	 * Constructor takes scheme number - 0=Daytime (Default), 1=Nightime
	 * @param scheme
	 */
	public ColourScheme(int scheme){
		if(scheme == DARK){
			// Nighttime colour scheme
			background = Color.BLACK;
			title = Color.RED;
			freeText = title;
			tab = title;
			chords = Color.YELLOW;
			lyrics = Color.WHITE;
			grids = lyrics;
			arrow = Color.BLUE;
			timer = Color.GRAY;
		}
	}
	public int background(){return background;}
	int title (){return title;}
	int freeText (){return freeText;}
	int tab (){return tab;}
	int chords (){return chords;}
	int lyrics (){return lyrics;}
	int grids (){return grids;}
	int arrow (){return arrow;}
	int timer (){return timer;}
}

