package com.bondevans.chordinator.search;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.Statics;
import com.bondevans.chordinator.conversion.SongConverter;
import com.bondevans.chordinator.conversion.SongConverterFragment;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.dialogs.GotSongDialog;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.utils.Ute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity implements GotSongDialog.GotSongListener {
	public static final String TAG = "SearchActivity";
	public static final String SEARCH_CRITERIA = "CRITERIA";
	private static final CharSequence GOOGLE = "google";
	public static final int MAX_LINES_TO_CHECK = 40;//2.4.0 Up'd to 40
    private static final String GOTSONG_TAG = "GotSonG";
    private static final String KEY_TITLE = "101";
    private static final String KEY_ARTIST = "102";
    private WebView searcher;
    String mFoundSongText;
	String mUrl;	// Current URL
	SearchPage mTask;
    ProgressBar mProgressBar;

    // Chord Reader stuff - START
    private static Pattern textAreaPattern = Pattern.compile(
            "(" +
                    "<\\s*textarea.*?>.*?<\\s*/textarea\\s*>" + // style span
                    ")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
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
		int colourScheme = Ute.getColourScheme(this);
		setTheme(colourScheme == ColourScheme.LIGHT? R.style.Chordinator_Light_Theme_Theme: R.style.Chordinator_Dark_Theme_Theme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.song_search_layout);
		searcher = findViewById(R.id.searcher);

		WebSettings settings = searcher.getSettings();
        String ua = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
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

        Toolbar toolbar = findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
        mProgressBar = findViewById(R.id.progress_bar); // Attaching the layout to the toolbar object


//        getSupportActionBar().setLogo(/*mColourScheme == ColourScheme.DARK? */R.drawable.chordinator_aug_logo_dark_bkgrnd/*: R.drawable.chordinator_aug_logo_light_bkgrnd*/);
        getSupportActionBar().setTitle(R.string.search_title);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set download folder
        mDownloadFolder = getDownloadFolder();
        if(savedInstanceState!=null){
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mArtist = savedInstanceState.getString(KEY_ARTIST);
            Log.d(TAG, "savedInstanceState: "+mTitle);
        }
	}

	private String getDownloadFolder() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getString(SongPrefs.PREF_KEY_DOWNLOADDIR, Statics.CHORDINATOR_DIR);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && searcher.canGoBack()) {
			Log.d(TAG, "HELLO - BACK PRESSED");
			cancelOldSearch();
			searcher.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void cancelOldSearch(){
		if(mTask != null){
			mTask.cancel(true);
			Log.d(TAG, "HELLO - cancelling task");
		}
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState: "+mTitle);
		cancelOldSearch();
		super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, mTitle);
        outState.putString(KEY_ARTIST, mArtist);
    }

	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null, otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected();
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

	@JavascriptInterface
	public void searchPageForSong(String html){
		Log.d(TAG, "HELLO searchPageForSong");
		// Don't bother if we are on a known non-song site - i.e. google
		if(mUrl.contains(GOOGLE)){
            cancelOldSearch();
			Log.d(TAG, "HELLO Ignoring page");
			return;
		}
		// Do all this on a new thread
		cancelOldSearch();
		mTask = new SearchPage();
		mTask.execute(html);
	}

    @Override
    public void onGotSong(String fileName, String songText, boolean isChoPro, boolean convertChopro) {
        // Save new song in chordinator folder...
        String fullPath = mDownloadFolder + fileName;
        try {
            SongUtils.writeFile(fullPath, songText);
            Log.d(TAG, "HELLO fileName=[" + fileName + "]");
            SongUtils.toast( SearchActivity.this, fullPath + " "+ getString(R.string.saved));
            // Create DB entry if its already in chopro format, otherwise convert to choPro if
            // required and then add to DB
            if(isChoPro){
                DBUtils.addSong(getContentResolver(), getString(R.string.authority), mDownloadFolder,
                        fileName, mTitle, mArtist, "");
            }
            else{
                if(convertChopro){
                    convertFileToChoPro(fileName);
                }
            }
        } catch (ChordinatorException e) {
            SongUtils.toast( SearchActivity.this, e.getMessage());
        }
    }

    public class SearchPage extends AsyncTask<String, Void, String>{
		String songText;
        private boolean mChopro;

        @Override
		protected String doInBackground(String... params) {
            Log.d(TAG, "HELLO searchPage1");
			// Look for start of song (i.e. {title:
			int start;
			int end;
			// See if there is a Chopro song in there
            String textArea=findTextArea(params[0]);
            if(!textArea.isEmpty() &&
			    (start=findChoproStart(textArea))>-1){
				songText = textArea.substring(start);
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
				Log.d(TAG, "HELLO searchPage got[" + mFoundSongText + "]");
                mChopro=true;
				return "OK";
			}
			else{
                // If not see if there is a UG-style song
				// Extract the contents of the <pre> </pre> tag if there is one
				mFoundSongText=findUGSong(params[0]);
				if( mFoundSongText != null){
                    mChopro = false;
                    return "OK";
				}
			}
			Log.d(TAG, "HELLO searchPage5");
			return "";
		}

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if( s.equalsIgnoreCase("OK")){
                showGotSongDialog(mFoundSongText, mChopro);
            }
        }

        /*
                 * {t:{start:(new Date).getTime()},bfr:!(!b)}};window.google.tick=function(a,b,c){if(!window.google.timers[a])google.startTick(a);window.google.timers[a].t[b]=c||(new Date).getTime()};google.startTick("load",true);try{window.google.pt=window.chrome&&window.chrome.csi&&Math.floor(window.chrome.csi().pageT);}catch(u){}

                 * */
        int findChoproStart(String line) {
			String [] startTags = {"{title:", "{t:"};// MUST BE LOWERCASE
			String [] badTags = {"{t:{start:","{t: t, o:"};
 			int ret=-1;
			int index=0;
			for( String tag: startTags){
				index=0;
                while( (ret=line.substring(index).toLowerCase().indexOf(tag))>-1){
					int ret2=-1;
					// Found a possible start tag, but need to check its not one of the known BAD tags
					for(String badTag: badTags){
						if((ret2=line.substring(index).substring(ret, ret+20).toLowerCase().indexOf(badTag))>-1){
							Log.d(TAG, "HELLO found BAD tag: ["+badTag+"] index=["+ret2+"]");
							index=ret+1; // Continue search for a matching tag
							break;
						}
					}
					if(ret2 == -1){
						Log.d(TAG, "HELLO findSongStart: ["+tag+"] index=["+ret+"]");
						break;
					}
				}
			}
			return index+ret;
		}
		int findChoproEnd(String line) {
			// IF any of the "end" tags are found then we have found the end
			String [] endTags={"\"<", "<a href=","</textarea>","</form>", "<input", "</pre>","</body>"};// MUST BE LOWERCASE
			int ret=-1;
			int index;
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
        String findTextArea(String html){
            Log.d(TAG, "HELLO findTextArea");
            Matcher matcher = textAreaPattern.matcher(html);

            if (matcher.find()) {
                Log.d(TAG, "HELLO found <textArea> tag"+matcher.group());
                return matcher.group();
            }
            else{
                return "";
            }
        }
		/**
		 * Try to find a likely chord chart from the "pre" section of a page
		 * Returns null if it doesn't find anything likely to be a chord chart
		 * @param html HTML to sesarch
		 * @return Returns song if found
		 */
        String findUGSong(String html) {
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
		String cleanUpText(String text) {

			if (text == null) {
				return null;
			}

			text = text.trim();

			// get rid of \r
			text = text.replaceAll("\r", "");

			// replace multiple newlines with just two newlines
			text = multipleNewlinePattern.matcher(text).replaceAll("\n\n");

			return text;
		}

		String convertHtmlToText(String htmlText) {

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
			Log.d(TAG, "HELLO got [" + html.substring(0, 10) + "..." + html.substring(html.length() - 10, html.length()));
			searchPageForSong(html);
		}
	}

	public class MyWebViewClient extends WebViewClient{

		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
		 */
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressBar.setVisibility(View.VISIBLE);
            mUrl = url;
			Log.d(TAG, "HELLO loading page:" + mUrl);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// This call injects JavaScript into the page which just finished loading.
			searcher.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

    String mTitle;
    String mArtist;
    private void showGotSongDialog(final String songText, final boolean choPro) {
        // Make sure we hafven't already got one going
        GotSongDialog mDialogFragment = (GotSongDialog) getSupportFragmentManager().findFragmentByTag(GOTSONG_TAG);
        if(mDialogFragment != null){
            mDialogFragment.dismiss();
        }

        mTitle = SongUtils.tagValue(songText, "title","t","newSong");
		mArtist= SongUtils.tagValue(songText, "subtitle","st","unknown");
		GotSongDialog dialog = new GotSongDialog();
        Bundle args = new Bundle();
        args.putBoolean(GotSongDialog.KEY_CHOPRO, choPro);
        args.putString(GotSongDialog.KEY_SONGTEXT, cleanSong(songText));
        args.putString(GotSongDialog.KEY_SONGFILE, mTitle + (choPro?Statics.SONGFILEEXT : Statics.TEXTFILEEXT));
        dialog.setArguments(args);
        dialog.setGotSongListener(this);
		dialog.show(getSupportFragmentManager(), GOTSONG_TAG);
	}

	private void convertFileToChoPro(String fileName) {
		Log.d(TAG, "HELLO - converting ["+fileName+"]");
		SongConverterFragment newFragment = SongConverterFragment.newInstance(getString(R.string.authority),
                mDownloadFolder, fileName);
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
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		cancelOldSearch();
		super.onDestroy();
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

    @Override
    protected void onResume() {
        super.onResume();
        // See if GotSongDialog is already up and running, if so, set this activity as the GotSongListener
        GotSongDialog mDialogFragment = (GotSongDialog) getSupportFragmentManager().findFragmentByTag(GOTSONG_TAG);
        if(mDialogFragment  != null){
            mDialogFragment.setGotSongListener(this);
        }
    }
}
