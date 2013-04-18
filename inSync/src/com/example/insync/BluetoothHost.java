package com.example.insync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothHost extends Activity {
	
	//Global String variable for filepath
	String fp = "";
	
	//Global bluetooth adapter so that you don't have to keep calling a new Bluetooth adapter
	BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
	Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_host);

		//Get the filepath for the selected music file
		Bundle extras = getIntent().getExtras();
		fp = extras.getString("filepath");

		//Display the filepath on the Textview
		final TextView uriTV = (TextView) findViewById(R.id.uriDisplayTV);
		uriTV.setText("Your selected song: " + fp);

 		//This is for the "Send mp3 file" button
		final Button sendbutton = (Button) findViewById(R.id.sendfilebutton);
		sendbutton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				//Call the sendFile() function which will use Bluetooth
				sendFile();
			}
		});

		//List the addresses for the connected Bluetooth devices
		listConnectedDevices();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
		return true;
	}

	public void listConnectedDevices(){
		//Get a list of the addresses of connected Bluetooth devices
		List<String> s = new ArrayList<String>();
		for(BluetoothDevice bt : pairedDevices){
			
			s.add(bt.getAddress());
		}

		//Get the TextView
		final TextView btDevAddTV = (TextView) findViewById(R.id.connectedBTdevTV);
		//Append the addresses for the Bluetooth devices onto the TextView
		btDevAddTV.append("\n"+s.toString());
	}

	public void sendFile(){

		//For loop:
		//For each bluetooth device that is connected, send the music file
		for(BluetoothDevice bt : pairedDevices){
			ContentValues values = new ContentValues();
			values.put(BluetoothShare.URI, "content://" + fp);

			//Send Bluetooth stuff for each Address
			values.put(BluetoothShare.DESTINATION, bt.getAddress());
			values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
			Long ts = System.currentTimeMillis();
			values.put(BluetoothShare.TIMESTAMP, ts);
		}


	}

}
