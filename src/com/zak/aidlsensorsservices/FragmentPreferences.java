package com.zak.aidlsensorsservices;

import java.util.List;
import android.annotation.SuppressLint;
import android.preference.PreferenceActivity;
import android.util.Log;

public class FragmentPreferences extends PreferenceActivity {
	public static final String USER_TAG = "log";

	@Override
	public void onBuildHeaders(List<Header> target) {
		Log.d(USER_TAG, "onBuildHeaders() - FragmentPreferences");
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@SuppressLint("Override")
	protected boolean isValidFragment(String fragmentName) {
		if (UserPreferenceFragment.class.getName().equals(fragmentName))
			return true;
		return false;
	}
}