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
	String fp = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_host);

		Bundle extras = getIntent().getExtras();
		fp = extras.getString("filepath");

		final TextView uriTV = (TextView) findViewById(R.id.uriDisplayTV);
		uriTV.setText("Your selected song: " + fp);

		final Button sendbutton = (Button) findViewById(R.id.sendfilebutton);
		sendbutton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
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
		BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
		List<String> s = new ArrayList<String>();
		for(BluetoothDevice bt : pairedDevices){
			s.add(bt.getAddress());
		}
		
		final TextView btDevAddTV = (TextView) findViewById(R.id.connectedBTdevTV);
		btDevAddTV.append("\n"+s.toString());
	}

	public void sendFile(){
		ContentValues values = new ContentValues();
		values.put(BluetoothShare.URI, "content://" + fp);
		
		//Hard coded device address
		values.put(BluetoothShare.DESTINATION, "E0:B9:A5:6C:F8:9C");
		values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
		Long ts = System.currentTimeMillis();
		values.put(BluetoothShare.TIMESTAMP, ts);

	}

}
