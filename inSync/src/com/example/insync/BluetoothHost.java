package com.example.insync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.example.insync.ConnectedThread;

public class BluetoothHost extends Activity {
	private File fp;
	private BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
	private Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
	// private byte[] buf;
	// private BufferedInputStream input = new BufferedInputStream(new
	// ByteArrayInputStream(buf));
	// private BufferedOutputStream output = new BufferedOutputStream(new
	// ByteArrayOutputStream());
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ImageButton pause;
	private SeekBar seekbar;
	private Button sendbutton;
	private TextView uriTV;
	private TextView connectedTV;
	private MediaPlayer mediaPlayer = new MediaPlayer();

	
    // Debugging
    private static final String TAG = "BluetoothChat";
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

	
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_host);

		Bundle extras = getIntent().getExtras();
		fp = new File(extras.getString("filepath"));

		uriTV = (TextView) findViewById(R.id.uriDisplayTV);
		uriTV.setText("Your selected song: " + fp);

		sendbutton = (Button) findViewById(R.id.sendfilebutton);
		sendbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendFile();
			}
		});

		listConnectedDevices();

		connectedTV = (TextView) findViewById(R.id.connectedBTdevTV);

		seekbar = (SeekBar) findViewById(R.id.mediaprogress);
		seekbar.setVisibility(View.INVISIBLE);
		pause = (ImageButton) findViewById(R.id.pause);
		pause.setVisibility(View.INVISIBLE);
		pause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pauseMedia();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
		return true;
	}

	public void listConnectedDevices() {
		List<String> s = new ArrayList<String>();
		for (BluetoothDevice bt : pairedDevices) {
			s.add(bt.getAddress());
		}

		final TextView btDevAddTV = (TextView) findViewById(R.id.connectedBTdevTV);
		btDevAddTV.append("\n" + s.toString());
	}

	public void sendFile() {
		if (bA == null) {
			// Device does not support Bluetooth
			return;
		}

		// Bring up Android's Bluetooth Device chooser
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fp));
		// ...

		// list of apps that can handle our intent
		PackageManager pm = getPackageManager();
		List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

		if (appsList.size() > 0) {
			// proceed
			// select bluetooth
			String packageName = null;
			String className = null;

			for (ResolveInfo info : appsList) {
				packageName = info.activityInfo.packageName;
				if (packageName.equals("com.android.bluetooth")) {
					className = info.activityInfo.name;
					break;// found
				}
			}

			// set our intent to launch Bluetooth
			intent.setClassName(packageName, className);
			startActivity(intent);

		}
	}


	// NOT TESTED---------
	public void setupMedia() throws IllegalStateException, IOException {
		connectedTV.setVisibility(View.INVISIBLE);
		sendbutton.setVisibility(View.INVISIBLE);
		uriTV.setVisibility(View.INVISIBLE);
		seekbar.setVisibility(View.VISIBLE);
		pause.setVisibility(View.VISIBLE);
		Uri myUri = Uri.fromFile(fp); // initialize Uri here

		mediaPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setDataSource(getApplicationContext(), myUri);
		mediaPlayer.prepare();

	}

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
	// -------
}
