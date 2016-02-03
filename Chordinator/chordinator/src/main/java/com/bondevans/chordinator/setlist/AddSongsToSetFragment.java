package com.bondevans.chordinator.setlist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.db.SongDB;

public class AddSongsToSetFragment extends ListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "AddSongsToSetFragment";
	private static final int SONG_LIST_LOADER = 0x01;
	private SongCursorAdapter adapter;
	private OnSongsAddedListener onSongsAddedListener;
	private boolean mConfigChangeInProgress=false;
	private long mSetId;
	private String mFilter="";

	static class CheckedSetSong extends SetSong{
		boolean selected;

		public CheckedSetSong(){
			super(0,"","","","",0);
			selected = false;
		}
	}

	/**
	 * List of songs to be added to the Set - only songs that have been selected
	 * are added to the list
	 */
	private static List<CheckedSetSong> mChecked = new ArrayList<>();

	public interface OnSongsAddedListener {
		void songsAdded();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onSongsAddedListener = (OnSongsAddedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSongsAddedListener");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ListView lv = getListView();
		Log.d(TAG, "HELLO onActivityCreated");
		registerForContextMenu(lv);
		lv.setFastScrollEnabled(true);
	}

	/**
	 * Create a new instance of MyFragment that will be initialized
	 * with the given arguments.
	 */
	static AddSongsToSetFragment newInstance(long setId, String setName) {
		AddSongsToSetFragment f = new AddSongsToSetFragment();
		Log.d(TAG, "HELLO newInstance");

		Bundle args = new Bundle();
		args.putLong(SetSongListActivity.KEY_SETID, setId);
		args.putString(SetSongListActivity.KEY_SETNAME, setName);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "HELLO onCreate");

		Bundle args = getArguments();
		if(args!=null){
			mSetId = args.getLong(SetSongListActivity.KEY_SETID);
			Log.d(TAG, "HELLO onCreate - setId["+mSetId+"]");
		}

		if(savedInstanceState==null){
			Log.d(TAG, "HELLO savedInstanceState ==null");
			// Reset mChecked arraylist
			mChecked.clear();
		}
		else{
			Log.d(TAG, "HELLO savedInstanceState !=null");
		}

		String[] uiBindFrom = { SongDB.COLUMN_TITLE, SongDB.COLUMN_ARTIST};
		int[] uiBindTo = { R.id.title, R.id.artist };

		getLoaderManager().initLoader(SONG_LIST_LOADER, null, this);

		adapter = new SongCursorAdapter( getActivity(), R.layout.add_song_to_set_item,
				null, uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		setListAdapter(adapter);    
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView ");
		return inflater.inflate(R.layout.add_song_to_set_layout, container, false);
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String[] projection = { SongDB.COLUMN_ID, SongDB.COLUMN_TITLE, 
				SongDB.COLUMN_ARTIST};

		String orderBy = SongDB.COLUMN_TITLE + " ASC";

		// Support Filter
		String selectionClause="";
		String[] selectionArgs=null;
		if(!mFilter.isEmpty()){
			selectionClause = SongDB.COLUMN_TITLE + " like ? OR " + SongDB.COLUMN_ARTIST + " like ? OR " + SongDB.COLUMN_COMPOSER + " like ?";
			selectionArgs = new String[3];
			selectionArgs[0]= "%"+mFilter+"%";
			selectionArgs[1]= "%"+mFilter+"%";
			selectionArgs[2]= "%"+mFilter+"%";
			Log.d(TAG, "HELLO - selection=["+selectionClause+"]");
		}

		return new CursorLoader(getActivity(),
				DBUtils.SONG(getString(R.string.authority)), projection, selectionClause, selectionArgs, orderBy);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "HELLO onLoadFinished");
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
		inflater.inflate(R.menu.song_list_context, menu);
		menu.setHeaderTitle(getString(R.string.options));
	}
	
	public void addSongsToSet(long setId){
		boolean listenerUpdated=false;
		Log.d(TAG, "HELLO addSongsToSet ["+setId+"]");
		for(int i=0; i< mChecked.size(); i++){
//			Log.d(TAG, "HELLO pos["+i+"] title["+mChecked.get(i).title+"]["+(mChecked.get(i).selected?"ON":"OFF")+"]");
			if(mChecked.get(i).selected){
				try {
					DBUtils.addSongToSet(getActivity().getContentResolver(), getString(R.string.authority), setId, mChecked.get(i).id);
					if(!listenerUpdated){
						onSongsAddedListener.songsAdded();
						listenerUpdated=true;
					}
				} catch (ChordinatorException e) {
					// Song already exists
					SongUtils.toast(getActivity(), mChecked.get(i).title + " "+ getString(R.string.already_in_set));
				}
			}
		}
	}

	private static class SongCursorAdapter extends SimpleCursorAdapter{
		private Cursor mCursor;
		private LayoutInflater mInflater;

		/* (non-Javadoc)
		 * @see android.support.v4.widget.SimpleCursorAdapter#swapCursor(android.database.Cursor)
		 */
		@Override
		public Cursor swapCursor(Cursor c) {
			mCursor = c;
			return super.swapCursor(c);
		}

		public SongCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			mInflater = LayoutInflater.from(context);
			mCursor = c;
		}

		/* (non-Javadoc)
		 * @see android.support.v4.widget.CursorAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unnecessary calls
            // to findViewById() on each row.
            final ViewHolder holder;
			if( convertView == null){
				convertView = mInflater.inflate(R.layout.add_song_to_set_item, parent, false);
                // Creates a ViewHolder and store references to the child views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.artist = (TextView) convertView.findViewById(R.id.artist);
                holder.isChecked = (CheckBox) convertView.findViewById(R.id.checkBox1);
                // All songs start as unchecked.
                holder.isChecked.setChecked(isSelected(holder.songId));
                holder.isChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Log.d(TAG,"Changed ["+(isChecked?"ON":"OFF")+"] title=["+holder.title.getText()+"] pos=["+holder.pos+"]");
						CheckedSetSong song = new CheckedSetSong();
						song.id = holder.songId;
						song.title = holder.title.getText().toString();
						song.selected = isChecked;
						setSelected(song);
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
				holder.pos = pos;
				holder.songId = mCursor.getLong(0);
				holder.title.setText(mCursor.getString(1));
				holder.artist.setText(mCursor.getString(2));
				holder.isChecked.setChecked(isSelected(holder.songId));
			}

			return convertView;
		}
        static class ViewHolder {
            int	pos;
            long	songId;
        	TextView title;
            TextView artist;
            CheckBox isChecked;
        }
	}

	/**
	 * Returns true if the song with the given Id is currently selected, else false
	 * @param id song.id
	 * @return true if the song is currently selected
	 */
	static boolean isSelected(long id){
		// Return mChecked position for give id
		for(int i=0; i< mChecked.size();i++){
//			Log.d(TAG, "isSelected pos["+i+"] title["+mChecked.get(i).title+"]["+(mChecked.get(i).selected?"ON":"OFF")+"]");
			if(mChecked.get(i).id == id){
				return mChecked.get(i).selected;
			}
		}
		return false;
	}

	/**
	 * Set the selected state of the given song in the mChecked list - adding it if necessary
	 * @param song A song
	 */
	static void setSelected(CheckedSetSong song){
		for(int i=0; i< mChecked.size();i++){
			Log.d(TAG, "setSelected pos["+i+"] title["+song.title+"]["+(song.selected?"ON":"OFF")+"]");
			if(mChecked.get(i).id == song.id){
				// Got the entry.  Now need to update the selected flag
				mChecked.set(i,song);
				return ;
			}
		}
		// If we didn't find an entry then add a new one (if selected is true - otherwise don't bother)
		if(song.selected){
			Log.d(TAG, "setSelected Adding title["+song.title+"]["+(song.selected?"ON":"OFF")+"]");
			mChecked.add(song);
		}
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "HELLO onListItemClicked");
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		if(!mConfigChangeInProgress){
			// Back button pressed so, update set songs
			Log.d(TAG, "BACK");
			addSongsToSet(mSetId);
		}
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mConfigChangeInProgress = true;
		Log.d(TAG, "onSaveInstanceState");
	}

	/**
	 * Sets the text to filter the lsit of songs by
	 * @param filter filter text
	 */
	public void setFilter(String filter){
		mFilter=filter;
		getLoaderManager().restartLoader(SONG_LIST_LOADER, null, this);
		mFilter="";
	}
}
