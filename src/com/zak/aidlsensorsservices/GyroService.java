package com.zak.aidlsensorsservices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class GyroService extends Service {

	private static final String USER_TAG = "GyroService";
	private final float NS2MS = 1.0f / 1000000.0f;

	IGETGyroData.Stub serviceBinder;

	private SensorManager sensorManager;
	String service_name = Context.SENSOR_SERVICE;
	private Sensor sensorGyroscope;

	public final static int TYPE_ALL = Sensor.TYPE_ALL;
	public final static int TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE;

	private final int DELAY_FAST = 0;
	private final int DELAY_GAME = 1;
	private final int DELAY_UI = 2;
	private final int DELAY_NORMAL = 3;

	private float[] gyroXYZ = new float[3];

	private float gyroDT;

	private long gyroTimestamp;
	private long gyroLogFileTimestamp;

	private String strAccel = "Normal";

	private PrintWriter gyroFile;

	private Date date;
	private File sdCard;
	private File path;
	private File gyro;
	private boolean recordOnChecked;

	private Object fileName;
	final private SimpleDateFormat sensorTimestamp = new SimpleDateFormat(
			"yyyy/MM/dd_HH:mm:ss:SSS");
	final private SimpleDateFormat csvDate = new SimpleDateFormat(
			"yyyy'\'MM'\'dd' - 'HH'h 'mm'min 'ss'sec - '");

	/** Finish to create Fields */

	/** Called by startService - MainActivity when Start button is ON */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(USER_TAG, "onCreate - GyroService");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorGyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

		sdCard = Environment.getExternalStorageDirectory();
		path = new File(sdCard.getAbsolutePath() + "/FilesFromService");
		path.mkdirs();
		getPreferences();
		serviceBinder = new IGETGyroData.Stub() {

			@Override
			public float[] getGyroXYZ() throws RemoteException {
				return gyroXYZ;
			}

			@Override
			public float getGyroDelay() throws RemoteException {
				return gyroDT * NS2MS;
			}
		};
	}

	/** Get Preferences */
	public void getPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		recordOnChecked = prefs.getBoolean(PreferencesActivity.RECORD_ON, true);
	}

	/**
	 * onStartCommand - Called whenever a Service is started using startService
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int getSpinnerDelay = intent.getIntExtra("gyroSelectedSpinner", 0);
		recordOnChecked = intent.getBooleanExtra("recordOnChecked", true);
		switch (getSpinnerDelay) {
		case 0:
			getSpinnerDelay = DELAY_FAST;
			strAccel = "Fast";
			break;
		case 1:
			getSpinnerDelay = DELAY_GAME;
			strAccel = "Game";
			break;
		case 2:
			getSpinnerDelay = DELAY_UI;
			strAccel = "UI";
			break;
		case 3:
			getSpinnerDelay = DELAY_NORMAL;
			strAccel = "Normal";
			break;
		default:
			break;
		}
		gyroUnregister();
		closeGyroFile();
		if (recordOnChecked && sensorGyroscope != null) {
			gyroFileName(getSpinnerDelay, strAccel);
			createGyroFile();
		}
		registerGyro(getSpinnerDelay);
		return START_NOT_STICKY;
	}

	/** Stop Sensors - Called from onStartCommand */
	private void gyroUnregister() {
		sensorManager.unregisterListener(gyroListener, sensorGyroscope);
	}

	/** Stop Log Files */
	private void closeGyroFile() {
		if (gyroFile != null) {
			gyroFile.close();
		}
	}

	/** Log Files Naming */
	private void gyroFileName(int Delay, String str) {
		if (recordOnChecked && sensorGyroscope != null) {
			date = new Date();
			fileName = csvDate.format(date);
			gyro = new File(path, fileName + "Gyroscope" + str + ".csv");
		}
	}

	/** Writing Log Files */
	private void createGyroFile() {
		if (recordOnChecked && sensorGyroscope != null) {
			try {
				gyroFile = new PrintWriter(new FileWriter(gyro, true));
				gyroFile.write("Timestamp, Gyroscope X, Gyroscope Y, Gyroscope Z, Delay (ms)\n");
			} catch (IOException ex) {
				Log.e(USER_TAG, ex.getMessage(), ex);
			}
		}
	}

	/** Start Sensors */
	private void registerGyro(int getSpinnerDelay) {
		if (sensorGyroscope != null) {
			sensorManager.registerListener(gyroListener, sensorGyroscope,
					getSpinnerDelay);
		} else {
			Toast.makeText(getBaseContext(), "Magnetic Sensor unavailable!",
					Toast.LENGTH_SHORT).show();
		}
	}

	/** 9.Called ...IBinder */
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	/** ...SensorEventListener */
	final SensorEventListener gyroListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			gyroLogFileTimestamp = (new Date()).getTime()
					+ (event.timestamp - System.nanoTime()) / 1000000L;
			if (event.sensor.getType() == TYPE_GYROSCOPE) {
				getSensorsValues(event);
				if (gyroTimestamp != 0) {
					gyroDT = event.timestamp - gyroTimestamp;
					if (gyroFile != null) {
						gyroFile.print(sensorTimestamp
								.format(gyroLogFileTimestamp));
						for (int i = 0; i < gyroXYZ.length; i++) {
							gyroFile.print("," + gyroXYZ[i]);
						}
						gyroFile.print("," + gyroDT * NS2MS);
						gyroFile.println();
					}
				}
				gyroTimestamp = event.timestamp;
			}
		}

		/** Called ...onAccuracyChanged */
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			gyroTimestamp = 0;
		}
	};

	/** getSensorsValues Called from onSensorChanged */
	private void getSensorsValues(SensorEvent event) {

		float[] values = event.values;

		switch (event.sensor.getType()) {
		case TYPE_GYROSCOPE:
			for (int i = 0; i < values.length; i++) {
				gyroXYZ[i] = values[i];
			}
			break;
		}
	}

	/** onDestroy Called when Service is stopped */
	@Override
	public void onDestroy() {
		Log.d(USER_TAG, "onDestroy - GyroService");
		gyroUnregister();
		closeGyroFile();
		super.onDestroy();
	}
}
