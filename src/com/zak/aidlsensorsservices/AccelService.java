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
public class AccelService extends Service {

	private static final String USER_TAG = "AccelService";
	private final float NS2MS = 1.0f / 1000000.0f;

	IGETAccelData.Stub serviceBinder;

	private SensorManager sensorManager;
	String service_name = Context.SENSOR_SERVICE;
	private Sensor sensorAccelerometer;

	public final static int TYPE_ALL = Sensor.TYPE_ALL;
	public final static int TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;

	private final int DELAY_FAST = 0;
	private final int DELAY_GAME = 1;
	private final int DELAY_UI = 2;
	private final int DELAY_NORMAL = 3;

	private float[] accelXYZ = new float[3];

	private float accelDT;

	private long accelTimestamp;
	private long accelLogFileTimestamp;

	private String strAccel = "Normal";

	private PrintWriter accelFile;

	private Date date;
	private File sdCard;
	private File path;
	private File accel;
	private Object fileName;
	private boolean recordOnChecked;

	final private SimpleDateFormat sensorTimestamp = new SimpleDateFormat(
			"yyyy/MM/dd_HH:mm:ss:SSS");
	final private SimpleDateFormat csvDate = new SimpleDateFormat(
			"yyyy'\'MM'\'dd' - 'HH'h 'mm'min 'ss'sec - '");

	/** Finish to create Fields */

	/** Called by startService - MainActivity when Start button is ON */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(USER_TAG, "onCreate - AccelService");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorAccelerometer = sensorManager
				.getDefaultSensor(TYPE_ACCELEROMETER);

		sdCard = Environment.getExternalStorageDirectory();
		path = new File(sdCard.getAbsolutePath() + "/FilesFromService");
		path.mkdirs();
		serviceBinder = new IGETAccelData.Stub() {

			@Override
			public float[] getAccelXYZ() throws RemoteException {
				return accelXYZ;
			}

			@Override
			public float getAccelDelay() throws RemoteException {
				return accelDT * NS2MS;
			}
		};
	}

	/**
	 * onStartCommand - Called whenever a Service is started using startService
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int getSpinnerDelay = intent.getIntExtra("accelSelectedSpinner", 0);
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
		accelUnregister();
		closeAccelFile();
		if (recordOnChecked && sensorAccelerometer != null) {
			Log.d(USER_TAG, "record: " + recordOnChecked);
			accelFileName(getSpinnerDelay, strAccel);
			createAccelFile();
		}
		registerAccel(getSpinnerDelay);
		return START_NOT_STICKY;
	}

	/** Stop Sensors - Called from onStartCommand */
	private void accelUnregister() {
		sensorManager.unregisterListener(accelListener, sensorAccelerometer);
	}

	/** Stop Log Files */
	private void closeAccelFile() {
		if (accelFile != null) {
			accelFile.close();
		}
	}

	/** Log Files Naming */
	private void accelFileName(int Delay, String str) {
		if (recordOnChecked && sensorAccelerometer != null) {
			date = new Date();
			fileName = csvDate.format(date);
			accel = new File(path, fileName + "Accelerometer" + str + ".csv");
		}
	}

	/** Writing Log Files */
	private void createAccelFile() {
		if (recordOnChecked && sensorAccelerometer != null) {
			try {
				accelFile = new PrintWriter(new FileWriter(accel, true));
				accelFile
						.write("Timestamp, Accelerometer X, Accelerometer Y, Accelerometer Z, Delay (ms)\n");
			} catch (IOException ex) {
				Log.e(USER_TAG, ex.getMessage(), ex);
			}
		}
	}

	/** Start Sensors */
	private void registerAccel(int getSpinnerDelay) {
		if (sensorAccelerometer != null) {
			sensorManager.registerListener(accelListener, sensorAccelerometer,
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
	final SensorEventListener accelListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			accelLogFileTimestamp = (new Date()).getTime()
					+ (event.timestamp - System.nanoTime()) / 1000000L;
			if (event.sensor.getType() == TYPE_ACCELEROMETER) {
				getSensorsValues(event);
				if (accelTimestamp != 0) {
					accelDT = event.timestamp - accelTimestamp;
					if (accelFile != null) {
						accelFile.print(sensorTimestamp
								.format(accelLogFileTimestamp));
						for (int i = 0; i < accelXYZ.length; i++) {
							accelFile.print("," + accelXYZ[i]);
						}
						accelFile.print("," + accelDT * NS2MS);
						accelFile.println();
					}
				}
				accelTimestamp = event.timestamp;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			accelTimestamp = 0;
		}
	};

	/** getSensorsValues Called from onSensorChanged */
	private void getSensorsValues(SensorEvent event) {

		float[] values = event.values;
		switch (event.sensor.getType()) {
		case TYPE_ACCELEROMETER:
			for (int i = 0; i < values.length; i++) {
				accelXYZ[i] = values[i];
			}
			break;
		}
	}

	/** onDestroy Called when Service is stopped */
	@Override
	public void onDestroy() {
		Log.d(USER_TAG, "onDestroy - AccelService");
		accelUnregister();
		closeAccelFile();
		super.onDestroy();
	}
}
