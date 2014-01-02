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
public class MagneService extends Service {

	private static final String USER_TAG = "MagneService";
	private final float NS2MS = 1.0f / 1000000.0f;

	IGETMagneData.Stub serviceBinder;

	private SensorManager sensorManager;
	String service_name = Context.SENSOR_SERVICE;
	private Sensor sensorMagnetic;

	public final static int TYPE_ALL = Sensor.TYPE_ALL;
	public final static int TYPE_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;

	private final int DELAY_FAST = 0;
	private final int DELAY_GAME = 1;
	private final int DELAY_UI = 2;
	private final int DELAY_NORMAL = 3;

	private float[] magneXYZ = new float[3];

	private float magneDT;

	private long magneTimestamp;
	private long magneLogFileTimestamp;

	private String strAccel = "Normal";

	private PrintWriter magneFile;

	private Date date;
	private File sdCard;
	private File path;
	private File magne;
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
		Log.d(USER_TAG, "onCreate - MagneService");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMagnetic = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD);

		sdCard = Environment.getExternalStorageDirectory();
		path = new File(sdCard.getAbsolutePath() + "/FilesFromService");
		path.mkdirs();
		serviceBinder = new IGETMagneData.Stub() {

			@Override
			public float[] getMagneXYZ() throws RemoteException {
				return magneXYZ;
			}

			@Override
			public float getMagneDelay() throws RemoteException {
				return magneDT * NS2MS;
			}
		};
	}

	/**
	 * onStartCommand - Called whenever a Service is started using startService
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int getSpinnerDelay = intent.getIntExtra("magneSelectedSpinner", 0);
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

		magneUnregister();
		closeMagneFile();
		if (recordOnChecked && sensorMagnetic != null) {
			magneFileName(getSpinnerDelay, strAccel);
			createMagneFile();
		}
		registerMagne(getSpinnerDelay);
		return START_NOT_STICKY;
	}

	/** Stop Sensors - Called from onStartCommand */
	private void magneUnregister() {
		sensorManager.unregisterListener(magneListener, sensorMagnetic);
	}

	/** Stop Log Files */
	private void closeMagneFile() {
		if (magneFile != null) {
			magneFile.close();
		}
	}

	/** Log Files Naming */
	private void magneFileName(int Delay, String str) {
		if (recordOnChecked && sensorMagnetic != null) {
			date = new Date();
			fileName = csvDate.format(date);
			magne = new File(path, fileName + "Magnetic" + str + ".csv");
		}
	}

	/** Writing Log Files */
	private void createMagneFile() {
		if (recordOnChecked && sensorMagnetic != null) {
			try {
				magneFile = new PrintWriter(new FileWriter(magne, true));
				magneFile
						.write("Timestamp, Magnetic X, Magnetic Y, Magnetic Z, Delay (ms)\n");
			} catch (IOException ex) {
				Log.e(USER_TAG, ex.getMessage(), ex);
			}
		}
	}

	/** Start Sensors */
	private void registerMagne(int getSpinnerDelay) {
		if (sensorMagnetic != null) {
			sensorManager.registerListener(magneListener, sensorMagnetic,
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

	final SensorEventListener magneListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			magneLogFileTimestamp = (new Date()).getTime()
					+ (event.timestamp - System.nanoTime()) / 1000000L;
			if (event.sensor.getType() == TYPE_MAGNETIC_FIELD) {
				getSensorsValues(event);
				if (magneTimestamp != 0) {
					magneDT = event.timestamp - magneTimestamp;
					if (magneFile != null) {
						magneFile.print(sensorTimestamp
								.format(magneLogFileTimestamp));
						for (int i = 0; i < magneXYZ.length; i++) {
							magneFile.print("," + magneXYZ[i]);
						}
						magneFile.print("," + magneDT * NS2MS);
						magneFile.println();
					}
				}
				magneTimestamp = event.timestamp;
			}
		}

		/** Called ...onAccuracyChanged */
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			magneTimestamp = 0;
		}
	};

	/** getSensorsValues Called from onSensorChanged */
	private void getSensorsValues(SensorEvent event) {

		float[] values = event.values;
		switch (event.sensor.getType()) {
		case TYPE_MAGNETIC_FIELD:
			for (int i = 0; i < values.length; i++) {
				magneXYZ[i] = values[i];
			}
			break;
		}
	}

	/** onDestroy Called when Service is stopped */
	@Override
	public void onDestroy() {
		Log.d(USER_TAG, "onDestroy - MagneService");
		magneUnregister();
		closeMagneFile();
		super.onDestroy();
	}
}
