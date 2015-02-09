package com.bondevans.chordinator.setlist;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.ListFragment;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View.OnKeyListener;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bondevans.chordinator.EditSong;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;
import com.bondevans.chordinator.songlist.SongListFragment.OnSongSelectedListener;
import com.bondevans.chordinator.dialogs.FeatureDisabledDialog;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.utils.Ute;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SetSongListFragment extends ListFragment implements OnKeyListener,
LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "SetSongListFragment";
	private OnSongSelectedListener songSelectedListener;
	private static final int SETSONG_LIST_LOADER = 0x01;
	private static final String KEY_SETID = "SET_ID";
	private static final String KEY_SETNAME = "SET_NAME";
	private long 	mSongId;
	private String	mFilePath;
	private String	mFileName;
	private SetSongCursorAdapter adapter;
	private long mSetId;
	private String mSetName="";
	Button addSongsButton;
	private View mContentView;
	private String mFilter="";
	private boolean mDiminished = false;
	private AdView mAdView;

	/* (non-Javadoc)
	 * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ListView lv = getListView();
		Log.d(TAG, "HELLO onActivityCreated");
		registerForContextMenu(lv);
		if(isDim()){
			// Diminished only - Load an ad
			// Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
			// values/strings.xml.
			mAdView = (AdView) getView().findViewById(R.id.adView);
			if(mAdView != null){
				// Create an ad request. 
		        // Start loading the ad in the background.
		        mAdView.loadAd(new AdRequest.Builder().build());
			}
        }
	}

	/**
	 * Create a new instance of MyFragment that will be initialized
	 * with the given arguments.
	 */
	public static SetSongListFragment newInstance(long setId, String setName) {
		SetSongListFragment f = new SetSongListFragment();
		Bundle b = new Bundle();
		b.putLong(KEY_SETID, setId);
		b.putString(KEY_SETNAME, setName);
		f.setArguments(b);
		Log.d(TAG, "HELLO newInstance");
		return f;
	}

	public void setSetId(long setId, String setName){
		mSetId=setId;
		mSetName=setName;
//		setNameText.setText(getString(R.string.set)+":"+mSetName);

		getLoaderManager().restartLoader(SETSONG_LIST_LOADER, null, this);
	}

	/**
	 * @return the Set Id
	 */
	public long getSetId() {
		return mSetId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mSetId = args.getLong(KEY_SETID, -1);
			mSetName = args.getString(KEY_SETNAME);
			Log.d(TAG, "HELLO onCreate set:["+mSetId+"]["+mSetName+"]");
		}
		else{
			Log.d(TAG, "HELLO onCreate - no setID!!");
			return;
		}
		String[] uiBindFrom = { SongDB.COLUMN_TITLE, SongDB.COLUMN_ARTIST, SongDB.COLUMN_COMPOSER };
		int[] uiBindTo = { R.id.title, R.id.artist, R.id.composer };

		getLoaderManager().initLoader(SETSONG_LIST_LOADER, null, this);

		adapter = new SetSongCursorAdapter(
				getActivity(), R.layout.setsonglist_item,
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
		Log.d(TAG, "HELLO onCreatView ["+mSetName+"]");
		
		mContentView = inflater.inflate(R.layout.setsonglist_layout, null);

		return mContentView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "HELLO onListItemClicked id=["+id+"]");
		// Get details of selected song
		if( mDiminished ){
			// Viewing song from SongList is disabled in Diminished
			FeatureDisabledDialog newFragment = FeatureDisabledDialog.newInstance();
			newFragment.show(getFragmentManager(), "dialog");
		}
		else{
			getSongFromId(id);
			songSelectedListener.onSongSelected(mSongId, Ute.doPath(mFilePath,mFileName) );
		}
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String[] projection = { SongDB.TABLE_SONG+"."+SongDB.COLUMN_ID, SongDB.COLUMN_TITLE, SongDB.COLUMN_ARTIST, SongDB.COLUMN_COMPOSER, SongDB.COLUMN_FAV };
		String selectionClause = null;
		String [] selectionArgs = null;

		if(mFilter.compareTo("")!=0){
			selectionClause = SongDB.COLUMN_TITLE + 	" like ? OR "+SongDB.COLUMN_ARTIST + " like ? OR "+SongDB.COLUMN_COMPOSER + " like ?";
			selectionArgs = new String[3];
			selectionArgs[0]= "%"+mFilter+"%";
			selectionArgs[1]= "%"+mFilter+"%";
			selectionArgs[2]= "%"+mFilter+"%";
			Log.d(TAG, "HELLO - selection=["+selectionClause+"]");
		}

		// create cursor to get all the set items in the correct order
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				Uri.withAppendedPath(DBUtils.SETITEM(getString(R.string.authority)),
						String.valueOf(mSetId)), projection, selectionClause, selectionArgs, SongDB.COLUMN_SET_ORDER + " ASC");

		return cursorLoader;
	}

	public void setFilter(String filter){
		mFilter=filter;
		getLoaderManager().restartLoader(SETSONG_LIST_LOADER, null, this);
		mFilter="";
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "HELLO onLoadFinished cursor["+(cursor==null?"is NULL":"NOT NULL")+"]");
		adapter.swapCursor(cursor);
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
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.d(TAG, "HELLO - songlistcontext");
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.setsong_list_context, menu);
		menu.setHeaderTitle(getString(R.string.options));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long song_id = info.id;
		if (item.getItemId() == R.id.edit) {
			Log.d(TAG,"HELLO edit Selected:");
			editSong(song_id);
			return true;
		} else if (item.getItemId() == R.id.delete) {
			Log.d(TAG, "Delete: "+song_id);
			doDelete(mSetId, song_id);		// This version just gets on with it.
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	void doDelete(long set_id, long song_id){
		String [] whereArgs = {""+song_id};
		int rows = getActivity().getContentResolver().delete(
				Uri.withAppendedPath(DBUtils.SETITEM(getString(R.string.authority)), String.valueOf(set_id)), 
				SongDB.COLUMN_SONG_ID+"=?", whereArgs);
		if(rows != 1){
			SongUtils.toast(getActivity(), "Failed to Delete - song_id="+song_id);
		}
	}

	private void getSongFromId(long id) {
		String projection[] = { 	SongDB.COLUMN_ID, 
				SongDB.COLUMN_FILE_PATH, 
				SongDB.COLUMN_FILE_NAME,
				SongDB.COLUMN_FAV};

		Cursor songCursor = getActivity().getContentResolver().query(
				Uri.withAppendedPath(DBUtils.SONG(getString(R.string.authority)),
						String.valueOf(id)), projection, null, null, null);
		if (songCursor.moveToFirst()) {
			mSongId = songCursor.getLong(0);
			mFilePath = songCursor.getString(1);
			mFileName = songCursor.getString(2);
		}
		else{
			mSongId = 0;
			mFilePath = "";
			mFileName = "";
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

	private static class SetSongCursorAdapter extends SimpleCursorAdapter{
		private Cursor mCursor;
		//		private Context context;
		private LayoutInflater mInflater;

		/* (non-Javadoc)
		 * @see android.widget.SimpleCursorAdapter#swapCursor(android.database.Cursor)
		 */
		@Override
		public Cursor swapCursor(Cursor c) {
			mCursor = c;
			return super.swapCursor(c);
		}

		public SetSongCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			mInflater = LayoutInflater.from(context);
			mCursor = c;
			//			this.context= context;
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
				convertView = mInflater.inflate(R.layout.setsonglist_item, null);
				// Creates a ViewHolder and store references to the child views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.artist = (TextView) convertView.findViewById(R.id.artist);
				holder.composer = (TextView) convertView.findViewById(R.id.composer);

				convertView.setTag(holder);
//				Log.d(TAG,"HELLO inflating v");
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			if( mCursor!= null){
				mCursor.moveToPosition(pos);
				holder.title.setText(mCursor.getString(1));
				holder.artist.setText(mCursor.getString(2));
				String composer = mCursor.getString(3);
				if(!composer.equals("")){
					holder.composer.setText( "("+composer+")");
				}
				else{
					holder.composer.setText( "");
				}
			}

			return convertView;
		}
		static class ViewHolder {
			TextView title;
			TextView artist;
			TextView composer;
		}
	}


	public void addSongs(long setId) {
		Intent myIntent = new Intent(getActivity(), AddSongsToSetActivity.class);
		try {
			// Put the SET iD in the intent
			myIntent.putExtra(AddSongsToSetActivity.KEY_SETID, setId);
			startActivity(myIntent);
		} 
		catch (ActivityNotFoundException e) {
			SongUtils.toast( getActivity(),e.getMessage());
		}
	}

	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Log.d(TAG, "HELLO keyPressed");
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Log.d(TAG, "HELLO BACK Pressed");
		}
		Log.d(TAG, "HELLO keyPressed - returning false");
		return false;
	}
	private boolean isDim(){
		return(getString(R.string.app_version).equalsIgnoreCase("dim")?true:false);
	}
}
