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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class LightService extends Service {

	private static final String USER_TAG = "LightService";
	private final float NS2MS = 1.0f / 1000000.0f;

	IGETLightData.Stub serviceBinder;

	private SensorManager sensorManager;
	String service_name = Context.SENSOR_SERVICE;
	private Sensor sensorLight;

	public final static int TYPE_ALL = Sensor.TYPE_ALL;
	public final static int TYPE_LIGHT = Sensor.TYPE_LIGHT;

	private final int DELAY_FAST = 0;
	private final int DELAY_GAME = 1;
	private final int DELAY_UI = 2;
	private final int DELAY_NORMAL = 3;

	private float lightL;

	private float lightDT;

	private long lightTimestamp;
	private long lightLogFileTimestamp;

	private String strAccel = "Normal";

	private PrintWriter lightFile;

	private Date date;
	private File sdCard;
	private File path;
	private boolean recordOnChecked;

	private File light;
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
		Log.d(USER_TAG, "onCreate - LightService");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorLight = sensorManager.getDefaultSensor(TYPE_LIGHT);

		sdCard = Environment.getExternalStorageDirectory();
		path = new File(sdCard.getAbsolutePath() + "/FilesFromService");
		path.mkdirs();
		serviceBinder = new IGETLightData.Stub() {

			@Override
			public float getLightL() throws RemoteException {
				return lightL;
			}

			@Override
			public float getLightDelay() throws RemoteException {
				return lightDT * NS2MS;
			}
		};
	}

	/**
	 * onStartCommand - Called whenever a Service is started using startService
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int getSpinnerDelay = intent.getIntExtra("lightSelectedSpinner", 0);
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

		lightUnregister();
		closeLightFile();
		if (recordOnChecked && sensorLight != null) {
			lightFileName(getSpinnerDelay, strAccel);
			createLightFile();
		}
		registerLight(getSpinnerDelay);
		return START_NOT_STICKY;
	}

	/** Stop Sensors - Called from onStartCommand */
	private void lightUnregister() {
		sensorManager.unregisterListener(lightListener, sensorLight);
	}

	/** Stop Log Files */
	private void closeLightFile() {
		if (lightFile != null) {
			lightFile.close();
		}
	}

	/** Log Files Naming */
	private void lightFileName(int Delay, String str) {
		if (recordOnChecked && sensorLight != null) {
			date = new Date();
			fileName = csvDate.format(date);
			light = new File(path, fileName + "Light" + str + ".csv");
		}
	}

	/** Writing Log Files */
	private void createLightFile() {
		if (recordOnChecked && sensorLight != null) {
			try {
				lightFile = new PrintWriter(new FileWriter(light, true));
				lightFile.write("Timestamp, Light L, Delay (ms)\n");
			} catch (IOException ex) {
				Log.e(USER_TAG, ex.getMessage(), ex);
			}
		}
	}

	/** Start Sensors */
	private void registerLight(int getSpinnerDelay) {
		if (sensorLight != null) {
			sensorManager.registerListener(lightListener, sensorLight,
					getSpinnerDelay);
		} else {
			Toast.makeText(getBaseContext(), "Magnetic Sensor unavailable!",
					Toast.LENGTH_SHORT).show();
		}
	}

	/** Called ...IBinder */
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	/** ...SensorEventListener */

	final SensorEventListener lightListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			lightLogFileTimestamp = (new Date()).getTime()
					+ (event.timestamp - System.nanoTime()) / 1000000L;
			if (event.sensor.getType() == TYPE_LIGHT) {
				getSensorsValues(event);
				if (lightTimestamp != 0) {
					lightDT = event.timestamp - lightTimestamp;
					if (lightFile != null) {
						lightFile.print(sensorTimestamp
								.format(lightLogFileTimestamp));
						lightFile.print("," + lightL);
						lightFile.print("," + lightDT * NS2MS);
						lightFile.println();
					}
				}
				lightTimestamp = event.timestamp;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			lightTimestamp = 0;
		}
	};

	/** getSensorsValues Called from onSensorChanged */
	private void getSensorsValues(SensorEvent event) {

		float[] values = event.values;
		switch (event.sensor.getType()) {
		case TYPE_LIGHT:
			lightL = values[0];
			break;
		}
	}

	/** 14.onDestroy Called when Service is stopped */
	@Override
	public void onDestroy() {
		Log.d(USER_TAG, "onDestroy - LightService");
		lightUnregister();
		closeLightFile();
		super.onDestroy();
	}
}
