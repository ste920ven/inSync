package com.example.insync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.bluetooth.*;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class CreateSession extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_session);
		enableBluetooth();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_session, menu);
		return true;
	}
	
	public void enableBluetooth(){
		BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
		
		//Enable Bluetooth
		if(!bA.isEnabled()){
			bA.enable();
			final TextView btCheck = (TextView) findViewById(R.id.bluetoothCheck);
			btCheck.setText("Turning Bluetooth on to detect other Android devices");		
		}
		
		//List all paired Bluetooth Devices
		Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
		List<String> s = new ArrayList<String>();
		for(BluetoothDevice bt : pairedDevices){
			s.add(bt.getName());
		}
		
		//Update TextView with list of Bluetooth Devices
		final TextView btDevTV = (TextView) findViewById(R.id.bluetoothTV);
		btDevTV.append("\n"+s.toString());
		
	}

}
