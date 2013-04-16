package com.example.insync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.bluetooth.*;
import android.content.Intent;
import android.graphics.Color;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class CreateSession extends Activity {

	String globalPath = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_session);

		//Prevents on-screen keyboard from popping up when Activity is started
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		enableBluetooth();

		//Code for "Choose an mp3 file to live stream" button
		final Button fCButton = (Button) findViewById(R.id.chooseFileButton);
		fCButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				try{
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("file/*");
					final int PICKFILE_RESULT_CODE = 1;
					startActivityForResult(intent,PICKFILE_RESULT_CODE);
				}

				//Activity Not Found Crash fix
				//Updates TextView with message to install a file browser
				catch(Exception e){
					final TextView fnTV = (TextView) findViewById(R.id.fileNameTextView);
					fnTV.setText("Error: No File Browser found! Please install a file browser (Such as ASTRO File Manager) to browse for an MP3 file.");
					fnTV.setTextColor(Color.RED);
				}
			}
		});

		final Button confirmbutton = (Button) findViewById(R.id.button1);
		confirmbutton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				if (existFilePath()){
					hostBluetooth();
				}
			}
		});
	}

	public String setFilePath(String path){
		globalPath = path;
		return globalPath;
	}

	public boolean existFilePath(){
		if (globalPath.equals("")){
			return false;
		}
		else{
			return true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Called after File Browser Activity returns file
		final int PICKFILE_RESULT_CODE = 1;
		switch(requestCode){
		case PICKFILE_RESULT_CODE:
			if(resultCode==RESULT_OK){
				//Retrieve URI and display it in the TextView
				String FilePath = data.getData().getPath();
				final TextView textFile = (TextView) findViewById(R.id.fileNameTextView); 
				textFile.setText("MP3 File Selected: " + FilePath);
				setFilePath(FilePath);
			}
			break;

		}
	}


	public void hostBluetooth(){
		Intent intent = new Intent(this, BluetoothHost.class);
		startActivity(intent);
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

