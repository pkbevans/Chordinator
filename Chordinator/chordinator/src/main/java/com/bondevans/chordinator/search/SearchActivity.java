package com.bondevans.chordinator.search;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.Statics;
import com.bondevans.chordinator.conversion.SongConverter;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.utils.Ute;
import com.bondevans.chordinator.conversion.SongConverterFragment;

public class SearchActivity extends SherlockFragmentActivity {
	public static final String TAG = "SearchActivity";
	public static final String SEARCH_CRITERIA = "CRITERIA";
	public static final String CHORDIE_ONLY = "CHORDIE_ONLY";
	private static final String KEY_SONGTEXT = "KEY1";
	private static final String KEY_SONGFILE = "KEY2";
	private static final String KEY_CHOPRO = "KEY3";
	private static final CharSequence GOOGLE = "google";
	private static final int GRABCHORDS_ID = Menu.FIRST + 1;
	public static final int MAX_LINES_TO_CHECK = 40;//2.4.0 Up'd to 40
	private WebView searcher;
	private String ua = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.77 Safari/535.7	";
	String mFoundSongText;
	String mUrl;	// Current URL
	SearchPage mTask;
	private int mColourScheme;

	// Chord Reader stuff - START
	// html tag or html escaped character
	private static Pattern htmlObjectPattern = Pattern.compile(
			"(" +
					"<\\s*style.*?>.*?<\\s*/style\\s*>" + // style span
					"|" + // OR
					"<\\s*script.*?>.*?<\\s*/script\\s*>" + // script span
					"|" + // OR
					"<\\s*head.*?>.*?<\\s*/head\\s*>" + // head span
					"|" + // OR
					"<[^>]++>" + // html tag, such as '<br/>' or '<a href="www.google.com">'
					"|" + // OR
					"&[^; \n\t]++;" + // escaped html character, such as '&amp;' or '&#0233;'
					")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


	// HTML newline tag, such as '<p>' or '<br/>'
	private static Pattern htmlNewlinePattern = Pattern.compile(
			"<(?:p|br)\\s*+(?:/\\s*+)?>", Pattern.CASE_INSENSITIVE);

	private static Pattern prePattern = Pattern.compile("<pre[^>]*>(.*?)</pre>",
			Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	private static Pattern multipleNewlinePattern = Pattern.compile("([ \t\r]*\n[\t\r ]*){2,}");
	// Chord Reader stuff - END

	private static String mDownloadFolder;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mColourScheme = Ute.getColourScheme(this);
		setTheme(mColourScheme == ColourScheme.LIGHT? R.style.Theme_Sherlock_Light: R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
        //This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.song_search_layout);
		searcher = (WebView) findViewById(R.id.searcher);

		WebSettings settings = searcher.getSettings();
		settings.setUserAgentString(ua);
		settings.setBuiltInZoomControls(true);

		// JavaScript must be enabled if you want it to work, obviously
		searcher.getSettings().setJavaScriptEnabled(true);
		// Register a new JavaScript interface called HTMLOUT
		searcher.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
		// WebViewClient must be set BEFORE calling loadUrl!
		searcher.setWebViewClient(new MyWebViewClient());

		if(isNetworkAvailable()){
			String searchCriteria = getIntent().getStringExtra(SEARCH_CRITERIA);
			//load the google search page
			searcher.loadUrl("http://www.google.com/search?q="+searchCriteria);
		}
		else{
			DialogFragment newFragment = new NoNetworkDialog();
			newFragment.show(getSupportFragmentManager(), "dialog");
		}
        getSupportActionBar().setLogo(mColourScheme == ColourScheme.DARK? R.drawable.chordinator_aug_logo_dark_bkgrnd: R.drawable.chordinator_aug_logo_light_bkgrnd);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set download folder
        mDownloadFolder = getDownloadFolder();
	}

	private String getDownloadFolder() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getString(SongPrefs.PREF_KEY_DOWNLOADDIR, Statics.CHORDINATOR_DIR);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && searcher.canGoBack()) {
			Log.d(TAG, "HELLO - BACK PRESSED");
			cancelSearch();
			searcher.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void cancelSearch(){
		if(mTask != null){
			mTask.cancel(true);
			Log.d(TAG, "HELLO - cancellign task");
		}
	}
	public void exitSearch(View v){
		// Get outta here
		finish();
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		cancelSearch();
		super.onSaveInstanceState(outState);
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null, otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static String cleanSong(String songText) {
		Log.d(TAG, "HELLO cleanSong- IN:"+songText);
		String[][] reps={
				{".*\\{title:", "{title:"},
				{"\\\\'", "'"},
				{"&nbsp;", " "},
				{"&amp;nbsp;", " "},
				{"&lt;", "<"},
				{"&gt;", ">"},
				{"\">", ""},
				{"&quot;", "\""},
				{"<a href.*",""}
		};
		for( String [] rep: reps){
			songText = songText.replaceAll(rep[0], rep[1]);
		}
		Log.d(TAG, "HELLO cleanSong-OUT:"+songText);
		return songText;
	}

/*
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		AlertDialog alertDialog = null;

		switch(id){
		case DIALOG_ID_NO_NETWORK:
			builder = new AlertDialog.Builder(SearchActivity.this);
			builder.setMessage("No Network connection");
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing....
				}
			});
			alertDialog = builder.create();
		}
		return alertDialog;
	}
*/

	@JavascriptInterface
	public void searchPageForSong(String html){
		Log.d(TAG, "HELLO searchPageForSong");
		// Don't bother if we are on a known non-song site - i.e. google
		if(mUrl.contains(GOOGLE)){
			Log.d(TAG, "HELLO Ignoring page");
			return;
		}
		// Do all this on a new thread
		cancelSearch();
		mTask = new SearchPage();
		mTask.execute(new String[] {html});
	}

	public class SearchPage extends AsyncTask<String, Void, String>{
		String songText;
		@Override
		protected String doInBackground(String... params) {
			Log.d(TAG, "HELLO searchPage1");
			// Look for start of song (i.e. {title:
			int start = -1;
			int end = -1;
			// See if there is a Chopro song in there
			if( (start=findChoproStart(params[0]))>-1){
				songText = params[0].substring(start);
				Log.d(TAG, "HELLO searchPage2["+songText+"]");
				// if start found then find end
				if( (end=findChoproEnd(songText))>-1){
					// clean up
					mFoundSongText = songText.substring(0, end);
				}
				else{
					// Assume its all part of the song if no end is found
					mFoundSongText = songText;
				}
				Log.d(TAG, "HELLO searchPage got["+mFoundSongText+"]");
				// and show dialog
				showGotSongDialog(mFoundSongText, true);
				Log.d(TAG, "HELLO searchPage4");
			}
			// If not see if there is a UG-style song
			else{
				// Extract the contents of the <pre> </pre> tag if there is one
				mFoundSongText=findUGSong(params[0]);
				if( mFoundSongText != null){
					showGotSongDialog(mFoundSongText, false);
				}
			}
			Log.d(TAG, "HELLO searchPage5");
			return null;
		}

		/*
		 * {t:{start:(new Date).getTime()},bfr:!(!b)}};window.google.tick=function(a,b,c){if(!window.google.timers[a])google.startTick(a);window.google.timers[a].t[b]=c||(new Date).getTime()};google.startTick("load",true);try{window.google.pt=window.chrome&&window.chrome.csi&&Math.floor(window.chrome.csi().pageT);}catch(u){}

		 * */

		public int findChoproStart(String line) {
			String [] startTags = {"{title:", "{t:"};// MUST BE LOWERCASE
			String [] badTags = {"{t:{start:"};
			int ret=-1;
			for( String tag: startTags){
				if( (ret=line.toLowerCase().indexOf(tag))>-1){
					int ret2=-1;
					// Found a possible start tag, but need to check its not one of the known BAD tags
					for(String badTag: badTags){
						if((ret2=line.substring(ret, ret+20).toLowerCase().indexOf(badTag))>-1){
							Log.d(TAG, "HELLO found BAD tag: ["+badTag+"] index=["+ret2+"]");
							break;
						}
					}
					if(ret2 == -1){
						Log.d(TAG, "HELLO findSongStart: ["+tag+"] index=["+ret+"]");
						break;
					}
				}
			}
			return ret;
		}
		public int findChoproEnd(String line) {
			// IF any of the "end" tags are found then we have found the end
			String [] endTags={"\"<", "<a href=","</form>", "<input", "</pre>","</body>"};// MUST BE LOWERCASE
			int ret=-1;
			int index=0;
			for( String tag: endTags){
				if( (index=line.toLowerCase().indexOf(tag))>-1 ){
					if(ret == -1 || index < ret){
						ret = index;
					}
				}
			}
			if(ret>-1){
				Log.d(TAG, "HELLO findSongEnd: ["+ret+"]");
			}
			return ret;
		}
		/**
		 * Try to find a likely chord chart from the "pre" section of a page
		 * Returns null if it doesn't find anything likely to be a chord chart
		 * @param html
		 * @return
		 */
		public String findUGSong(String html) {
			Log.d(TAG, "HELLO findUGSong");
			Matcher matcher = prePattern.matcher(html);

			while (matcher.find()) {
				Log.d(TAG, "HELLO findUGSong found <pre> tag");
				String preHtml = matcher.group(1);
				Log.d(TAG, "HELLO preHTML=["+preHtml.substring(0, preHtml.length()>100?100:preHtml.length())+"]");//2.4.0 Bug fix - remove hard coded 100
				String preTxt = convertHtmlToText(preHtml);
				Log.d(TAG, "HELLO: preTxt=["+preTxt.substring(0, preTxt.length()>100?100:preTxt.length())+"]");//2.4.0 Bug fix - remove hard coded 100
				if (SongConverter.containsChordLines(preTxt, MAX_LINES_TO_CHECK)) {
					return cleanUpText(preTxt);
				}
			}
			return null;
		}
		public String cleanUpText(String text) {

			if (text == null) {
				return text;
			}

			text = text.trim();

			// get rid of \r
			text = text.replaceAll("\r", "");

			// replace multiple newlines with just two newlines
			text = multipleNewlinePattern.matcher(text).replaceAll("\n\n");

			return text;
		}

		public String convertHtmlToText(String htmlText) {

			StringBuilder plainText = new StringBuilder();

			// replace HTML tags with spaces and unescape HTML characters
			Matcher matcher = htmlObjectPattern.matcher(htmlText);
			int searchIndex = 0;
			while (matcher.find(searchIndex)) {
				int start = matcher.start();
				int end = matcher.end();
				String htmlObject = matcher.group();

				String replacementString;
				if (htmlText.charAt(start) == '&') { // html escaped character

					if (htmlObject.equalsIgnoreCase("&nbsp;")) {
						replacementString = " "; // apache replaces nbsp with unicode \xc2\xa0, but we prefer just " "
					} else {
						replacementString = Html.fromHtml(htmlObject).toString();
					}

					if (TextUtils.isEmpty(replacementString)) { // ensure non-empty - otherwise offsets would be screwed up
						Log.d(TAG, "Warning: couldn't escape html string: '" + htmlObject + "'");
						replacementString = " ";
					}
				} else if (htmlNewlinePattern.matcher(htmlObject).matches()) { // newline tag
					if (htmlObject.toLowerCase().contains("p")) { // paragraph break
						replacementString = "\n\n";
					} else { // 'br' (carriage return)
						replacementString = "\n";
					}
				} else { // html tag or <style>/<script> span
					replacementString = "";
				}

				plainText.append(htmlText.substring(searchIndex, start));

				plainText.append(replacementString);

				searchIndex = end;
			}
			plainText.append(htmlText.substring(searchIndex, htmlText.length()));

			return plainText.toString();
		}
	}

	public class MyJavaScriptInterface{

		@JavascriptInterface
		public void showHTML(String html)
		{
			Log.d(TAG, "HELLO got ["+html.substring(0, 10)+"..."+html.substring(html.length()-10, html.length()));
			searchPageForSong(html);
		}
	}

	public class MyWebViewClient extends WebViewClient{

		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
		 */
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			setSupportProgressBarIndeterminateVisibility(true);
			mUrl = url;
			Log.d(TAG, "HELLO loading page:"+mUrl);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// This call injects JavaScript into the page which just finished loading.
			searcher.loadUrl( "javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	private void showGotSongDialog(String songText, boolean choPro) {
		String title = SongUtils.tagValue(songText, "title","t","newSong");
		String artist = SongUtils.tagValue(songText, "subtitle","st","unknown");
		DialogFragment newFragment = GotSongDialog.newInstance(
				choPro,
				cleanSong(songText),
				title,
				artist,
				title + (choPro?Statics.SONGFILEEXT:Statics.TEXTFILEEXT));	// Default file name
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public static class GotSongDialog extends DialogFragment{
		private static TextView mSongTextView;
		private static TextView mFileName;
		static String mTitle;
		static String mArtist;

		static GotSongDialog newInstance(boolean choPro, String songText, String title, String artist, String songFile) {
			GotSongDialog frag = new GotSongDialog();
			Bundle args = new Bundle();
			mArtist = artist;
			mTitle = title;
			args.putString(KEY_SONGTEXT, songText);
			args.putString(KEY_SONGFILE, songFile);
			args.putBoolean(KEY_CHOPRO, choPro);
			frag.setArguments(args);
			return frag;
		}
		/* (non-Javadoc)
		 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final String songText;
			final boolean choPro = getArguments().getBoolean(KEY_CHOPRO);
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.song_found_dialog,
					(ViewGroup) getActivity().findViewById(R.id.layout_root));

			// Only show the "Convert to Chopro checkbox if the file is NOT already in chopro
			final CheckBox convertChopro = (CheckBox) layout.findViewById(R.id.convertChopro);
			convertChopro.setVisibility(choPro?View.INVISIBLE:View.VISIBLE);

			mSongTextView = (TextView) layout.findViewById(R.id.song);
			mFileName = (TextView) layout.findViewById(R.id.file_name);

			songText = getArguments().getString(KEY_SONGTEXT);
			mSongTextView.setText(songText);
			mFileName.setText(getArguments().getString(KEY_SONGFILE));

			return new AlertDialog.Builder(getActivity())
			.setView(layout)
			.setTitle(R.string.set_song_details)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Save new song in chordinator folder...
					String fileName = mFileName.getText().toString();
					String fullPath = mDownloadFolder + fileName;
					try {
//						File x = new File(mDownloadFolder + newFileName);
						SongUtils.writeFile(fullPath, songText);
						Log.d(TAG, "HELLO fileName=["+fileName+"]");
						SongUtils.toast( getActivity(), fullPath + " "+ getString(R.string.saved));
						// Create DB entry if its already in chopro format, otherwise convert to choPro if
						// required and then add to DB
						if(choPro){
							DBUtils.addSong(getActivity().getContentResolver(), getString(R.string.authority), mDownloadFolder,
								fileName, mTitle, mArtist, "");
						}
						else{
							if(convertChopro.isChecked()){
								((SearchActivity)getActivity()).convertFileToChoPro(mDownloadFolder, fileName);
							}
						}

					} catch (ChordinatorException e) {
						SongUtils.toast( getActivity(), e.getMessage());
					}
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Cancel pressed so do nothing
				}
			}).create();
		}
	}

	private void convertFileToChoPro(String folder, String fileName) {
		Log.d(TAG, "HELLO - converting ["+fileName+"]");
		SongConverterFragment newFragment = SongConverterFragment.newInstance(getString(R.string.authority), folder, fileName);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back to Songs
			finish();
			break;
		case GRABCHORDS_ID:
			break;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		cancelSearch();
		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//		menu.add(0,GRABCHORDS_ID, 0, getString(R.string.start_grab))
		//		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	public static class NoNetworkDialog extends DialogFragment{

		/* (non-Javadoc)
		 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
			.setMessage(getString(R.string.no_network))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Finish
					getActivity().finish();
				}
			})
			.create();
		}
	}
}
