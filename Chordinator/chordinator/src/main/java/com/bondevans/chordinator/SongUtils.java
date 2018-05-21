package com.bondevans.chordinator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import android.widget.Toast;
/**
 * Class representing various Utility functions
 * @author Paul
 */
public class SongUtils {
	public static final int	SETOPTIONS_REQUEST = 1;
	static final int	EDITSONG_REQUEST = 2;
    static final int	SONGACTIVITY_REQUEST = 5;
	private static final String TAG = "SongUtils";
	private final static int FILE_TOO_BIG = 15000;

	/**
	 * Empty Constructor
	 */
	public SongUtils(){
	}

	/**
	 * Checks whether given char is in given String
	 *
	 * @param x Char to search for
	 * @param y String to search in
	 * @return POsition of x in y
	 */
	public static boolean charIsInString(char x, String y) {
		boolean ret = false;
		for (int i = 0; i < y.length(); i++) {
			if (x == y.charAt(i)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * Extracts value of tag from text
	 * @param text Text to search
	 * @param tag Tag
	 * @return Value of tag
	 */
	static String tagValue(String text, String tag)
	{
		int x, y;
		String myTag = "{"+tag+":";
		String ret = "";
		if( (x = text.indexOf(myTag)+myTag.length())>=myTag.length() &&
				(y = text.indexOf('}', x)) >=0 ){
			ret = text.substring(x, y);
			//				Log.d(TAG,tag + "=[" + ret + "]");
		}
		return ret.trim();
	}

	public static String tagValue( String text, String tag1, String tag2, String defaultRet  )
	{
//		Log.d(TAG, "HELLO - tagvalue ["+text+"]["+tag1+"]["+tag2+"]");
		// Try get tag1. If not found try tag2, if this isn't found return default
		String ret=tagValue(text, tag1);
		if(ret.equals(""))
		{
			ret=tagValue(text, tag2);
			if(ret.equals(""))
			{
				ret = defaultRet;
			}
		}
		//		Log.d(TAG,tag1 + "/" + tag2 + "=[" + ret + "]");
		return ret;
	}

	/**
	 * Gets value of tag -  ensuring that the value returned is from the first tag in the String
	 * @param text Text
	 * @param tag1 Tag
	 * @param tag2 Tag
	 * @param defaultRet Default if not found
	 * @return Returns value of tag
	 */
	public static String tagValueX(String text, String tag1, String tag2, String defaultRet ){
		// Get end of tag
		int end;
		end=text.indexOf("}");
		if( end != -1){
			return tagValue(text.substring(0, end+1), tag1, tag2, defaultRet);
		}
		else{
			return defaultRet;
		}
	}

	/**
	 * LoadFile - Load a complete song file into Contents
	 */
	public static String loadFile(String filePath, FileDescriptor fileDescriptor, String defaultEncoding) throws ChordinatorException{
		StringBuilder sb = new StringBuilder();
		try
		{
			int length = 2048, bytesRead=0;
			byte[] buffer = new byte[length];

			BufferedInputStream buf;
			if(fileDescriptor!=null){
				buf = new BufferedInputStream(new FileInputStream(fileDescriptor));
			}else {
				buf = new BufferedInputStream(new FileInputStream(filePath), length);
			}
			length=2048;
			int i=0;
			String enc = "";
			while( bytesRead>=0){
				if((bytesRead = buf.read(buffer, 0, length))>=0 ){
					// Have a look at the 1st 3 bytes. If these indicate UTF-8 then use UTF-8.
					// Chordinator will always save in UTF-8
					int offset=0;
					if(i==0){
						++i;
						if( isUTF8(buffer)){
							// Ignore these three bytes and don't need to get file encoding
							enc = "UTF-8";
							offset=3;
						}
						else if( isUTF16LE(buffer)){
							// Ignore 2 bytes and don't need to get file encoding
							enc = "UTF-16LE";
							offset=2;
						}
						else if( isUTF16BE(buffer)){
							// Ignore 2 bytes and don't need to get file encoding
							enc = "UTF-16BE";
							offset=2;
						}
						else{
							enc = getFileEncoding(buffer, defaultEncoding);
						}
					}
					// Store the buffer in the buffer array - unless its the 3 byte UTF-8 header
					sb.append(new String(buffer, offset, bytesRead-offset, enc));
				}
			}
			buf.close();
			//			String x = sb.toString();
			//			byte []y = x.getBytes();
			//			String z = new String(y);
			//			String z2 = new String (x.getBytes("UTF-8"));
			//			Log.d(TAG,"Got String:["+sb.toString()+"]");
			return new String(sb.toString().getBytes("UTF-8"));
		}
		catch (Exception e) {
			Log.d(TAG,e.getMessage());
			throw new ChordinatorException("ERROR opening file: "+ e.getMessage());
		}
	}

	public final static void writeFile( String fileName, String contents) throws ChordinatorException{
		Log.d(TAG,"Writing: " + fileName);
		byte [] utfHeader = {(byte)0xef,(byte)0xbb,(byte)0xbf};
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
			byte [] utf8 =contents.getBytes("UTF-8");		
			// Don't add the UTF-8 header if its already there
			if(!isUTF8(utf8)){
				// Always write out in UTF-8 adding 3 byte header
				out.write(utfHeader);
			}
			out.write(utf8);		
			out.flush();
			out.close();
		} 
		catch (IOException e) {
			Log.e(TAG, "WriteFile failed: "+e.getMessage());
			e.printStackTrace();
			throw new ChordinatorException("An error has ocurred writing: ["+fileName+"]:"+ e.getMessage());
		}
	}
	public static void toast(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	private static boolean isUTF8( byte[] buffer){
		// FIX 29/11/11 - Force close on creation of new set
        //			Log.d(TAG, "IS UTF8");
//			Log.d(TAG, "NOT UTF8");
        return buffer.length >= 3 && buffer[0] == (byte) 0xef && buffer[1] == (byte) 0xbb && buffer[2] == (byte) 0xbf;
	}
	private static boolean isUTF16LE( byte[] buffer){
		// FIX 29/11/11 - Force close on creation of new set
		if (buffer.length>=2 && buffer[0] == (byte) 0xff && buffer[1] == (byte) 0xfe ){
//			Log.d(TAG, "IS UTF16-LE");
			return true;
		}
//		Log.d(TAG, "NOT UTF16-LE");
		return false;
	}
	private static boolean isUTF16BE( byte[] buffer){
		// FIX 29/11/11 - Force close on creation of new set
		if (buffer.length>=2 && buffer[0] == (byte) 0xfe && buffer[1] == (byte) 0xff ){
//			Log.d(TAG, "IS UTF16-BE");
			return true;
		}
//		Log.d(TAG, "NOT UTF16-LE");
		return false;
	}
	public static String getFileEncoding(byte [] buffer, String defEncoding)
	{

		String enc;

		// IF default encoding specified use it other wise default is ISO-8859-1
		// However use UTF 8 if we identify the UTF header
		if(defEncoding.length()>0){
			enc = defEncoding;
		}
		else{
			enc = "iso-8859-1";
		}
		// else use default
//		Log.d(TAG, "Using "+enc+" encoding");
		return enc;
	}
	static boolean isBanned(String fileName){
		return isBanned(new File(fileName));
	}
	static boolean isBanned(File file) {
        // Check Size 1st
        return file.length() > FILE_TOO_BIG || file.getName().startsWith(".") || isBannedFileType(file.getName());
    }
	@SuppressLint("DefaultLocale")
	public static boolean isBannedFileType(String fileName){
		// Suffixs MUST be UPPERCASE!!!
		String [] banned = {"JPG","MP3","MOV","ZIP","PDF","DB","JPEG","PNG",
				"3GP","HTML","HTM","DOC", "APK", "M4A","OGG","LOG","TMP","XML"};// TODO add more 
		String file=fileName.toUpperCase();
		for( String suffix:banned){
			if(file.endsWith("."+suffix)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Changes the file suffix of a file path
	 * @param path Path
	 * @param suffix Suffix
	 * @return Returns new path
	 */
	public static String swapSuffix(String path, String suffix){
		String ret;
		// find the last dot and chop off
		char[] x = path.toCharArray();
		int i = x.length-1;
		while(i>=0){
			if(x[i]=='.'){
				break;
			}
			--i;
		}
		if(i<0){
			// Didn't find a suffix so just add newSuffix on the end of path
			ret = path+suffix;
		}
		else{
			// found a suffix so swap if for new one
			ret = path.substring(0, i)+suffix;
		}
//		Log.d(TAG, "HELLO - swapSuffix ["+ret+"]");
		return ret;
	}

	public static void getLog(){
		getLog(Environment.getExternalStorageDirectory()+"/chordinator.log");
	}
	public static void getLog(String fileName){
		File log = new File (fileName);
		try {
			log.createNewFile();
			String cmd = "logcat -d -f"+log.getAbsolutePath();
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// IGNORE
			e.printStackTrace();
		}
	}

}
