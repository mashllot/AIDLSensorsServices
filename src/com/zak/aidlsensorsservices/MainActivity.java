package com.zak.aidlsensorsservices;

import com.zak.aidlsensorsservices.widget.ActionBar;
import com.zak.aidlsensorsservices.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	public static final String USER_TAG = "log";
	private static final String TITLE_TAG = "AIDL Sensors Service";
	final static private long ONE_SECOND = 1000; // delayMillis 1000 * SECONDS

	int icon = R.drawable.ic_action_about;
	String tickerText = "Service started";
	String messageText = "Service is Running!";
	long when = System.currentTimeMillis();

	IGETAccelData accelService;
	IGETGyroData gyroService;
	IGETMagneData magneService;
	IGETLightData lightService;

	accelServiceConnection accelConnection;
	gyroServiceConnection gyroConnection;
	magneServiceConnection magneConnection;
	lightServiceConnection lightConnection;

	private boolean accelIsBound;
	private boolean gyroIsBound;
	private boolean magneIsBound;
	private boolean lightIsBound;

	private final int DELAY_FAST = 0;
	private final int DELAY_GAME = 1;
	private final int DELAY_UI = 2;
	private final int DELAY_NORMAL = 3;

	private int accelSpinnerDelay;
	private int gyroSpinnerDelay;
	private int magneSpinnerDelay;
	private int lightSpinnerDelay;

	private Spinner accelSpinner;
	private Spinner gyroSpinner;
	private Spinner magneSpinner;
	private Spinner lightSpinner;

	@SuppressWarnings("unused")
	private String[] accelSpinnerString;
	@SuppressWarnings("unused")
	private String[] gyroSpinnerString;
	@SuppressWarnings("unused")
	private String[] magneSpinnerString;
	@SuppressWarnings("unused")
	private String[] lightSpinnerString;

	private ToggleButton toggleServiceButton;
	private Handler handler = new Handler();
	PendingIntent pendingIntent;

	private BroadcastReceiver broadcastReceiver;
	private PendingIntent pendindIntent_autStop;
	private AlarmManager alarmManager_autoStop;

	static final private int MENU_PREFERENCES = Menu.FIRST + 1;
	private static final int SHOW_PREFERENCES = 1;

	SharedPreferences SP;
	SharedPreferences.Editor SPE;

	private boolean serviceOnChecked;
	private boolean notificationOnChecked;
	private boolean recordOnChecked;
	private boolean bNotification = true;

	private int alarmType;
	private long timeToStop;
	private int upTime;

	private float[] accelXYZ;
	private float[] gyroXYZ;
	private float[] magneXYZ;
	private float lightL;

	private float accelDelay;
	private float gyroDelay;
	private float magneDelay;
	private float lightDelay;

	private TextView[] accelTextFields = new TextView[3];
	final static int[] accelFields = { R.id.accel_x, R.id.accel_y, R.id.accel_z };
	private TextView accel_delayField;
	final static int accel_delayValue = R.id.accel_delayValue;

	private TextView[] gyroTextFields = new TextView[3];
	final static int[] gyroFields = { R.id.gyro_x, R.id.gyro_y, R.id.gyro_z };
	private TextView gyro_delayField;
	final static int gyro_delayValue = R.id.gyro_delayValue;

	private TextView[] magneTextFields = new TextView[3];
	final static int[] magneFields = { R.id.magne_x, R.id.magne_y, R.id.magne_z };
	private TextView magne_delayField;
	final static int magne_delayValue = R.id.magne_delayValue;

	private TextView lightTextField;
	final static int lightField = R.id.light_l;
	private TextView light_delayField;
	final static int light_delayValue = R.id.light_delayValue;

	int xyzFieldLength = accelFields.length;

	private accelAsyncTask accelSyncTask = null;
	private gyroAsyncTask gyroSyncTask = null;
	private magneAsyncTask magneSyncTask = null;
	private lightAsyncTask lightSyncTask = null;

	private Intent accelIntentDelay;
	private Intent gyroIntentDelay;
	private Intent magneIntentDelay;
	private Intent lightIntentDelay;
	private Notification notification;
	private NotificationManager notificationManager;

	public static final String PREFS_NAME = "spinnerDelayPrefs";

	/** Finish creating fields **/

	/** onCreate Called when the Application first created */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(USER_TAG, "onCreate - MainActivity");
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setHomeAction(new IntentAction(this, createIntent(this),
				R.drawable.ic_launcher));
		actionBar.setTitle(R.string.title_activity_main);
		actionBar.addAction(new IntentAction(this, createShareIntent(),
				R.drawable.ic_title_share_default));
		actionBar.addAction(new IntentAction(this, new Intent(this,
				FragmentPreferences.class), R.drawable.ic_action_settings));
		actionBar.addAction(new IntentAction(this, new Intent(this,
				AboutActivity.class), R.drawable.ic_action_help));

		accelSpinner = (Spinner) findViewById(R.id.accelSpinner);
		gyroSpinner = (Spinner) findViewById(R.id.gyroSpinner);
		magneSpinner = (Spinner) findViewById(R.id.magneSpinner);
		lightSpinner = (Spinner) findViewById(R.id.lightSpinner);

		// Restore preferences
		SP = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

		// Get delay from preferences
		accelSpinnerDelay = SP.getInt("accelSpinnerDelay", 3);
		gyroSpinnerDelay = SP.getInt("gyroSpinnerDelay", 3);
		magneSpinnerDelay = SP.getInt("magneSpinnerDelay", 3);
		lightSpinnerDelay = SP.getInt("lightSpinnerDelay", 3);

		ArrayAdapter<CharSequence> accelAdapter = ArrayAdapter
				.createFromResource(this, R.array.accelSpinner,
						android.R.layout.simple_spinner_item);

		ArrayAdapter<CharSequence> gyroAdapter = ArrayAdapter
				.createFromResource(this, R.array.gyroSpinner,
						android.R.layout.simple_spinner_item);

		ArrayAdapter<CharSequence> magneAdapter = ArrayAdapter
				.createFromResource(this, R.array.magneSpinner,
						android.R.layout.simple_spinner_item);

		ArrayAdapter<CharSequence> lightAdapter = ArrayAdapter
				.createFromResource(this, R.array.lightSpinner,
						android.R.layout.simple_spinner_item);

		accelAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accelSpinner.setAdapter(accelAdapter);
		accelSpinner.setSelection(accelSpinnerDelay);

		gyroAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gyroSpinner.setAdapter(gyroAdapter);
		gyroSpinner.setSelection(gyroSpinnerDelay);

		magneAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		magneSpinner.setAdapter(magneAdapter);
		magneSpinner.setSelection(magneSpinnerDelay);

		lightAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		lightSpinner.setAdapter(lightAdapter);
		lightSpinner.setSelection(lightSpinnerDelay);

		/** ACCEL_spinner.setOnItemSelectedListene Called */
		accelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				accelSpinnerDelay = arg0.getSelectedItemPosition();

				// storing string resources into Array
				accelSpinnerString = getResources().getStringArray(
						R.array.accelSpinner);

				/** Called by onCreate within spinner event */
				accelSpinnerDelay(accelSpinnerDelay);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		/** GYRO_spinner.setOnItemSelectedListene Called */
		gyroSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				gyroSpinnerDelay = arg0.getSelectedItemPosition();

				gyroSpinnerString = getResources().getStringArray(
						R.array.gyroSpinner);

				gyroSpinnerDelay(gyroSpinnerDelay);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		/** MAGNE_spinner.setOnItemSelectedListene Called */
		magneSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				magneSpinnerDelay = arg0.getSelectedItemPosition();

				magneSpinnerString = getResources().getStringArray(
						R.array.magneSpinner);

				magneSpinnerDelay(magneSpinnerDelay);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		/** LIGHT_spinner.setOnItemSelectedListene Called */
		lightSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				lightSpinnerDelay = arg0.getSelectedItemPosition();

				lightSpinnerString = getResources().getStringArray(
						R.array.lightSpinner);

				lightSpinnerDelay(lightSpinnerDelay);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		for (int i = 0; i < xyzFieldLength; i++) {
			accelTextFields[i] = (TextView) findViewById(accelFields[i]);
			gyroTextFields[i] = (TextView) findViewById(gyroFields[i]);
			magneTextFields[i] = (TextView) findViewById(magneFields[i]);
		}
		lightTextField = (TextView) findViewById(lightField);
		accel_delayField = (TextView) findViewById(accel_delayValue);
		gyro_delayField = (TextView) findViewById(gyro_delayValue);
		magne_delayField = (TextView) findViewById(magne_delayValue);
		light_delayField = (TextView) findViewById(light_delayValue);

		toggleServiceButton = (ToggleButton) findViewById(R.id.toggleServiceButton);

		setupBroadcastReceiver();
	}

	public static Intent createIntent(Context context) {
		Intent i = new Intent(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return i;
	}

	private Intent createShareIntent() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT,
				"https://github.com/naimbic/Android_AIDL_Sensors_Plus.");
		return Intent.createChooser(intent, "Share");
	}

	/** Start AsyncTask */
	private void startAccel() {
		accelSyncTask = new accelAsyncTask();
		accelSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
	}

	private void startGyro() {
		gyroSyncTask = new gyroAsyncTask();
		gyroSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
	}

	private void startMagne() {
		magneSyncTask = new magneAsyncTask();
		magneSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
	}

	private void startLight() {
		lightSyncTask = new lightAsyncTask();
		lightSyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
	}

	private void aTaskProcess() {
		try {
			// Is better to keep the SERVICES alive and do not Use SLEEP.
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class accelAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			int progress = ((Integer[]) params)[0];
			do {
				aTaskProcess();
				progress++;
				publishProgress(progress);
				try {
					if (accelService != null) {
						accelXYZ = accelService.getAccelXYZ();
						accelDelay = accelService.getAccelDelay();

						displaySensorValue(String.valueOf(accelXYZ[0]),
								String.valueOf(accelXYZ[1]),
								String.valueOf(accelXYZ[2]),
								String.valueOf(accelDelay));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} while (accelService != null);
			return progress;
		}

		protected void onProgressUpdate(Integer... values) {
			@SuppressWarnings("unused")
			int progress = ((Integer[]) values)[0];
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled(Integer result) {
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}
	}

	class gyroAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			int progress = ((Integer[]) params)[0];
			do {
				aTaskProcess();
				progress++;
				publishProgress(progress);
				try {
					if (gyroService != null) {
						gyroXYZ = gyroService.getGyroXYZ();
						gyroDelay = gyroService.getGyroDelay();

						displayGSensorValue(String.valueOf(gyroXYZ[0]),
								String.valueOf(gyroXYZ[1]),
								String.valueOf(gyroXYZ[2]),
								String.valueOf(gyroDelay));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} while (gyroService != null);
			return progress;
		}

		protected void onProgressUpdate(Integer... values) {
			@SuppressWarnings("unused")
			int progress = ((Integer[]) values)[0];
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled(Integer result) {
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}
	}

	class magneAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			int progress = ((Integer[]) params)[0];
			do {
				aTaskProcess();
				progress++;
				publishProgress(progress);
				try {
					if (magneService != null) {
						magneXYZ = magneService.getMagneXYZ();
						magneDelay = magneService.getMagneDelay();

						displayMSensorValue(String.valueOf(magneXYZ[0]),
								String.valueOf(magneXYZ[1]),
								String.valueOf(magneXYZ[2]),
								String.valueOf(magneDelay));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} while (magneService != null);
			return progress;
		}

		protected void onProgressUpdate(Integer... values) {
			@SuppressWarnings("unused")
			int progress = ((Integer[]) values)[0];
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled(Integer result) {
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}
	}

	class lightAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			int progress = ((Integer[]) params)[0];
			do {
				aTaskProcess();
				progress++;
				publishProgress(progress);
				try {
					if (lightService != null) {
						lightL = lightService.getLightL();
						lightDelay = lightService.getLightDelay();
						displayLSensorValue(String.valueOf(lightL),
								String.valueOf(lightDelay));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} while (lightService != null);
			return progress;
		}

		protected void onProgressUpdate(Integer... values) {
			@SuppressWarnings("unused")
			int progress = ((Integer[]) values)[0];
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled(Integer result) {
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}
	}

	public void displaySensorValue(final String x, final String y,
			final String z, final String del) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				accelTextFields[0].setText(x);
				accelTextFields[1].setText(y);
				accelTextFields[2].setText(z);
				accel_delayField.setText(del);
			}
		});
	}

	public void displayGSensorValue(final String x, final String y,
			final String z, final String del) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				gyroTextFields[0].setText(x);
				gyroTextFields[1].setText(y);
				gyroTextFields[2].setText(z);
				gyro_delayField.setText(del);
			}
		});
	}

	public void displayMSensorValue(final String x, final String y,
			final String z, final String del) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				magneTextFields[0].setText(x);
				magneTextFields[1].setText(y);
				magneTextFields[2].setText(z);
				magne_delayField.setText(del);
			}
		});
	}

	public void displayLSensorValue(final String light, final String del) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				lightTextField.setText(light);
				light_delayField.setText(del);
			}
		});
	}

	/** 2.onCreateOptionsMenu Called */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {

		case (MENU_PREFERENCES): {
			Class<? extends Activity> c = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? PreferencesActivity.class
					: FragmentPreferences.class;
			Intent i = new Intent(this, c);

			startActivityForResult(i, SHOW_PREFERENCES);

			return true;
		}
		}
		return false;
	}

	/** accelSpinnerDelay Called when Spinner changed */
	private void accelSpinnerDelay(int index) {
		if (toggleServiceButton.isChecked()) {
			switch (index) {
			case 0:
				accelSpinnerDelay = DELAY_FAST;
				break;
			case 1:
				accelSpinnerDelay = DELAY_GAME;
				break;
			case 2:
				accelSpinnerDelay = DELAY_UI;
				break;
			case 3:
				accelSpinnerDelay = DELAY_NORMAL;
				break;
			default:
				break;
			}
			stopAccelService();
			startAccelService();
			doAccelBindService();
			startAccel();
		} else {
			Log.d(USER_TAG, "spinnerDelay - OFF");
			if (accelService != null) {
				stopAccelService();
			}
		}
		accelSpinnerDelay = index;
		SPE = SP.edit();
		SPE.putInt("accelSpinnerDelay", accelSpinnerDelay);
		SPE.commit();
	}

	/** gyroSpinnerDelay Called when Spinner changed */
	private void gyroSpinnerDelay(int index) {
		if (toggleServiceButton.isChecked()) {
			switch (index) {
			case 0:
				gyroSpinnerDelay = DELAY_FAST;
				break;
			case 1:
				gyroSpinnerDelay = DELAY_GAME;
				break;
			case 2:
				gyroSpinnerDelay = DELAY_UI;
				break;
			case 3:
				gyroSpinnerDelay = DELAY_NORMAL;
				break;
			default:
				break;
			}
			stopGyroService();
			startGyroService();
			doGyroBindService();
			startGyro();
		} else {
			if (gyroService != null) {
				stopGyroService();
			}
		}
		gyroSpinnerDelay = index;
		SPE = SP.edit();
		SPE.putInt("gyroSpinnerDelay", gyroSpinnerDelay);
		SPE.commit();
	}

	/** magneSpinnerDelay Called when Spinner changed */
	private void magneSpinnerDelay(int index) {
		if (toggleServiceButton.isChecked()) {
			switch (index) {
			case 0:
				magneSpinnerDelay = DELAY_FAST;
				break;
			case 1:
				magneSpinnerDelay = DELAY_GAME;
				break;
			case 2:
				magneSpinnerDelay = DELAY_UI;
				break;
			case 3:
				magneSpinnerDelay = DELAY_NORMAL;
				break;
			default:
				break;
			}
			stopMagneService();
			startMagneService();
			doMagneBindService();
			startMagne();
		} else {
			if (magneService != null) {
				stopMagneService();
			}
		}
		magneSpinnerDelay = index;
		SPE = SP.edit();
		SPE.putInt("magneSpinnerDelay", magneSpinnerDelay);
		SPE.commit();
	}

	/** lightSpinnerDelay Called when Spinner changed */
	private void lightSpinnerDelay(int index) {
		if (toggleServiceButton.isChecked()) {
			switch (index) {
			case 0:
				lightSpinnerDelay = DELAY_FAST;
				break;
			case 1:
				lightSpinnerDelay = DELAY_GAME;
				break;
			case 2:
				lightSpinnerDelay = DELAY_UI;
				break;
			case 3:
				lightSpinnerDelay = DELAY_NORMAL;
				break;
			default:
				break;
			}
			stopLightService();
			startLightService();
			doLightBindService();
			startLight();
		} else {
			if (lightService != null) {
				stopLightService();
			}
		}
		lightSpinnerDelay = index;
		SPE = SP.edit();
		SPE.putInt("lightSpinnerDelay", lightSpinnerDelay);
		SPE.commit();
	}

	/** 5.Called by pressing Start/Stop button */
	public void onServiceToggleClicked(View v) {
		boolean on = ((ToggleButton) v).isChecked();
		if (on) {
			bNotification = true;
			startAccelService();
			startGyroService();
			startMagneService();
			startLightService();
			doAccelBindService();
			doGyroBindService();
			doMagneBindService();
			doLightBindService();
		} else {
			bNotification = false;
			stopAccelService();
			stopGyroService();
			stopMagneService();
			stopLightService();
			taskCancel();
		}
		ifNotificationOnChecked();
		ifServiceOnChecked();
	}

	public void notifyMain(String leftText, String rightText) {
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.notify_main);
		remoteViews.setTextViewText(R.id.upTime_text_view, leftText);
		remoteViews.setTextViewText(R.id.recording_text_view, rightText);

		Intent notificationIntent = new Intent(this.getApplicationContext(),
				MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(
				this.getApplicationContext(), 0, notificationIntent, 0);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification.Builder(this)
				.setSmallIcon(icon)
				.setTicker(tickerText)
				.setContentText(messageText)
				.setWhen(when)
				.setContentTitle(TITLE_TAG)
				.setOngoing(true)
				// .setDefaults(
				// Notification.DEFAULT_SOUND
				// | Notification.DEFAULT_VIBRATE
				// | Notification.DEFAULT_LIGHTS)
				.setPriority(Notification.PRIORITY_MAX)
				.setVibrate(new long[] { 100, 100, 100, 100 })
				.setLights(Color.BLUE, 0, 1).build();

		notification.bigContentView = remoteViews;
		notification.contentIntent = contentIntent;
		notificationManager.notify(0, notification);
	}

	private void startAccelService() {
		accelConnection = new accelServiceConnection();

		accelIntentDelay = new Intent(this, AccelService.class);
		accelIntentDelay.putExtra("accelSelectedSpinner", accelSpinnerDelay);
		accelIntentDelay.putExtra("recordOnChecked", recordOnChecked);
		pendingIntent = PendingIntent.getService(this, 0, accelIntentDelay,
				PendingIntent.FLAG_UPDATE_CURRENT);
		startService(accelIntentDelay);

		accelIntentDelay.setClassName(this, AccelService.class.getName());
	}

	private void startGyroService() {
		gyroConnection = new gyroServiceConnection();

		gyroIntentDelay = new Intent(this, GyroService.class);
		gyroIntentDelay.putExtra("gyroSelectedSpinner", gyroSpinnerDelay);
		gyroIntentDelay.putExtra("recordOnChecked", recordOnChecked);
		pendingIntent = PendingIntent.getService(this, 0, gyroIntentDelay,
				PendingIntent.FLAG_UPDATE_CURRENT);
		startService(gyroIntentDelay);

		gyroIntentDelay.setClassName(this, GyroService.class.getName());
	}

	private void startMagneService() {
		magneConnection = new magneServiceConnection();
		magneIntentDelay = new Intent(this, MagneService.class);
		magneIntentDelay.putExtra("magneSelectedSpinner", magneSpinnerDelay);
		magneIntentDelay.putExtra("recordOnChecked", recordOnChecked);
		pendingIntent = PendingIntent.getService(this, 0, magneIntentDelay,
				PendingIntent.FLAG_UPDATE_CURRENT);
		startService(magneIntentDelay);

		magneIntentDelay.setClassName(this, MagneService.class.getName());
	}

	private void startLightService() {
		lightConnection = new lightServiceConnection();
		lightIntentDelay = new Intent(this, LightService.class);
		lightIntentDelay.putExtra("lightSelectedSpinner", lightSpinnerDelay);
		lightIntentDelay.putExtra("recordOnChecked", recordOnChecked);
		pendingIntent = PendingIntent.getService(this, 0, lightIntentDelay,
				PendingIntent.FLAG_UPDATE_CURRENT);
		startService(lightIntentDelay);

		lightIntentDelay.setClassName(this, LightService.class.getName());
	}

	/** doBindServiceCalled by onServiceToggleClicked when Start button is ON */
	void doAccelBindService() {
		bindService(accelIntentDelay, accelConnection, Context.BIND_AUTO_CREATE);
		accelIsBound = true;
	}

	void doGyroBindService() {
		bindService(gyroIntentDelay, gyroConnection, Context.BIND_AUTO_CREATE);
		gyroIsBound = true;
	}

	void doMagneBindService() {
		bindService(magneIntentDelay, magneConnection, Context.BIND_AUTO_CREATE);
		magneIsBound = true;
	}

	void doLightBindService() {
		bindService(lightIntentDelay, lightConnection, Context.BIND_AUTO_CREATE);
		lightIsBound = true;
	}

	/**
	 * stopAccelService Called by onServiceToggleClicked when Start button is
	 * OFF Changer to Runnable for handler call.
	 */
	private void stopAccelService() {
		stopService(new Intent(MainActivity.this, AccelService.class));
		// alarmManager_autoStop.cancel(pendindIntent_autStop);
		doAccelUnbindService();
		accelService = null;
		accelConnection = null;
		if (accelSyncTask != null) {
			accelSyncTask.cancel(true);
		}
	}

	private void stopGyroService() {
		stopService(new Intent(MainActivity.this, GyroService.class));
		doGyroUnbindService();
		gyroService = null;
		gyroConnection = null;
		if (gyroSyncTask != null) {
			gyroSyncTask.cancel(true);
		}
	}

	private void stopMagneService() {
		stopService(new Intent(MainActivity.this, MagneService.class));
		doMagneUnbindService();
		magneService = null;
		magneConnection = null;
		if (magneSyncTask != null) {
			magneSyncTask.cancel(true);
		}
	}

	private void stopLightService() {
		stopService(new Intent(MainActivity.this, LightService.class));
		doLightUnbindService();
		lightService = null;
		lightConnection = null;
		if (lightSyncTask != null) {
			lightSyncTask.cancel(true);
		}
	}

	/** Called after pressing Stop button */
	void doAccelUnbindService() {
		if (accelIsBound) {
			unbindService(accelConnection);
			accelIsBound = false;
		}
	}

	void doGyroUnbindService() {
		if (gyroIsBound) {
			unbindService(gyroConnection);
			gyroIsBound = false;
		}
	}

	void doMagneUnbindService() {
		if (magneIsBound) {
			unbindService(magneConnection);
			magneIsBound = false;
		}
	}

	void doLightUnbindService() {
		if (lightIsBound) {
			unbindService(lightConnection);
			lightIsBound = false;
		}
	}

	public void taskCancel() {
		if (accelSyncTask != null) {
			accelSyncTask.cancel(true);
		}
		if (gyroSyncTask != null) {
			gyroSyncTask.cancel(true);
		}
		if (magneSyncTask != null) {
			magneSyncTask.cancel(true);
		}
		if (lightSyncTask != null) {
			lightSyncTask.cancel(true);
		}
	}

	/**
	 * ServiceConnection Called when Service is started / Handles the connection
	 * between the service and activity
	 */
	class accelServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder bService) {
			accelService = IGETAccelData.Stub.asInterface(bService);
			startAccel();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			accelService = null;
		}
	}

	class gyroServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder bService) {
			gyroService = IGETGyroData.Stub.asInterface(bService);
			startGyro();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			gyroService = null;
		}
	}

	class magneServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder bService) {
			magneService = IGETMagneData.Stub.asInterface(bService);
			startMagne();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			magneService = null;
		}
	}

	class lightServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder bService) {
			lightService = IGETLightData.Stub.asInterface(bService);
			startLight();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			lightService = null;
		}
	}

	/** Check if notificationOnChecked is true */
	public void ifNotificationOnChecked() {
		if (notificationOnChecked && bNotification) {
			notifyMain("Autostop:\n" + upTime + " s", "Recording...");
		} else {
			if (notificationManager != null) {
				notificationManager.cancelAll();
			}
		}
	}

	/** Check if serviceOnChecked is true */
	public void ifServiceOnChecked() {

		if (serviceOnChecked) {
			Log.d(USER_TAG, "serviceOnChecked - ifServiceOnChecked: "
					+ serviceOnChecked);
			alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
			timeToStop = SystemClock.elapsedRealtime() + upTime * ONE_SECOND;
			alarmManager_autoStop.set(alarmType, timeToStop,
					pendindIntent_autStop);
			Log.d(USER_TAG, "upTime: " + upTime + " --- serviceOnChecked: "
					+ serviceOnChecked);
		} else if (alarmManager_autoStop != null) {
			alarmManager_autoStop.cancel(pendindIntent_autStop);
			Log.d(USER_TAG, "alarmManager_autoStop.cancel - ifServiceOnChecked");
		}
	}

	/** Called from onCreate */
	private void setupBroadcastReceiver() {
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (accelService != null && gyroService != null
						&& magneService != null && lightService != null) {
					Toast.makeText(context, "Services are destroying...",
							Toast.LENGTH_SHORT).show();
					stopAccelService();
					stopGyroService();
					stopMagneService();
					stopLightService();
					taskCancel();
					bNotification = false;
					ifNotificationOnChecked();
				}
				toggleServiceButton.setChecked(false);
			}
		};
		registerReceiver(broadcastReceiver, new IntentFilter(
				"com.zak.aidlsensorsservices.autoStop"));
		pendindIntent_autStop = PendingIntent.getBroadcast(this, 0, new Intent(
				"com.zak.aidlsensorsservices.autoStop"), 0);
		alarmManager_autoStop = (AlarmManager) (this
				.getSystemService(Context.ALARM_SERVICE));
	}

	/** Get Preferences */
	public void getPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		upTime = Integer.parseInt(prefs.getString(
				PreferencesActivity.UPTIME_FREQ, "300"));
		serviceOnChecked = prefs.getBoolean(PreferencesActivity.SERVICE_ON,
				true);
		notificationOnChecked = prefs.getBoolean(
				PreferencesActivity.NOTIFICATION_ON, true);

		recordOnChecked = prefs.getBoolean(PreferencesActivity.RECORD_ON, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(USER_TAG, "onResume - MainActivity");
		bNotification = true;
		getPreferences();
		if (serviceOnChecked) {
			Log.d(USER_TAG, "serviceOnChecked - onResume: " + serviceOnChecked
					+ " - " + upTime);
			// setupBroadcastReceiver();
			ifServiceOnChecked();
		} else if (alarmManager_autoStop != null) {
			alarmManager_autoStop.cancel(pendindIntent_autStop);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(USER_TAG, "onPause - MainActivity");
	}

	@Override
	public void onStop() {
		super.onStop();
		taskCancel();
		Log.d(USER_TAG, "onStop - MainActivity");
	}

	@Override
	public void onDestroy() {
		Log.d(USER_TAG, "onDestroy - MainActivity");

		bNotification = false;
		ifNotificationOnChecked();
		alarmManager_autoStop.cancel(pendindIntent_autStop);
		unregisterReceiver(broadcastReceiver);
		stopAccelService();
		stopGyroService();
		stopMagneService();
		stopLightService();
		taskCancel();
		setupBroadcastReceiver();
		super.onDestroy();
	}

	/** Called When backKey pressed */
	public void onBackPressed() {
		Log.d(USER_TAG, "onBackPressed");
		moveTaskToBack(true);
	}
}
