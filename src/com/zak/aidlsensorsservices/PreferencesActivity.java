package com.zak.aidlsensorsservices;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class PreferencesActivity extends Activity {
	public static final String RECORD_ON = "RECORD_ON";
	public static final String NOTIFICATION_ON = "NOTIFICATION_ON";
	public static final String SERVICE_ON = "SERVICE_ON";
	public static final String UPTIME_FREQ = "UPTIME_FREQ";
	static final String TAG = "log";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate() - PreferencesActivity");
		// 1. Get an instance of the LayoutInflater
		// 2. Specify the XML to inflate 3. Use the returned View

		LayoutInflater inflater = LayoutInflater.from(PreferencesActivity.this);
		@SuppressWarnings("unused")
		View theInflatedView = inflater.inflate(R.xml.userpreferences, null);
	}
}
