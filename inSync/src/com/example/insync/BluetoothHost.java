package com.example.insync;

import java.io.File;
import java.io.IOException;
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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class BluetoothHost extends Activity {
	// Filepath to music file
	private File fp;
	// Local BluetoothAdapter
	private BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
	private Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ImageButton pause;
	private SeekBar seekbar;
	private Button sendbutton;
	private TextView uriTV;
	private TextView connectedTV;
	private MediaPlayer mediaPlayer = new MediaPlayer();

	// Debugging
	private static final String TAG = "BluetoothHost";
	private static final boolean D = true;


	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

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
	// Member object for the chat services
	private BluetoothService mService = null;

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
				//sendFile();
				setupMedia();
				connectDevice(false);
			}
		});

		listConnectedDevices();

		connectedTV = (TextView) findViewById(R.id.connectedBTdevTV);

		seekbar = (SeekBar) findViewById(R.id.mediaprogress);
		seekbar.setVisibility(View.INVISIBLE);

		// Initialize the BluetoothChatService to perform bluetooth connections
		mService = new BluetoothService(this, mHandler);

		Button findDevice = (Button) findViewById(R.id.connecttoadevice);
		findDevice.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				findDevice();
			}
		});

		pause = (ImageButton) findViewById(R.id.pause);
		pause.setVisibility(View.INVISIBLE);
		pause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message = "p";
				byte[] send = message.getBytes();
				mService.write(send);
				pauseMedia();

			}
		});
	}

	public void findDevice(){
		Intent serverIntent = null;
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);       		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
		return true;
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

	private void connectDevice(boolean secure) {
		// List all paired Bluetooth Devices
		bA.getBondedDevices();
		List<String> s = new ArrayList<String>();
		for (BluetoothDevice bt : pairedDevices) {
			s.add(bt.getAddress());
		}

		// Get the device MAC address
		String address = s.get(0);
		// Get the BluetoothDevice object
		BluetoothDevice device = bA.getRemoteDevice(address);
		// Attempt to connect to the device
		mService.connect(device, secure);
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

	// NOT TESTED---------
	public void setupMedia() {
		connectedTV.setVisibility(View.INVISIBLE);
		sendbutton.setVisibility(View.INVISIBLE);
		uriTV.setVisibility(View.INVISIBLE);
		seekbar.setVisibility(View.VISIBLE);
		pause.setVisibility(View.VISIBLE);
		Uri myUri = Uri.fromFile(fp); // initialize Uri here

		mediaPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Initialize the BluetoothChatService to perform bluetooth connections
		//mService = new BluetoothService(this, mHandler);
		// ** Code commented out, I initialized mService up there to be sure that it's initialized. 
		// ** - Brian Lam

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
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                //setupChat();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                //Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = bA.getRemoteDevice(address);
        // Attempt to connect to the device
        mService.connect(device, secure);
    }
}
