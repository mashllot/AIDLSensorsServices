package com.zak.aidlsensorsservices;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class UserPreferenceFragment extends PreferenceFragment {
	static final String USER_TAG = "log";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(USER_TAG, "onCreate() - UserPreferenceFragment");
		addPreferencesFromResource(R.xml.userpreferences);
	}
}
