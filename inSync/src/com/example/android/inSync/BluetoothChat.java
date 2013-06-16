/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.inSync;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import brian.lam.insync.R;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	String globalPath = "";
	int globalSeek = 0;

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

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
	private ListView mConversationView;
	private SeekBar mSeekBar;
	private ImageButton playButton;
	private ImageButton pauseButton;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private MediaMetadataRetriever MDR = new MediaMetadataRetriever();
	private String maxTime;
	private TextView time;
	private TextView currTime;
	private TextView songTitle;
	private ImageView cover;
	private TextView connection;
	private Button help;
	private ImageView overlay;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	// Adapter for media file

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.main);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		;

		time = (TextView) findViewById(R.id.time);
		// --songTitle = (TextView) findViewById(R.id.songTitle);
		connection = (TextView) findViewById(R.id.connection);
		connection.setTextColor(Color.RED);
		cover = (ImageView) findViewById(R.id.coverArt);
		cover.setVisibility(View.INVISIBLE);
		currTime = (TextView) findViewById(R.id.currTime);
		help = (Button) findViewById(R.id.button1);
		overlay = (ImageView) findViewById(R.id.overlay);
		overlay.setVisibility(View.INVISIBLE);
		help.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (overlay.getVisibility() == View.INVISIBLE) {
					overlay.setVisibility(View.VISIBLE);
				} else
					overlay.setVisibility(View.INVISIBLE);
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		// mConversationView = (ListView) findViewById(R.id.in);
		// mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the Seekbar with a listener
		mSeekBar = (SeekBar) findViewById(R.id.mediaprogress);
		mSeekBar.setOnSeekBarChangeListener(new yourListener());

		// Initialize the play button with a listener that for click events
		playButton = (ImageButton) findViewById(R.id.play);
		playButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mediaPlayer != null)
					pauseMedia();
			}
		});

		// Initialize the pause button with a listener that for click events
		pauseButton = (ImageButton) findViewById(R.id.pause);
		pauseButton.setVisibility(View.INVISIBLE);
		pauseButton.setClickable(false);
		pauseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mediaPlayer != null)
					pauseMedia();
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
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

	public void getMP3File() {
		try {
			/*
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("audio/*");
			final int PICKFILE_RESULT_CODE = 1251;
			startActivityForResult(intent, PICKFILE_RESULT_CODE);
			*/
			
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("audio/*");
			final int PICKFILE_RESULT_CODE = 1251;
			startActivityForResult(intent, PICKFILE_RESULT_CODE);
			
		}

		// Activity Not Found Crash fix
		// Updates TextView with message to install a file browser
		catch (Exception e) {
			// fnTV.setText("Error: No File Browser found! Please install a file browser (Such as ASTRO File Manager) to browse for an MP3 file.");
			Toast.makeText(
					this,
					"Error: No File Browser found! Please install a file browser (Such as ASTRO File Manager) to browse for an MP3 file.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			// mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	private final Runnable timeUpdate = new Runnable() {
		@Override
		public void run() {

			while (mediaPlayer != null && 0 < mediaPlayer.getDuration()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				} catch (Exception e) {
					return;
				}
				mSeekBar.post(new Runnable() {
					public void run() {
						int currentPosition = mediaPlayer.getCurrentPosition();
						mSeekBar.setProgress(currentPosition);
					}
				});
				mSeekBar.post(new Runnable() {
					public void run() {
						int currentPosition = mediaPlayer.getCurrentPosition();
						String cur = convertTime(currentPosition);
						currTime.setText(cur);
					}
				});

			}
		}
	};
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					connection.setTextColor(Color.GREEN);
					connection.setText("Connected!");
					break;
				case BluetoothChatService.STATE_CONNECTING:
					connection.setTextColor(Color.YELLOW);
					connection.setText("Connecting...");
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					connection.setTextColor(Color.RED);
					connection.setText("Not connected to another device");
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				/*
				 * SOME PRETTY IMPORTANT STUFF HERE! This is the code for read
				 */
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
						+ readMessage);
				if (readMessage.equals("pause")) {
					actuallyPause();
				}
				if (readMessage.startsWith("play:")) {
					String position = readMessage.replaceAll("play:", "");
					int intposition = Integer.parseInt(position);
					mediaPlayer.seekTo(intposition);
					actuallyResume();
				}
				if (readMessage.startsWith("seek:")) {
					String position = readMessage.replaceAll("seek:", "");
					int intposition = Integer.parseInt(position);
					mediaPlayer.seekTo(intposition);
				}
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
	
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		final int PICKFILE_RESULT_CODE = 1251;
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case PICKFILE_RESULT_CODE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				
				Log.e("Brian Lam","Activity result Debug 1");
				// Retrieve URI and display it in the TextView
				String FilePath = data.getData().getPath();
				
				Log.e("Brian Lam","Activity result Debug 2");
				
				String contentPath = data.getData().toString();
				if (contentPath.contains("content:")){
					Log.e("Brian Lam","Resolving");
					FilePath = getRealPathFromURI(Uri.parse(contentPath));
				}
				
				Log.e("Brian Lam","Activity result Debug 3");
				
				setFilePath(FilePath);
				sendMessage(FilePath);
				// Concat File Path
				// String s=FilePath.substring(FilePath.lastIndexOf("/"));
				
				Log.e("Brian Lam","Activity result Debug 4");

				Uri myUri = Uri.parse(FilePath);
				
				Log.e("Brian Lam","Activity result Debug 4.5");
				
				MDR.setDataSource(FilePath);
				
				Log.e("Brian Lam","Activity result Debug 5");

				// convert time to min:sec
				
				Log.e("Brian Lam","Activity result Debug 6");
				int max = Integer
						.parseInt(MDR
								.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
				mSeekBar.setMax(max);
				maxTime = convertTime(max);
				time.setText(maxTime);
				
				Log.e("Brian Lam","Activity result Debug 7");

				
				cover.setVisibility(View.VISIBLE);
				if (MDR.getEmbeddedPicture() == null)
					cover.setImageResource(R.drawable.coverart);
				else {
					byte[] img = MDR.getEmbeddedPicture();
					ByteArrayInputStream is = new ByteArrayInputStream(img);
					Drawable drw = Drawable.createFromStream(is, "coverart1");
					cover.setImageDrawable(drw);
				}
				Log.e("Brian Lam","Activity result Debug 8");

				// close object
				//MDR.release();

				sendMessage(Integer.toString(max));
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
				new Thread(timeUpdate).start();
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
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private String convertTime(int i) {
		String seconds = String.valueOf((i % 60000) / 1000);

		String minutes = String.valueOf(i / 60000);
		if (seconds.length() == 1)
			seconds = "0" + seconds;
		String res = minutes + ":" + seconds;
		return res;
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	public void sendFile() {
		// Bring up Android's Bluetooth Device chooser
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		File filetosend = new File(globalPath);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(filetosend));
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Start Activity for an MP3 File
			getMP3File();
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.sendfilethroughbluetooth:
			if (!existFilePath())
				Toast.makeText(this, "Choose a Music File first!",
						Toast.LENGTH_SHORT).show();
			else {
				sendMessage("I would like to send you" + globalPath);
				sendFile();
			}
			return true;
		case R.id.about:
			// Show about info
			aboutScreen(findViewById(android.R.id.content));
			return true;
		}
		return false;
	}

	public void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			sendMessage("pause");
			actuallyPause();
		} else {
			sendMessage("play:"
					+ String.valueOf(mediaPlayer.getCurrentPosition()));
			actuallyResume();
		}
	}

	public void actuallyPause() {
		mediaPlayer.pause();
		playButton.setVisibility(View.VISIBLE);
		playButton.setClickable(true);
		pauseButton.setVisibility(View.INVISIBLE);
		pauseButton.setClickable(false);
	}

	public void actuallyResume() {
		mediaPlayer.start();
		playButton.setVisibility(View.INVISIBLE);
		playButton.setClickable(false);
		pauseButton.setVisibility(View.VISIBLE);
		pauseButton.setClickable(true);
	}

	public void aboutScreen(View view) {
		Intent intent = new Intent(this, AboutScreen.class);
		startActivity(intent);
	}

	private class yourListener implements SeekBar.OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			int CurrentLevel = seekBar.getProgress();
			seekBar.setProgress(CurrentLevel);
			sendMessage("seek:" + Integer.toString(CurrentLevel));
		}

	}
}
