package org.oobd.ui.android.bus;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.bus.*;
import org.oobd.base.support.Onion;
import org.oobd.ui.android.MainActivity;
import org.oobd.ui.android.application.OOBDApp;
import org.json.JSONException;

import android.bluetooth.BluetoothSocket;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.util.*;

public class BusCom extends OobdBus implements OOBDConstants {

	InputStream inputStream;
	Thread readThread;
	static String messageString = "Hello, world!";
	static OutputStream outputStream;
	static boolean outputBufferEmptyFlag = false;

	public BusCom() {
		super("Buscom");

	}

	@Override
	public void registerCore(Core thisCore) {
		super.registerCore(thisCore);
		Debug.msg("busecho", DEBUG_BORING, "Core registered...");
	}

	@Override
	public String getPluginName() {
		return "b:Com";
	}

	public void run() {
		Looper.prepare();
		AndroidBusReader reader = new AndroidBusReader();

		// TODO check whether these properties are needed
		Properties props = new Properties();
		try {
			// props.load(new FileInputStream("resources/buscom.props"));
			props.load(OOBDApp.getInstance().generateResourceStream(
					FT_PROPS,
					OOBDApp.getInstance().generateUIFilePath(FT_PROPS,
							"buscom.props")));
		} catch (IOException ignored) {
			System.out
					.println("buscom.props not found - Does not matter in Android Version");
		}

		Thread bluetoothThread = new Thread(new BluetoothInitWorker());
		bluetoothThread.start();
		BluetoothSocket socket = null;
		socket = BluetoothInitWorker.getMyInstance().initializeBluetoothSocket(props.getProperty("BTDevice", "00:12:6F:07:27:25"));
		if (socket != null) {
			try {

				socket.connect();
				reader.connect(socket);
				Log.d("OOBD:Bluetooth", "Bluetooth connected!");
			} catch (IOException ex) {
				Log.e(this.getClass().getSimpleName(),
						"Error: Could not connect socket.");
				Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}else{
			Log.e("OOBD:Bluetooth", "Bluetooth NOT connected!");
 /*           MainActivity.getMyMainActivity().runOnUiThread(new Runnable() {
                public void run() {
                	Toast.makeText(MainActivity.getMyMainActivity().getApplicationContext(), "No Bluetooth Connection", Toast.LENGTH_LONG).show();
                }
            });
*/
		}

		while (keepRunning == true) {
			//Looper.loop();
			System.out.println("--- naechste Runde ---");
			Debug.msg("buscom", DEBUG_BORING, "sleeping...");
			Message msg = getMsg(true);
			Onion on = msg.getContent();
			Debug.msg("buscom", DEBUG_BORING, "Msg received:"
					+ msg.getContent().toString());
			String command = on.getOnionString("command");
			if ("serWrite".equalsIgnoreCase(command)) {
				try {
					/*
					 * // Old Version: sun.misc.* reader.write(new String(new
					 * BASE64Decoder
					 * ().decodeBuffer(on.getOnionString("data"))));
					 * replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS +
					 * "'," + "'owner':" + "{'name':'" + getPluginName() + "'},"
					 * + "'result':'" + "" + "'}"));
					 */
					reader.write(new String(Base64Coder.decodeLines(on
							.getOnionString("data"))));
					replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS
							+ "'," + "'owner':" + "{'name':'" + getPluginName()
							+ "'}," + "'result':'" + "" + "'}"));
				} catch (JSONException ex) {
					Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
							null, ex);
				}

			} else if ("serFlush".equalsIgnoreCase(command)) {
				try {
					reader.flush();
					replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS
							+ "'," + "'owner':" + "{'name':'" + getPluginName()
							+ "'}," + "'result':'" + "" + "'}"));
				} catch (JSONException ex) {
					Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
							null, ex);
				}
			} else if ("serWait".equalsIgnoreCase(command)) {
				try {
					// Old Version: sun.misc.*
					// Integer result = reader.wait(new String(new
					// BASE64Decoder().decodeBuffer(on.getOnionString("data"))),
					// on.getInt("timeout"));
					Integer result = reader.wait(new String(Base64Coder
							.decodeLines(on.getOnionString("data"))), on
							.getInt("timeout"));
					System.out.println("busCom serWait: " + result);
					replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':" + result + ","
                            + "'replyID':" + on.getInt("msgID")
                            + "}"));
				} catch (JSONException ex) {
					Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
							null, ex);
				}

			} else if ("serReadLn".equalsIgnoreCase(command)) {
				try {
					String result = reader.readln(on.getInt("timeout"), on
							.optBoolean("ignore"));
					System.out.println("busCom readline: " + result);
					/*
					 * //Old Version: sun.misc.* replyMsg(msg, new Onion("" +
					 * "{'type':'" + CM_RES_BUS + "'," + "'owner':" +
					 * "{'name':'" + getPluginName() + "'}," +
					 * "'replyID':"+on.getInt("replyID")+ "," + "'result':'" +
					 * new BASE64Encoder().encode(result.getBytes()) + "'}"));
					 */
					replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'replyID':" + on.getInt("msgID") + ","
                             + "'result':'" + Base64Coder.encodeString(result) + "'}"));
				} catch (JSONException ex) {
					Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
							null, ex);
				}

			} else {
				try {

					replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS
							+ "'," + "'owner':" + "{'name':'" + getPluginName()
							+ "'}," + "'result':'" + "" + "'}"));
				} catch (JSONException ex) {
					Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
							null, ex);
				}
			}
			Debug.msg("buscom", DEBUG_BORING, "waked up after received msg...");

		}
		//Looper.quit();
		try {
			socket.close();
		} catch (IOException ex) {
			Log.e(this.getClass().getSimpleName(),
					"Error: Could not close socket.");
			Logger.getLogger(BusCom.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

}
