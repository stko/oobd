package org.oobd.ui.android;

//openXC imports

import android.content.Context;
import android.util.Log;

//import com.google.common.base.Objects;

//import com.google.protobuf.GeneratedMessage.*;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.ContextualVehicleDataSource;
import com.openxc.sources.SourceCallback;

/**
 * A openXC vehicle data source that pushes OOBD data to openXC.
 * 
 * data transfer will not begin until a callback is set, either via a
 * constructor or the
 * {@link com.openxc.sources.BaseVehicleDataSource#setCallback(SourceCallback)}
 * function.
 */
class OOBDVehicleDataSource extends ContextualVehicleDataSource implements
		Runnable {
	private static final String TAG = "OOBDVehicleDataSource";

	private boolean mTraceValid = false;
	private long mFirstTimestamp = 0;
	private boolean mRunning = true;
	private String line = "";
	final Object synchLock = new Object();

	/**
	 * Construct a oobd data source with the given context and callback
	 * 
	 * If the callback is not null, transfer will begin immediately.
	 * 
	 * @param callback
	 *            An object implementing the SourceCallback interface that
	 *            should receive data as it is received and parsed.
	 */
	public OOBDVehicleDataSource(SourceCallback callback, Context context) {
		super(callback, context);
		Log.d(TAG, "Starting new OOBD openXC data source");
		new Thread(this).start();
	}

	/**
	 * Consider the trace source "connected" if it's running and at least 1
	 * measurement was parsed successfully from the file.
	 * 
	 * This will catch errors e.g. if the trace is totally corrupted, but it
	 * won't give you any indication if it is partially corrupted.
	 */
	@Override
	public boolean isConnected() {
		return mRunning && mTraceValid;
	}

	/**
	 * Stop trace file playback and the playback thread.
	 */
	public void stop() {
		super.stop();
		Log.d(TAG, "Stopping trace playback");
		mRunning = false;
		synchronized (synchLock) {
			line = "";
			synchLock.notify();
		}
	}

	/**
	 * transfer line.
	 */
	public void sendJsonString2openXC(String json) {
		if (json != "") {
			RawMeasurement measurement;
			try {
				measurement = new RawMeasurement(json);
				if (measurement != null && !measurement.isTimestamped()) {
					Log.w(TAG, "A trace line was missing a timestamp: " + json);
				} else {

					measurement.untimestamp();
					if (!mTraceValid) {
						connected();
						mTraceValid = true;
					}

					handleMessage(measurement);
				}
			} catch (UnrecognizedMeasurementTypeException e) {
				Log.w(TAG, "A trace line was not in the expected " + "format: "
						+ json);
			}

		}
	}

	/**
	 * While running, continuously read from the trace file and send messages to
	 * the callback.
	 * 
	 * If the callback is not set, this function will exit immediately and the
	 * thread will die a quick death.
	 */
	public void run() {
		Log.d(TAG, "Starting oobd -> openXC transfer");
		while (mRunning) {
			try {
				synchronized (synchLock) {
					synchLock.wait();
				}
			} catch (InterruptedException ex) {

			}

			if (line != "") {
				RawMeasurement measurement;
				try {
					measurement = new RawMeasurement(line);
				} catch (UnrecognizedMeasurementTypeException e) {
					Log.w(TAG, "A trace line was not in the expected "
							+ "format: " + line);
					continue;
				}

				if (measurement != null && !measurement.isTimestamped()) {
					Log.w(TAG, "A trace line was missing a timestamp: " + line);
					continue;
				}

				measurement.untimestamp();
				if (!mTraceValid) {
					connected();
					mTraceValid = true;
				}

				handleMessage(measurement);
			}

			disconnected();
			Log.d(TAG, "Restarting OOBD openXC transfer");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		disconnected();
		mRunning = false;
		Log.d(TAG, "OOBD -> openXC transfer is finished");
	}

	public void sendJSONString(String jsonString) {
		sendJsonString2openXC(jsonString);
		/*
		try {
			synchronized (synchLock) {

				line = jsonString;
				synchLock.notify();
			}
		} catch (Exception ex) {

		}
		*/
	}

	protected String getTag() {
		return TAG;
	}
}