/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bondevans.chordinator;

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.os.Bundle;


/**
 * Example of requesting and displaying an interstitial ad.
 */
public class InterstitialActivity extends Activity {
	protected static final String TAG = "InterstitialActivity";
	private InterstitialAd mInterstitial;
	private Timer waitTimer;
	private boolean interstitialCanceled = false;
	private static final int WAIT_TIME = 10000;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interstitial);

		mInterstitial = new InterstitialAd(this);
		mInterstitial.setAdUnitId(getResources().getString(R.string.ad_unit_id));
		mInterstitial.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				Log.d(TAG, "HELLO OnAdLoaded1");
				if (!interstitialCanceled) {
					Log.d(TAG, "HELLO OnAdLoaded2");
					waitTimer.cancel();
					mInterstitial.show();
				}
				Log.d(TAG, "HELLO OnAdLoaded3");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				super.onAdFailedToLoad(errorCode);
				Log.d(TAG, "HELLO OnAdFailedToLoad");
				finish();
			}
			@Override
			public void onAdClosed() {
				super.onAdClosed();
				Log.d(TAG, "HELLO OnAdClosed");
				finish();
			}

		});

		mInterstitial.loadAd(new AdRequest.Builder().build());
		waitTimer = new Timer();
		waitTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.d(TAG, "HELLO Ad timed out");
				interstitialCanceled = true;
				InterstitialActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "HELLO run - finishing");
						// The interstitial didn't load in a reasonable amount of time. Stop waiting for the
						// interstitial, and get outta here.
						finish();
					}
				});
			}
		}, WAIT_TIME);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "HELLO OnPause");
		// Flip the interstitialCanceled flag so that when the user comes back they aren't stuck inside
		// the splash screen activity.
		waitTimer.cancel();
		interstitialCanceled = true;
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "HELLO OnResume1");
		if (mInterstitial.isLoaded()) {
			Log.d(TAG, "HELLO OnResume2");
			// The interstitial finished loading while the app was in the background. It's up to you what
			// the behavior should be when they return. In this example, we show the interstitial since
			// it's ready.
			mInterstitial.show();
		} else if (interstitialCanceled) {
			Log.d(TAG, "HELLO OnResume3");
			// There are two ways the user could get here:
			//
			// 1. After dismissing the interstitial
			// 2. Pressing home and returning after the interstitial finished loading.
			//
			// In either case, it's awkward to leave them in the splash screen activity, so just start the
			// application.
			finish();
		}
		Log.d(TAG, "HELLO OnResume4");
	}
}
