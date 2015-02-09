package com.bondevans.chordinator;

import java.io.File;

import android.os.Environment;

public class Statics {
	public static final String CHORDINATOR_DIR = Environment.getExternalStorageDirectory().getPath()+File.separator+"chordinator"+File.separator;
	public static final String SONGFILEEXT = ".csf";
	public static final String TEXTFILEEXT = ".txt";

}
