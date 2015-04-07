package com.bondevans.chordinator.setlist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bondevans.chordinator.EditSong;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.songlist.SongListFragment;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class EditSetListFragment extends ListFragment {
	private final static String TAG = "EditSetListFragment";
	private boolean mLoaded = false;
	public SetList2 mSetList;
	private long mSetId;
	private String mSetName;
	private SetSongAdapter mAdapter;
	private SongListFragment.OnSongSelectedListener songSelectedListener;

	private final DragSortListView.DropListener mDropListener =
			new DragSortListView.DropListener() {
				@Override
				public void drop(int from, int to) {
					if (from != to) {
						Log.d(TAG, "HELLO drop [" + from + "][" + to + "]");
						// Swap list items in adapter
						SetSong item = mAdapter.getItem(from);
						mAdapter.remove(item);
						mAdapter.insert(item, to);
						// also swap items in SetList2
						mSetList.moveSong(from, to);
						// Swap in the DB too
						doSwap(from, to);
					}
				}
			};

	private final DragSortListView.RemoveListener mRemoveListener =
			new DragSortListView.RemoveListener() {
				@Override
				public void remove(int which) {
					Log.d(TAG, "HELLO remove[" + which + "]");
					// remove item from list
					SetSong song = mAdapter.getItem(which);
					mAdapter.remove(song);
					// also remove it from the SetList2
					mSetList.removeSong(which);
					// Delete from DB
					doDelete(mSetId, song.id);
				}
			};
	private String mFilter="";

	public static EditSetListFragment newInstance(long setId, String setName) {
		EditSetListFragment f = new EditSetListFragment();

		Bundle args = new Bundle();
		args.putLong(SetSongListActivity.KEY_SETID, setId);
		args.putString(SetSongListActivity.KEY_SETNAME, setName);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		Log.d(TAG, "HELLO onCreate - mLoaded[" + (mLoaded ? "TRUE" : "FALSE") + "]");
		super.onCreate(savedInstance);

		Bundle args = getArguments();
		if (args != null) {
			mSetId = args.getLong(SetSongListActivity.KEY_SETID);
			mSetName = args.getString(SetSongListActivity.KEY_SETNAME);
			Log.d(TAG, "HELLO onCreate - setId[" + mSetId + "] setname[" + mSetName + "]");
		}
		setRetainInstance(true);
	}

	/**
	 * Called from DSLVFragment.onActivityCreated(). Override to
	 * set a different mAdapter.
	 */
	protected void setListAdapter(String filter) {
		List<SetSong> array;
		if(!filter.isEmpty()){
			array = mSetList.getFilteredSongs(filter);
		}
		else {
			array = mSetList.getSongs();
		}
		List<SetSong> list = new ArrayList<SetSong>(array);

		mAdapter = new SetSongAdapter(getActivity(), R.layout.edit_setlist_item, R.id.title, list);
		setListAdapter(mAdapter);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		DragSortListView dslv;
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "HELLO onActivityCreated - mLoaded[" + (mLoaded ? "TRUE" : "FALSE") + "]");

		dslv = (DragSortListView) getListView();
		DragSortController controller = buildController(dslv);
		dslv.setFloatViewManager(controller);
		dslv.setOnTouchListener(controller);
		dslv.setDragEnabled(true);

		dslv.setDropListener(mDropListener);
		dslv.setRemoveListener(mRemoveListener);

		if (!mLoaded) {
			// Get the ID and name of the chosen setlist from the Intent
			loadSongs();
		}

		setListAdapter(mFilter);
		mAdapter = (SetSongAdapter) getListAdapter();

		dslv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
			                        long id) {
				SetSong song = mAdapter.getItem(position);
				songSelectedListener.onSongSelected(song.id, song.filePath);

			}
		});
		registerForContextMenu(dslv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		SetSong song = mAdapter.getItem(info.position);

		if (item.getItemId() == R.id.edit) {
			Log.d(TAG,"HELLO edit Selected:");
			editSong(song);
			return true;
		} else if (item.getItemId() == R.id.delete) {
			Log.d(TAG, "Delete: "+song.id);
			mRemoveListener.remove(info.position);
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			songSelectedListener = (SongListFragment.OnSongSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSongSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView [" + mSetName + "]");

		return inflater.inflate(R.layout.editable_setlist_layout, container, false);
	}

	/**
	 * Called in onCreateView. Override this to provide a custom
	 * DragSortController.
	 */
	protected DragSortController buildController(DragSortListView dslv) {
		// defaults are
		//   dragStartMode = onDown
		//   removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		controller.setClickRemoveId(R.id.click_remove);
		// Fling to remove disabled - feedback from users is that it's too easy to
		// remove a song accidentally.
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(DragSortController.ON_LONG_PRESS);
		controller.setRemoveMode(DragSortController.FLING_REMOVE);
		return controller;
	}

	public void setSetId(long setId, String setName) {
		mSetId = setId;
		mSetName = setName;
	}

	/**
	 *
	 * Sets the song filter text - only songs which match the given string on title, artist or
	 * composer will be shown
	 * @param filter the filter
	 */
	public void setFilter(String filter) {
		mFilter = filter;
		setListAdapter(mFilter);
		mFilter="";
	}

	private void loadSongs() {
		Log.d(TAG, "loadSongs");
		// Create a SetList from the DB and load up into listView
		try {
			mSetList = new SetList2(getActivity().getContentResolver(), getString(R.string.authority), mSetId);
		} catch (Exception e) {
			SongUtils.toast(getActivity(), "Can't open set list: " + mSetId);
			e.printStackTrace();
			return;
		}
		mLoaded = true;
		getListView().invalidateViews();
	}

	public void updateList() {
		loadSongs();
		setListAdapter(mFilter);
	}

	static class SetSongAdapter extends ArrayAdapter<SetSong> {

		private final LayoutInflater mInflater;

		public SetSongAdapter(Context context, int resource, int textViewResourceId, List<SetSong> objects) {
			super(context, resource, textViewResourceId, objects);
			mInflater = LayoutInflater.from(context);
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#getView(int, android.view.View, android.view.ViewGroup)
        */
		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			SetSong song = getItem(pos);
			// A ViewHolder keeps references to children views to avoid unnecessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.edit_setlist_item, parent, false);
				// Creates a ViewHolder and store references to the child views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.artist = (TextView) convertView.findViewById(R.id.artist);
				holder.composer = (TextView) convertView.findViewById(R.id.composer);

				convertView.setTag(holder);
//				Log.d(TAG,"HELLO inflating v");
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title.setText(song.title);
			holder.artist.setText(song.artist);
			String composer = song.composer;
			if (!composer.equals("")) {
				holder.composer.setText("(" + composer + ")");
			} else {
				holder.composer.setText("");
			}

			return convertView;
		}

		static class ViewHolder {
			TextView title;
			TextView artist;
			TextView composer;
		}

	}

	public void setLoaded(boolean loaded) {
		this.mLoaded = loaded;
	}

	private void doDelete(long setId, long songId) {
		int rows = DBUtils.deleteSongFromSet(getActivity().getContentResolver(),
				getString(R.string.authority), setId, songId);
		if (rows != 1) {
			SongUtils.toast(getActivity(), "Failed to Delete - songId=" + songId);
		}
	}

	private void doSwap(int pos1, int pos2) {
		SetSong song;
		// Update the set_order for all set songs between position from and position to
		int from = pos1 < pos2 ? pos1 : pos2;
		int to = pos1 < pos2 ? pos2 : pos1;
		for (int index = from; index <= to; index++) {
			song = mAdapter.getItem(index);
			Log.d(TAG, "HELLO - Updating " + song.title + " set_order to " + index);
			// Update the song at position index - set_order = index
			DBUtils.updateSetSong(getActivity().getContentResolver(), getString(R.string.authority), mSetId, song.id, index);
		}
	}
	/**
	 * Edit the current Song
	 */
	private void editSong(SetSong song) {
		// Open the file with the EditSong Activity
		Intent myIntent = new Intent(getActivity(), EditSong.class);
		try {
			// Put the path to the song file in the intent
			myIntent.putExtra(getString(R.string.song_path), song.filePath);
			startActivity(myIntent);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( getActivity(),e.getMessage());
		}
	}
}
