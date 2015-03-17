package com.bondevans.chordinator.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.bondevans.chordinator.R;

public class ChordinatorPrefsFragment extends PreferenceFragment {
//	private static final String TAG = "ChordinatorPrefsFragment";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
}
