package com.bondevans.chordinator.setlist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongUtils;


public class EditSetList extends ListActivity implements OnClickListener{
	private final static String TAG = "EditSetList";
	private String filePath;
	private boolean loaded = false;
	private SetList setList;
    private List<String> songs = new ArrayList<String>();
	private Button up;
	private Button down;
	private Button delete;
	private Button save;
	private int mPosition=-1;
	private ListView list;

	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_setlist);

//		Log.d(TAG, "HELLO onCreate");
		if(!loaded){
			// Get the full file path to the chosen setlist from the Intent
	        filePath = getIntent().getStringExtra(getString(R.string.song_path));
	        // Create a SetList from the file and load up into listView
			try {
				setList = new SetList(null, new File(filePath));
			} catch (Exception e) {
				SongUtils.toast(EditSetList.this, "Can't open set list: "+ filePath);
				e.printStackTrace();
				return;
			}
	        // Set up buttons
	        up = (Button) findViewById(R.id.xup);
	        up.setOnClickListener(this);
	        down = (Button) findViewById(R.id.xdown);
	        down.setOnClickListener(this);
	        delete = (Button) findViewById(R.id.delete);
	        delete.setOnClickListener(this);
	        save = (Button) findViewById(R.id.save);
	        save.setOnClickListener(this);
			loaded = true;
			
			// Start off with no buttons, because nothing selected yet
			up.setEnabled(false);
			down.setEnabled(false);
			delete.setEnabled(false);
		}

        loadSongs();
	}
	private void loadSongs(){
		songs.clear();
		try {
			songs.add(setList.getFirstSongString());
			for( int i=1;i< setList.size();i++){
				songs.add(setList.getNextSongString());
			}
		} catch (Exception e) {
			// Ignore errors due to <2 songs in set list
			Log.d(TAG, "EOF - songs.size()="+songs.size());
		}
        list = getListView();
        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, songs));
        list.setItemsCanFocus(false);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mPosition=position;
		setButtons();
	}

	private void setButtons(){
		// If its the first item disable the UP button
		if(mPosition <= 0){
			up.setEnabled(false);
		}
		else{
			up.setEnabled(true);
		}
		// If its the last item disable the DOWN button
		if(mPosition == setList.size()-1){
			down.setEnabled(false);
		}
		else{
			down.setEnabled(true);
		}
		// If something is selected then enable the delete button
		if(mPosition>=0){
			delete.setEnabled(true);
		}
		else{
			delete.setEnabled(false);
		}
	}
	
	@Override
	public void onClick(View view) {
		if( view.getId() == R.id.xup) {
			// move it up one
			if( mPosition>0){
				setList.moveUp(mPosition);
				// redisplay list
				loadSongs();
				// Set the same item as clicked
				list.setItemChecked(--mPosition, true);
			}
		}
		else if( view.getId() == R.id.xdown){
			// move it down one
			if(mPosition < songs.size()-1){
				setList.moveDown(mPosition);
				// redisplay list
				loadSongs();
				// Set the same item as clicked
				list.setItemChecked(++mPosition, true);
			}
		}
		else if( view.getId() == R.id.delete){
			setList.delete(mPosition);
			loadSongs();
			// Set the item in same position as clicked - unless it was the bottom item
			if(mPosition >= songs.size()-1){
				mPosition = songs.size()-1;
			}
			list.setItemChecked(mPosition, true);
		}
		else if( view.getId() == R.id.save) {
			// Save the setlist and exit
			saveSetList();
		}
		// Enable/disable buttons depending on position of selected item
		setButtons();
	}

	/**
	 * Writes out set list and exits
	 */
	public void saveSetList(){
		Intent myIntent = new Intent();
		setList.writeSetList();
		this.setResult(RESULT_OK, myIntent);
		this.finish();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		loaded = true;
		super.onConfigurationChanged(newConfig);
	}
}
