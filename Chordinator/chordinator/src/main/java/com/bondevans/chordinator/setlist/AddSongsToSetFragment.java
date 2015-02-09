package com.bondevans.chordinator.setlist;

import java.util.ArrayList;
import java.util.List;

//import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
//import android.net.Uri;
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
	static class Songs{
		boolean selected;
		long	songId;
		String	title;
	}
	static List<Songs> checked = new ArrayList<Songs>();

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
	static AddSongsToSetFragment newInstance(int mode) {
		AddSongsToSetFragment f = new AddSongsToSetFragment();
		Log.d(TAG, "HELLO newInstance");
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "HELLO onCreate");
		
		if(savedInstanceState==null){
			Log.d(TAG, "HELLO savedInstanceState ==null");
			// Reset checked arraylist
			checked.clear();
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

	private View mContentView;
	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.add_song_to_set_layout, null);
		Log.d(TAG, "onCreatView ");
		return mContentView;
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String[] projection = { SongDB.COLUMN_ID, SongDB.COLUMN_TITLE, 
				SongDB.COLUMN_ARTIST};

		String orderBy = SongDB.COLUMN_TITLE + " ASC";

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				DBUtils.SONG(getString(R.string.authority)), projection, null, null, orderBy);
		return cursorLoader;
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
		Log.d(TAG, "HELLO addSongsToSet ["+setId+"]");
		for(int i=0; i< checked.size(); i++){
//			Log.d(TAG, "HELLO pos["+i+"] title["+checked.get(i).title+"]["+(checked.get(i).selected?"ON":"OFF")+"]");
			if(checked.get(i).selected){
				try {
					DBUtils.addSongToSet(getActivity().getContentResolver(), getString(R.string.authority), setId, checked.get(i).songId);
				} catch (ChordinatorException e) {
					// Song already exists
					SongUtils.toast(getActivity(), checked.get(i).title + " "+ getString(R.string.already_in_set));
				}
			}
		}
		// TODO - Put list of songs in set into Description column
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
            // Need to maintain an array of booleans, matching the list of songs 
//			Log.d(TAG,"HELLO getView pos=["+pos+"] size=["+checked.size()+"]");

			// Have we got an entry for this position - if not add
			while(checked.size()<=pos){
				// add entry to array
				checked.add(new Songs());
			}
			// A ViewHolder keeps references to children views to avoid unnecessary calls
            // to findViewById() on each row.
            final ViewHolder holder;
			if( convertView == null){
//				Log.d(TAG,"HELLO inflating ["+pos+"]");
				convertView = mInflater.inflate(R.layout.add_song_to_set_item, null);
                // Creates a ViewHolder and store references to the child views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.artist = (TextView) convertView.findViewById(R.id.artist);
                holder.isChecked = (CheckBox) convertView.findViewById(R.id.checkBox1);
                // All songs start as unchecked.
                holder.isChecked.setChecked(checked.get(pos).selected);
                holder.isChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Log.d(TAG,"HELLO onCheckedChanged ["+(isChecked?"ON":"OFF")+"] title=["+holder.title.getText()+"]");
						Songs song = new Songs();
						song.songId = holder.songId;
						song.title = (String) holder.title.getText();
						song.selected = isChecked;
						checked.set(holder.pos, song);
					}
				});

                convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
//				Log.d(TAG,"HELLO NOT inflating ["+pos+"]");
				holder = (ViewHolder) convertView.getTag();
			}

			if( mCursor!= null){
				mCursor.moveToPosition(pos);
				holder.pos = pos;
				holder.songId = mCursor.getLong(0);
				holder.title.setText(mCursor.getString(1));
				holder.artist.setText(mCursor.getString(2));
				holder.isChecked.setChecked(checked.get(pos).selected);
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "HELLO onListItemClicked");
	}
}
