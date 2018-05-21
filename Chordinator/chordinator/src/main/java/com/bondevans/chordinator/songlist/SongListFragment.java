package com.bondevans.chordinator.songlist;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AlphabetIndexer;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.ColourScheme;
import com.bondevans.chordinator.EditSong;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongFile;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.dialogs.DeleteSongDialog;
import com.bondevans.chordinator.dialogs.SetListDialog;
import com.bondevans.chordinator.dialogs.SongDetailsDialog;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.utils.Ute;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class SongListFragment extends ListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "SongListFragment";
	private static OnSongSelectedListener songSelectedListener;
	private static final int SONG_LIST_LOADER = 0x01;
	private String mProjection[] = { 	SongDB.COLUMN_ID, 
			SongDB.COLUMN_FILE_PATH, 
			SongDB.COLUMN_FILE_NAME,
			SongDB.COLUMN_TITLE,
			SongDB.COLUMN_FAV};
	private long 	mSongId;
	private String	mFilePath;
	private String	mFileName;
	private String	mTitle;
	private SongCursorAdapter adapter;
	public final static int LIST_MODE_TITLE=0;
	public final static int LIST_MODE_RECENT=1;
	public final static int LIST_MODE_FAVS=2;
	public final static int LIST_MODE_ARTIST=3;
	public final static int LIST_MODE_NONE=99;
	public final static String LIST_MODE_KEY = "songlist_mode";
	public final static String LIST_DIR_KEY = "hs3e";
	public static int mListMode = LIST_MODE_TITLE;
	private int mPrevListMode = LIST_MODE_NONE;
	private int mDirection=LIST_MODE_NONE;	// 0=asc 1=desc
	private static final int ASC=0;
	private static final int DESC=1;
	static ListView mLv; 
	public String mFilter = "";

    /* (non-Javadoc)
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		mLv = getListView();
		Log.d(TAG, "HELLO onActivityCreated2");
		registerForContextMenu(mLv);
		// Enable fastscroll
		mLv.setFastScrollEnabled(true);
		if(isDim()){
			// Diminished Only - Load an ad
	        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
	        // values/strings.xml.
            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
	        // Create an ad request. 
	        if(mAdView != null){
		        // Start loading the ad in the background.
		        mAdView.loadAd(new AdRequest.Builder().build());
	        }
		}
	}

	/**
	 * Create a new instance of MyFragment that will be initialized
	 * with the given arguments.
	 */
	public static SongListFragment newInstance(int sortOrder, int direction) {
		SongListFragment f = new SongListFragment();
		Bundle b = new Bundle();
		b.putInt(LIST_MODE_KEY, sortOrder);
		b.putInt(LIST_DIR_KEY, direction);
		f.setArguments(b);
		Log.d(TAG, "HELLO newInstance");
		return f;
	}

	public void setFilter(String filter){
		mFilter=filter;
		getLoaderManager().restartLoader(SONG_LIST_LOADER, null, this);
		mFilter="";
	}

	public void changeSortOrder(int mode){
		Log.d(TAG, "changeSortOrder mode=["+mode+"]");
		mPrevListMode = mListMode;
		mListMode = mode;
		if(mListMode == LIST_MODE_TITLE || mListMode == LIST_MODE_ARTIST){
			getListView().setFastScrollEnabled(true);
		}
		else{
			// Disable fastscroll if NOT sorted by Title
			getListView().setFastScrollEnabled(false);
		}
		getLoaderManager().restartLoader(SONG_LIST_LOADER, null, this);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mListMode = args.getInt(LIST_MODE_KEY, LIST_MODE_TITLE);
			mDirection = args.getInt(LIST_DIR_KEY, LIST_MODE_NONE);
			Log.d(TAG, "HELLO onCreate ListMode:["+mListMode+"] Direction["+(mDirection==ASC?"ASC":"DESC")+"]");
		}

		Log.d(TAG, "HELLO onCreate");

		String[] uiBindFrom = { SongDB.COLUMN_TITLE, SongDB.COLUMN_ARTIST, SongDB.COLUMN_COMPOSER };
		int[] uiBindTo = { R.id.title, R.id.artist, R.id.composer };

		getLoaderManager().initLoader(SONG_LIST_LOADER, null, this);

		adapter = new SongCursorAdapter(
				getActivity(), R.layout.songlist_item,
				null, uiBindFrom, uiBindTo,
				0);

		setListAdapter(adapter);    
	}

    /* (non-Javadoc)
     * @see android.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreatView");
        View contentView = inflater.inflate(R.layout.songlist_layout, container, false);
		Log.d(TAG, "HELLO onCreatView2");
        return contentView;
	}

	public interface OnSongSelectedListener {
		void onSongSelected(long songId, String songPath);
		void browseFiles();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "HELLO onListItemClicked");
		viewSong(id);
	}

	private void viewSong(long id){
		Log.d(TAG, "HELLO viewSong");
		getSongFromId(id);
		songSelectedListener.onSongSelected( mSongId, Ute.doPath(mFilePath,mFileName) );
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			songSelectedListener = (OnSongSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSongSelectedListener");
		}
	}

	private String getDirection(){
		if(mListMode == mPrevListMode){
			// toggle direction if we've hit the same sort order twice
			mDirection = mDirection==ASC?DESC:ASC;
		}
		else if(mPrevListMode == LIST_MODE_NONE &&
				mDirection != LIST_MODE_NONE){
			// If this is the first load after the app re-starts (but not the very first run)
			// Then just use the stored direction
            Log.d(TAG, "Whatever");
		}
		else{
			// everything else use default sort direction
			if( mListMode == LIST_MODE_TITLE){
				mDirection=ASC;
			} else if(mListMode == LIST_MODE_ARTIST){
				mDirection=ASC;
			} else if(mListMode == LIST_MODE_RECENT){
				mDirection=DESC;
			} else if(mListMode == LIST_MODE_FAVS){
				mDirection=ASC;
			}
		}
		Log.L(TAG, "HELLO Direction", mDirection==ASC?" ASC":" DESC");
		return mDirection==ASC?" ASC":" DESC";
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String[] projection = { SongDB.COLUMN_ID, SongDB.COLUMN_TITLE, 
				SongDB.COLUMN_ARTIST, SongDB.COLUMN_COMPOSER, SongDB.COLUMN_LAST_ACCESS, SongDB.COLUMN_FAV };
		String orderBy="";
		String selectionClause = null;
		String [] selectionArgs = null;

		switch(mListMode){
		case LIST_MODE_TITLE:
			orderBy = SongDB.COLUMN_TITLE + getDirection();
			break;

		case LIST_MODE_ARTIST:
			orderBy = SongDB.COLUMN_ARTIST + getDirection();
			break;

		case LIST_MODE_RECENT:
			orderBy = SongDB.COLUMN_LAST_ACCESS + getDirection();
			break;

		case LIST_MODE_FAVS:
			String secondOrder="";
			// If sort by favs, sort by previous sort order as a secondary sort order
			switch(mPrevListMode){
			case LIST_MODE_TITLE:
				secondOrder = "," + SongDB.COLUMN_TITLE + " ASC";
				break;
			case LIST_MODE_RECENT:
				secondOrder = "," + SongDB.COLUMN_LAST_ACCESS + " DESC";
				break;
			}
			orderBy = SongDB.COLUMN_FAV + " DESC"+secondOrder;
			break;
		}

		if(mFilter.compareTo("")!=0){
			selectionClause = SongDB.COLUMN_TITLE + 	" like ? OR "+SongDB.COLUMN_ARTIST + " like ? OR "+SongDB.COLUMN_COMPOSER + " like ?";
			selectionArgs = new String[3];
			selectionArgs[0]= "%"+mFilter+"%";
			selectionArgs[1]= "%"+mFilter+"%";
			selectionArgs[2]= "%"+mFilter+"%";
			Log.d(TAG, "HELLO - selection=["+selectionClause+"]");
		}
		Log.d(TAG, "HELLO onCreateLoader id=["+id+"] listmode=["+mListMode+"]");
        return new CursorLoader(getActivity(),
                DBUtils.SONG(getString(R.string.authority)), projection, selectionClause, selectionArgs, orderBy);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "HELLO onLoadFinished");
		adapter.swapCursor(cursor);

		ListView lv = getListView();
		lv.setFastScrollEnabled(true);
		lv.setScrollingCacheEnabled(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "HELLO onLoadReset");
		adapter.swapCursor(null);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.d(TAG, "HELLO - songlistcontext");
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.d(TAG, "HELLO - songlistcontext2");
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.song_list_context, menu);
		menu.setHeaderTitle(getString(R.string.options));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG,"HELLO onContextItemSelected");
		if(item.getGroupId()!= R.id.context_group_songlist ){
			Log.d(TAG,"HELLO onContextItemSelected - NOT now dear");

			return super.onContextItemSelected(item);
		}
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long song_id = info.id;
		if (item.getItemId() == R.id.edit) {
			Log.d(TAG,"HELLO edit Selected:");
			editSong(song_id);
			return true;
		} else if (item.getItemId() == R.id.delete) {
			Log.d(TAG, "Delete: "+song_id);
			doDeleteX(song_id);		// This version asks first.
			return true;
		} else if (item.getItemId() == R.id.add_to_set) {
			Log.d(TAG, "Add_to_set: "+song_id);
			addSongToSet(song_id);
			return true;
		} else if (item.getItemId() == R.id.open) {
			Log.d(TAG,"HELLO open Selected ["+song_id+"]");
			viewSong(song_id);
			return true;
		} else if (item.getItemId() == R.id.details) {
			showDetails(song_id);
			return true;
		} else if (item.getItemId() == R.id.browseTo) {
			browseTo(song_id);
			return true;
		} else if (item.getItemId() == R.id.share) {
			shareSong(song_id);
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	private void browseTo(long song_id) {
		Log.d(TAG, "Browse to: "+song_id);
		getSongFromId(song_id);
		// Set current directory to the filepath of the selected song
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(SongPrefs.PREF_KEY_SONGDIR, mFilePath);
		editor.apply();
		// Switch to browse mode
		songSelectedListener.browseFiles();
	}

	private void showDetails(long song_id) {
		Log.d(TAG, "Show details: "+song_id);
		getSongFromId(song_id);
		DialogFragment newFragment = SongDetailsDialog.newInstance(Ute.doPath(mFilePath, mFileName));
		newFragment.show(getFragmentManager(), "dialog");
	}

	public void shareSong(long song_id){
		getSongFromId(song_id);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		SongFile mSf;
		try {
			mSf = new SongFile(Ute.doPath(mFilePath, mFileName), null, settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, ""));
		} catch (ChordinatorException e1) {
			e1.printStackTrace();
			SongUtils.toast(getActivity(), e1.getMessage());
			return;
		}
		File aFile = new File(mSf.getSongFilePath());
		Intent theIntent = new Intent(Intent.ACTION_SEND);
		theIntent.setType("text/plain");
		// TODO - need more intelligence about whether we want to share the raw chopro or
		// the formatted text.
		theIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(aFile));
		theIntent.putExtra(Intent.EXTRA_TEXT, mSf.getSong().getSongText());
		//next line specific to email attachments
		theIntent.putExtra(Intent.EXTRA_SUBJECT, "Sending " + aFile.getName());
		try {
			startActivity(Intent.createChooser(theIntent, "Share With...."));
		}
		catch (Exception e) {
            Log.d(TAG, "Oops");
		}
	}

	private void addSongToSet(long song_id) {
		Log.d(TAG, "Getting Set");
		getSongFromId(song_id);
		showSetListDialog(song_id, mTitle);
	}

	public void showSetListDialog(long songId, String songName) {
		DialogFragment newFragment = SetListDialog.newInstance(songId, songName);
		newFragment.show(getFragmentManager(), "dialog");
	}

    private void doDeleteX(long songId){
			Log.d(TAG, "doDelete: "+songId);
			getSongFromId(songId);
			DialogFragment newFragment = DeleteSongDialog.newInstance(getString(R.string.authority), songId, mTitle, Ute.doPath(mFilePath, mFileName));
			newFragment.show(getFragmentManager(), "dialog");
	}

	private void getSongFromId(long id) {
		Cursor songCursor = getActivity().getContentResolver().query(
				Uri.withAppendedPath(DBUtils.SONG(getString(R.string.authority)),
						String.valueOf(id)), mProjection, null, null, null);
		if (songCursor.moveToFirst()) {
			mSongId = songCursor.getLong(0);
			mFilePath = songCursor.getString(1);
			mFileName = songCursor.getString(2);
			mTitle = songCursor.getString(3);
		}
		else{
			mSongId = 0;
			mFilePath = "";
			mFileName = "";
			mTitle = "";
		}
		songCursor.close();
	}


	/**
	 * Edit the current Song 
	 */
	private void editSong(long id) {
		getSongFromId(id);
		// Open the file with the EditSong Activity
		Intent myIntent = new Intent(getActivity(), EditSong.class);
		try {
			// Put the path to the song file in the intent
			myIntent.putExtra(getString(R.string.song_path), Ute.doPath(mFilePath, mFileName));
			startActivity(myIntent);
		} 
		catch (ActivityNotFoundException e) {
			SongUtils.toast( getActivity(),e.getMessage());
		}
	}

	private static class SongCursorAdapter extends SimpleCursorAdapter implements SectionIndexer{
		private static final int TITLE_COLUMN_INDEX = 1;
		private static final int ARTIST_COLUMN_INDEX = 2;
		private Cursor mCursor;
		private Context mContext;
		private LayoutInflater mInflater;
		AlphabetIndexer mAlphaIndexer;
		private int mColourScheme;


		/* (non-Javadoc)
		 * @see android.widget.SimpleCursorAdapter#swapCursor(android.database.Cursor)
		 */
		@Override
		public Cursor swapCursor(Cursor c) {
			Log.d(TAG, "HELLO swapCursor listMode=["+mListMode+"]");
			mCursor = c;
			// Create our indexer
			if (c != null) {
				Log.d(TAG, "HELLO swapCursor2 listMode=["+mListMode+"]");
				// But only if we are sorting on Song Title or Artist
				if( mListMode == LIST_MODE_TITLE || mListMode == LIST_MODE_ARTIST ){
					mAlphaIndexer = new AlphabetIndexer(c, 
							c.getColumnIndex(mListMode == LIST_MODE_TITLE?SongDB.COLUMN_TITLE:SongDB.COLUMN_ARTIST),
							mContext.getString(R.string.alphabet));
				}
				else{
					mAlphaIndexer = null;
				}
			}
			return super.swapCursor(c);
		}

		public SongCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			mInflater = LayoutInflater.from(context);
			mCursor = c;
			this.mContext= context;
			mColourScheme = Ute.getColourScheme(context);
			Log.d(TAG, "HELLO SongCursorAdapter1 listMode=["+mListMode+"]");
			if( mListMode == LIST_MODE_TITLE || mListMode == LIST_MODE_ARTIST ){
				int index = mListMode == LIST_MODE_TITLE?TITLE_COLUMN_INDEX:ARTIST_COLUMN_INDEX;
				Log.d(TAG, "HELLO SongCursorAdapter2 index=["+index+"]");
				mAlphaIndexer = new AlphabetIndexer(c, index, mContext.getString(R.string.alphabet));
			}
			Log.d(TAG, "HELLO SongCursorAdapter3 listMode=["+mListMode+"]");
			//			mAlphaIndexer = new AlphabetIndexer(c, TITLE_COLUMN_INDEX, context.getString(R.string.alphabet));
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unnecessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			if( convertView == null){
				convertView = mInflater.inflate(R.layout.songlist_item, parent, false);
				// Creates a ViewHolder and store references to the child views
				// we want to bind data to.
				holder = new ViewHolder();
				//				holder.songId=pos;
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.artist = (TextView) convertView.findViewById(R.id.artist);
				holder.composer = (TextView) convertView.findViewById(R.id.composer);
				holder.fav = (ImageButton) convertView.findViewById(R.id.fav);
				holder.fav.setFocusable(false);

				holder.fav.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						final int position = mLv.getPositionForView((View) v.getParent());
						mCursor.moveToPosition(position);
						// Toggle Favourite
						ContentValues values = new ContentValues();//Values to update
						values.put(SongDB.COLUMN_FAV, mCursor.getInt(5)==1?0:1);

						mContext.getContentResolver().update(Uri.parse(DBUtils.SONG(mContext.getString(R.string.authority))+"/"+mCursor.getLong(0)), 
								values, 	// Columns to update
								null,	// where clause
								null);	// any args in the where clause
					}
				});

				convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			if( mCursor!= null){
				mCursor.moveToPosition(pos);
				//				holder.songId = mCursor.getLong(0);
				holder.title.setText(mCursor.getString(1));
				holder.artist.setText(mCursor.getString(2));
				String composer = mCursor.getString(3);
				if(!composer.equals("")){
					holder.composer.setText( "("+composer+")");
				}
				else{
					holder.composer.setText( "");
				}

				//				holder.last_access.setText(mCursor.getString(4));
				// Favourite image
				if( mCursor.getInt(5)== 1){
					holder.fav.setImageResource(R.drawable.fav_on_icon);
				}
				else{
					holder.fav.setImageResource(mColourScheme == ColourScheme.DARK?R.drawable.fav_off_dark:R.drawable.fav_off_light);
				}
			}

			return convertView;
		}
		static class ViewHolder {
			//			long 	songId;
			TextView title;
			TextView artist;
			TextView composer;
			ImageButton fav;
			//            TextView last_access;
		}
		@Override
		public int getPositionForSection(int section) {
//			Log.d(TAG, "HELLO getPositionForSection listMode=["+mListMode+"] section=["+section+"]");
			if(mAlphaIndexer != null){
				return mAlphaIndexer.getPositionForSection(section);
			}
			else{
				return 0;
			}
		}

		@Override
		public int getSectionForPosition(int position) {
//			Log.d(TAG, "HELLO getSectionForPosition listMode=["+mListMode+"] position=["+position+"]");
			if(mAlphaIndexer != null){
				return mAlphaIndexer.getSectionForPosition(position);
			}
			else{
				return 0;
			}
		}

		@Override
		public Object[] getSections() {
//			Log.d(TAG, "HELLO getSections listMode=["+mListMode+"]");
			if(mAlphaIndexer != null){
				return mAlphaIndexer.getSections();
			}
			else{
				return new Object[]{};
			}
		}
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	public void onStop() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(SongPrefs.PREF_KEY_SORTORDER, mListMode);
		editor.putInt(SongPrefs.PREF_KEY_SORTDIRECTION, mDirection);
		editor.apply();
		super.onStop();
	}
	private boolean isDim(){
		return(getString(R.string.app_version).equalsIgnoreCase("dim")?true:false);
	}
}
