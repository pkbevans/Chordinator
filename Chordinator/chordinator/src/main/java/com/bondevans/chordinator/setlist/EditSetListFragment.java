package com.bondevans.chordinator.setlist;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.R;
import com.commonsware.cwac.tlv.TouchListView;

public class EditSetListFragment extends SherlockListFragment {
	private final static String TAG = "EditSetListActivity";
	public final static String 	SET_ID = "set_id";
	public final static String 	SET_NAME = "set_name";
	private static final int 	REQUEST_ADDSONGS = 0;
	private boolean 			loaded = false;
	public	SetList2 			setList;
	private IconicAdapter 		adapter=null;
	private List<String> 		songs = new ArrayList<String>();
	private long 				mSetId;
	private String				mSetName;
	private View 				mContentView;

	@Override
	public void onCreate(Bundle savedInstance) {
		Log.d(TAG, "HELLO onCreate - loaded["+(loaded?"TRUE":"FALSE")+"]");
		super.onCreate(savedInstance);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		TouchListView tlv=(TouchListView)getListView();

		if(!loaded){
			// Get the ID and name of the chosen setlist from the Intent
			loadSongs();
		}

		adapter=new IconicAdapter(songs);
		setListAdapter(adapter);

		tlv.setDropListener(onDrop);
		tlv.setRemoveListener(onRemove);
		super.onActivityCreated(savedInstanceState);
	}

	public void setSet(long setId, String setName){
		mSetId = setId;
		mSetName = setName;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "HELLO onCreatView ["+mSetName+"]");
		
		mContentView = inflater.inflate(R.layout.edit_set_layout, null);
		return mContentView;
	}

	public void loadSongs(){
		Log.d(TAG, "loadSongs");
		// Create a SetList from the file and load up into listView
		try {
			setList = new SetList2(getActivity().getContentResolver(), getString(R.string.authority), mSetId);
		} catch (Exception e) {
			SongUtils.toast(getActivity(), "Can't open set list: "+ mSetId);
			e.printStackTrace();
			return;
		}
		songs.clear();
		try {
			for( int i=0;i< setList.size();i++){
				songs.add(setList.getNextTitle());
			}
		} catch (Exception e) {
			// Ignore errors 
			Log.d(TAG, "EOF - songs.size()="+songs.size());
		}
		getListView().invalidateViews();
	}


	private TouchListView.DropListener onDrop=new TouchListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			String item=adapter.getItem(from);

			adapter.remove(item);
			adapter.insert(item, to);
			setList.moveSong(from, to);
		}
	};

	private TouchListView.RemoveListener onRemove=new TouchListView.RemoveListener() {
		@Override
		public void remove(int which) {
			Log.d(TAG, "HELLO remove1");
			adapter.remove(adapter.getItem(which));
			Log.d(TAG, "HELLO remove2");
			setList.removeSong(which);
			Log.d(TAG, "HELLO remove3");
		}
	};

	class IconicAdapter extends ArrayAdapter<String> {
		IconicAdapter(List<String> songs) {
			super(getActivity(), R.layout.edit_set_item, songs);
		}

		public View getView(int position, View convertView,
				ViewGroup parent) {
			View row=convertView;

			if (row==null) {													
				LayoutInflater inflater=LayoutInflater.from(getActivity());;
				row=inflater.inflate(R.layout.edit_set_item, parent, false);
			}

			TextView label=(TextView)row.findViewById(R.id.label);
			label.setText(songs.get(position));

			return(row);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged");
		loaded = true;
		super.onConfigurationChanged(newConfig);
	}


	public void addSongs(long setId, String setName) {
		Intent myIntent = new Intent(getActivity(), AddSongsToSetActivity.class);
		try {
			// Put the SET iD in the intent
			myIntent.putExtra(AddSongsToSetActivity.KEY_SETID, setId);
			myIntent.putExtra(AddSongsToSetActivity.KEY_SETNAME, setName);
			startActivityForResult(myIntent, REQUEST_ADDSONGS);
		}
		catch (ActivityNotFoundException e) {
			SongUtils.toast( getActivity(),e.getMessage());
		}
	}
}
