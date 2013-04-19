package com.example.insync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.net.Uri;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class BluetoothHost extends Activity {
	private File fp;

	//Global Bluetooth Adapter
	private BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
	private Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();

	MediaPlayer buttonClick = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_host);

		//Mediaplayer for sound that is played when button is clicked
		buttonClick = MediaPlayer.create(this, R.raw.buttonclick);

		Bundle extras = getIntent().getExtras();
		fp = new File(extras.getString("filepath"));

		final TextView uriTV = (TextView) findViewById(R.id.uriDisplayTV);
		uriTV.setText("Your selected song: " + fp);

		final Button sendbutton = (Button) findViewById(R.id.sendfilebutton);
		sendbutton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				buttonClick.start();
				sendFile();
			}
		});

		listConnectedDevices();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
		return true;
	}

	public void listConnectedDevices(){
		List<String> s = new ArrayList<String>();
		for(BluetoothDevice bt : pairedDevices){
			s.add(bt.getAddress());
		}

		final TextView btDevAddTV = (TextView) findViewById(R.id.connectedBTdevTV);
		btDevAddTV.append("\n"+s.toString());
	}

	public void sendFile(){
		if (bA == null) {
			// Device does not support Bluetooth
			return;
		}

		// Bring up Android's Bluetooth Device chooser
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fp) );
		//...	

		//list of apps that can handle our intent
		PackageManager pm = getPackageManager();
		List<ResolveInfo> appsList = pm.queryIntentActivities( intent, 0);

		if(appsList.size() > 0) {
			// proceed
			//select bluetooth
			String packageName = null;
			String className = null;

			for(ResolveInfo info: appsList){
				packageName = info.activityInfo.packageName;
				if( packageName.equals("com.android.bluetooth")){
					className = info.activityInfo.name;
					break;// found
				}
			}

			//set our intent to launch Bluetooth
			intent.setClassName(packageName, className);
			startActivity(intent);
		}

	}

}
