package com.bondevans.chordinator;

import android.os.Environment;

import java.io.File;

public class Statics {
    public static final String CHORDINATOR = File.separator+"chordinator"+File.separator;
    public static final String CHORDINATOR_DIR = Environment.getExternalStorageDirectory().getPath()+CHORDINATOR;
	public static final String SONGFILEEXT = ".csf";
	public static final String TEXTFILEEXT = ".txt";

}
