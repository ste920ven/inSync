package com.example.insync;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothGuest extends Activity {
	String globalPath = "";
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();

	private File fp;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final UUID MY_UUID_SECURE =
			UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final UUID MY_UUID_INSECURE =
			UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

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

	// Variable: Number of seconds during which the device will be Bluetooth
	// discoverable
	private static final int DISCOVER_DURATION = 100;
	// our request code (must be greater than zero)
	private static final int REQUEST_BLU = 1;

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Member object for the chat services
	private BluetoothService mService = null;

	MediaPlayer buttonClick = null;
	TextView debugTextView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_guest);

		buttonClick = MediaPlayer.create(this, R.raw.buttonclick);

		enableBluetooth();

		final Button fCButton = (Button) findViewById(R.id.chooseFileButton);
		fCButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					buttonClick.start();
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("file/*");
					final int PICKFILE_RESULT_CODE = 1;
					startActivityForResult(intent, PICKFILE_RESULT_CODE);
				}

				// Activity Not Found Crash fix
				// Updates TextView with message to install a file browser
				catch (Exception e) {
					buttonClick.start();
					final TextView fnTV = (TextView) findViewById(R.id.fileNameTextView);
					fnTV.setText("Error: No File Browser found! Please install a file browser (Such as ASTRO File Manager) to browse for an MP3 file.");
					fnTV.setTextColor(Color.RED);
				}
			}
		});

		debugTextView = (TextView) findViewById(R.id.debugText);


		mService = new BluetoothService(this, mHandler);
		mService.start();

		Thread checkInput = new Thread(checkInputStream);
		checkInput.start();
		
		Button findDevice = (Button) findViewById(R.id.connecttoadevice2);
		findDevice.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				findDevice();
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
		getMenuInflater().inflate(R.menu.bluetooth_guest, menu);
		return true;
	}

	public void updateDebugText(int i){
		String iToString = Integer.toString(i);
		debugTextView.append(iToString);
	}

	public void updateDebugText(String s){
		debugTextView.append(s);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Initialize the BluetoothChatService to perform bluetooth connections

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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Called after File Browser Activity returns file
		final int PICKFILE_RESULT_CODE = 1;
		switch (requestCode) {
		case PICKFILE_RESULT_CODE:
			if (resultCode == RESULT_OK) {
				// Retrieve URI and display it in the TextView
				String FilePath = data.getData().getPath();
				final TextView textFile = (TextView) findViewById(R.id.fileNameTextView);

				//Concat File Path
				String s=FilePath.substring(FilePath.lastIndexOf("/"));

				textFile.setText("MP3 File Selected: " + s);
				setFilePath(FilePath);
			}
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
			break;

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
    
	public String setFilePath(String path) {
		globalPath = path;
		return globalPath;
	}

	public String getFilePath() {
		return globalPath;
	}

	public boolean existFilePath() {
		if (globalPath.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	public void enableBluetooth() {
		BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();

		// Enable Bluetooth
		if (!bA.isEnabled()) {
			bA.enable();
			final TextView btCheck = (TextView) findViewById(R.id.bluetoothCheck);
			btCheck.setText("Turning Bluetooth on to detect other Android devices");
		}

		// Enable device discovery
		enableBlu();


	}

	public void enableBlu() {
		// enable device discovery - this will automatically enable Bluetooth
		Intent discoveryIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

		discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
				DISCOVER_DURATION);

		startActivityForResult(discoveryIntent, REQUEST_BLU);
	}

	Runnable checkInputStream = new Runnable(){
		public void run(){
			//Get all devices, and for each device, try to get the input Stream for both secure AND insecure UUID

			BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();

			BluetoothSocket tmp = null;
			while(true){
				for (BluetoothDevice device : pairedDevices) {
					try{
						//First Attempt to Read
						tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
						InputStream input = tmp.getInputStream();
						updateDebugText(input.read());

						//Second Attempt to Read
						tmp = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
						InputStream input2 = tmp.getInputStream();
						updateDebugText(input2.read());

						//Third Attempt to Read
						BluetoothSocket btSocket = InsecureBluetooth.createRfcommSocketToServiceRecord(
								device, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"), false);
						btSocket.connect();
						InputStream input3 = btSocket.getInputStream();
						DataInputStream dinput = new DataInputStream(input3);					
						byte[] byteArray = null;
						dinput.readFully(byteArray, 0, byteArray.length);
						updateDebugText(byteArray.toString());

						//Fourth attemp to Read
						tmp = InsecureBluetooth.createRfcommSocketToServiceRecord(
								device, MY_UUID_INSECURE, false);
						tmp.connect();
						InputStream input4 = tmp.getInputStream();
						DataInputStream dinput2 = new DataInputStream(input4);					
						byte[] byteArray2 = null;
						dinput.readFully(byteArray2, 0, byteArray2.length);
						updateDebugText(byteArray2.toString());
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	};
	
	

}
