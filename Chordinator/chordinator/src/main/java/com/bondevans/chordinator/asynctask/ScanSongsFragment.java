package com.bondevans.chordinator.asynctask;

import java.io.File;

import com.bondevans.chordinator.ChordinatorException;
import com.bondevans.chordinator.Log;
import com.bondevans.chordinator.R;
import com.bondevans.chordinator.SongFile;
import com.bondevans.chordinator.SongUtils;
import com.bondevans.chordinator.Statics;
import com.bondevans.chordinator.db.DBUtils;
import com.bondevans.chordinator.prefs.SongPrefs;
import com.bondevans.chordinator.setlist.SetList;
import com.bondevans.chordinator.utils.SdCardFactory.SdCard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;


/**
 * TaskFragment manages a single background task and retains itself across
 * configuration changes.
 */
public class ScanSongsFragment extends Fragment {
	private static final String TAG = ScanSongsFragment.class.getSimpleName();

	/**
	 * Callback interface through which the fragment can report the task's
	 * progress and results back to the Activity.
	 */
	static interface TaskCallbacks {
		void onPreExecute();
		void onProgressUpdate(String folder, String fileFound, String fileCount, String found);
		void onCancelled();
		void onPostExecute(int found);
	}

	private TaskCallbacks mCallbacks;
	private ScanForSongsTask mTask;
	private boolean mRunning;
	private Activity mActivity;
	private boolean mFirstTime=true;

	/**
	 * Hold a reference to the parent Activity so we can report the task's current
	 * progress and results. The Android framework will pass us a reference to the
	 * newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach(Activity)");
		super.onAttach(activity);
		if (!(activity instanceof TaskCallbacks)) {
			throw new IllegalStateException("Activity must implement the TaskCallbacks interface.");
		}

		// Hold a reference to the parent Activity so we can report back the task's
		// current progress and results.
		mCallbacks = (TaskCallbacks) activity;
		mActivity = activity;
	}

	/**
	 * This method is called once when the Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate(Bundle)");
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	/**
	 * Note that this method is <em>not</em> called when the Fragment is being
	 * retained across Activity instances. It will, however, be called when its
	 * parent Activity is being destroyed for good (such as when the user clicks
	 * the back button, etc.).
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		cancel();
	}

	/*****************************/
	/***** TASK FRAGMENT API *****/
	/*****************************/

	/**
	 * Start the background task.
	 * @param doSets 
	 * @param sdCards 
	 */
	public void start(boolean onlyIfFirstTime, Boolean doSets, SdCard[] sdCards) {
		Log.d(TAG, (onlyIfFirstTime?"START IF FIRST TIME":"ALWAYS START"));
		if(onlyIfFirstTime && !mFirstTime){
			// Ignore this.  This means the calling activity has resumed.  If its the first time that
			// this task is called then just get on with it.  However, if the activity has resumed
			// after a screen orientation change then we don't want to start.
			mFirstTime=false;
			return;
		}
		Log.d(TAG, (doSets?"DOING SETS":"NOT DOING SETS"));
		if (!mRunning) {
			mTask = new ScanForSongsTask(doSets);
			mTask.execute(sdCards);
			mRunning = true;
		}
	}

	/**
	 * Cancel the background task.
	 */
	public void cancel() {
		if (mRunning) {
			mTask.cancel(false);
			mTask = null;
			mRunning = false;
		}
	}

	/**
	 * Returns the current state of the background task.
	 */
	public boolean isRunning() {
		return mRunning;
	}

	/***************************/
	/***** BACKGROUND TASK *****/
	/***************************/

	/**
	 * A dummy task that performs some (dumb) background work and proxies progress
	 * updates and results back to the Activity.
	 */
	private class ScanForSongsTask extends AsyncTask<SdCard, String, Integer> {
		private static final long FILE_TOO_BIG = 15000;	// Arbitrary value
		private static final String TAG = "ScanForSongsTask";
		String	mEncoding;
		boolean mDoSets=false;
		private Integer mFileCount=0;
		private Integer mFound=0;

		public ScanForSongsTask( boolean doSets){
			mDoSets= doSets;
		}

		@Override
		protected void onPreExecute() {
			// Proxy the call to the Activity.
			mCallbacks.onPreExecute();
			mRunning = true;
		}

		/**
		 * Note that we do NOT call the callback object's methods directly from the
		 * background thread, as this could result in a race condition.
		 */
		@Override
		protected Integer doInBackground(SdCard... paths) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
			mEncoding = settings.getString(SongPrefs.PREF_KEY_DEFAULT_ENCODING, "");
			Log.d(TAG,"XXXdoInBackground");

			String state = Environment.getExternalStorageState();
			if(state.equals(Environment.MEDIA_MOUNTED) ||
					state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){

				for( SdCard path : paths){
					Log.d(TAG, "HELLO do Path:["+path.name+"]");
					doFolder(new File(path.path), false);
				}
				// Now that we have done all songs lets do all SETLISTS in the chordinator directory
				if( mDoSets){
					doFolder(new File(Statics.CHORDINATOR_DIR), true);
				}
			}
			else{
				Log.d(TAG,"No Media Mounted!");
			}

			return 0;
		}

		void doFolder(File folder, boolean doSets){
			publishProgress(folder.getAbsolutePath(), "", mFileCount+"", mFound+"");
			Log.d(TAG, "HELLO doFolder:["+folder.getAbsolutePath()+"]");
			// Iterate through all files in the folder
			if(folder.listFiles() == null){
				Log.d(TAG, "HELLO EMPTY:"+folder.getName());
				return;
			}
			for(File file : folder.listFiles()){
				publishProgress("", file.getName(), ++mFileCount+"", mFound+"" );
				if(file.isDirectory()){
					Log.d(TAG, "HELLO folder:"+file.getName());
					doFolder(file, doSets);
				}
				else if(SongUtils.isBannedFileType(file.getName())){
					Log.d(TAG, "HELLO banned:"+file.getName());
					// Ignore
				}
				else if(file.length()>= FILE_TOO_BIG){
					Log.d(TAG, "HELLO too big:"+file.getName()+" ["+file.length()+"]");
					// Ignore
				}
				else if(file.getName().startsWith(".")){
					// Ignore
					Log.d(TAG, "HELLO DOT");
				}
				else if(doSets && file.getName().startsWith(SetList.SETLIST_PREFIX)){
					// If its a setlist - import it.
					importSet(file.getPath());
				}
				else{
					doFile(file);
				}
				if( this.isCancelled()){
					Log.d(TAG, "HELLO IS CANCELLED:"+file.getName());
					return;
				}
			}
		}

		void doFile(File file){
			SongFile sf = null;
			try {
				// Create a new SongFile - this loads up the contents of the file into the Song class
//				Log.d(TAG, "HELLO dofile:["+file.getAbsolutePath()+"]");
				sf = new SongFile(file.getPath(), null, mEncoding);
			} catch (ChordinatorException e) {
				Log.d(TAG, "HELLO ERROR!!!!!:"+file.getName());
				return;
			}
			if(sf.hasTitle){
				// Need to compare the correct path - i.e. the same that will be logged when a file is opened from the
				// file browser - this is all handled in DBUtils.
				Log.d(TAG, "HELLO IS chopro:"+file.getName());
				if(DBUtils.getSongIdFromPath(mActivity.getContentResolver(), mActivity.getString(R.string.authority), sf.getSongPath(), sf.getSongFile())==0){
					Log.d(TAG, "HELLO adding to DB:"+file.getName());
					DBUtils.addSong(mActivity.getContentResolver(), 
							mActivity.getString(R.string.authority),
							sf.getSongPath(),
							sf.getSongFile(), 
							sf.getTitleTitleCase(), 
							sf.getArtistTitleCase(), 
							sf.getComposerTitleCase());
					publishProgress("", "", mFileCount+"", ++mFound+"");

				}
				else{
					Log.d(TAG, "HELLO Already in DB:"+file.getName());
				}
			}
			else{
				Log.d(TAG, "HELLO NOT chopro:"+file.getName());
			}
		}

		private void importSet(String filePath) {
			try {
				// Load up into SetList
				SetList mySet = new SetList(mActivity.getString(R.string.authority), filePath);
				// and then create SET in DB from it
				mySet.importSet(mActivity.getContentResolver());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(String... msg) {
			// Proxy the call to the Activity.
			mCallbacks.onProgressUpdate(msg[0], msg[1], msg[2], msg[3]);
		}

		@Override
		protected void onCancelled() {
			// Proxy the call to the Activity.
			mCallbacks.onCancelled();
			mRunning = false;
		}

		@Override
		protected void onPostExecute(Integer count) {
			// Proxy the call to the Activity.
			mCallbacks.onPostExecute(mFound);
			mRunning = false;
		}
	}

	/************************/
	/***** LOGS & STUFF *****/
	/************************/

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated(Bundle)");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();
	}

}
