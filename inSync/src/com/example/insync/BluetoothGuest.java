package com.example.insync;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class BluetoothGuest extends Activity {
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private File fp;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Debugging
	private static final String TAG = "BluetoothGuest";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Member object for the chat services
	private BluetoothService mService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_guest);
		/*
		 * Bundle extras = getIntent().getExtras(); fp = new
		 * File(extras.getString("filepath"));
		 * mediaPlayer.setWakeMode(getApplicationContext(),
		 * PowerManager.PARTIAL_WAKE_LOCK);
		 * mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); try {
		 * mediaPlayer.setDataSource(getApplicationContext(), myUri); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } try { mediaPlayer.prepare(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_guest, menu);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mService = new BluetoothService(this, mHandler);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mService != null)
			mService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				break;
			case MESSAGE_WRITE:
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// DEBUGG
				Context context = getApplicationContext();
				CharSequence text = "Hello toast!";
				int duration = Toast.LENGTH_SHORT;
				Toast.makeText(context, text, duration).show();
				// ---- controller(readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		} else {
			mediaPlayer.start();
		}
	}

	public void seekMedia(int loc) {
		mediaPlayer.seekTo(loc);
	}

	public void controller(String s) {
		if (s == "p")
			pauseMedia();
		if (s == "s")
			seekMedia(1);
	}
}
