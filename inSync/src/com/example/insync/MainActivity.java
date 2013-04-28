package com.example.insync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	MediaPlayer buttonClick = null;
	// --------------
	private BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
	private Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();

	// ----------------------
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ArrayList<BluetoothSocket> Sockets = new ArrayList<BluetoothSocket>();
	private ArrayList<OutputStream> Output = new ArrayList<OutputStream>();
	private ArrayList<InputStream> Input = new ArrayList<InputStream>();
	private ArrayList<ConnectedThread> ctArray;
	public static final int MESSAGE_WRITE = 1;
	public static final int MESSAGE_READ = 2;

	// ---------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buttonClick = MediaPlayer.create(this, R.raw.buttonclick);

		final Button testStreamButton = (Button) findViewById(R.id.testButton);
		testStreamButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				}
			}
		);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Will be called when the Create Session button is clicked
	public void createSession(View view) {
		buttonClick.start();
		Intent intent = new Intent(this, CreateSession.class);
		startActivity(intent);
	}

	// Will be called when the Help! :( button is clicked
	public void helpScreen(View view) {
		buttonClick.start();
		Intent intent = new Intent(this, HelpScreen.class);
		startActivity(intent);
	}

}
