package com.bondevans.chordinator;

public class Log {
	static public void d(String tag, String text){
		android.util.Log.d(tag, text);
	}
	static public void e(String tag, String text){
		android.util.Log.e(tag,text);
	}
	public static void L(String tag, String name, String value){
		android.util.Log.d(tag, "HELLO-"+name+"["+value+"]");;
	}

}