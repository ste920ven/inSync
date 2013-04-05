package com.example.insync;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CreateSession extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_session);
		enableBluetooth();
		
		final Button fCButton = (Button) findViewById(R.id.chooseFileButton);
		
		fCButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	             intent.setType("file/*");
	             final int PICKFILE_RESULT_CODE = 1;
	             startActivityForResult(intent,PICKFILE_RESULT_CODE);
			}
		});
	}
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  // TODO Auto-generated method stub
		 final int PICKFILE_RESULT_CODE = 1;
	  switch(requestCode){
	  case PICKFILE_RESULT_CODE:
	   if(resultCode==RESULT_OK){
	    String FilePath = data.getData().getPath();
	    final TextView textFile = (TextView) findViewById(R.id.fileNameTextView); 
	    textFile.setText("MP3 File Selected: " + FilePath);
	   }
	   break;
	   
	  }
	 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_session, menu);
		return true;
	}

	/*
	//Choose an mp3 file - Code starts here
	private String[] mFileList;
	private File mPath = new File(Environment.getExternalStorageDirectory() + "//music//");
	private String mChosenFile;
	private static final String FTYPE = ".mp3";    
	private static final int DIALOG_LOAD_FILE = 1000;

	private void loadFileList(View v) {
		try {
			mPath.mkdirs();
		}
		catch(SecurityException e) {
			//Log.e(TAG, "unable to write on the sd card " + e.toString());
		}
		if(mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return filename.contains(FTYPE) || sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		}
		else {
			mFileList= new String[0];
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

		switch(id) {
		case DIALOG_LOAD_FILE:
			builder.setTitle("Choose your file");
			if(mFileList == null) {
				//Log.e(TAG, "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mChosenFile = mFileList[which];
					//you can do stuff with the file here too
				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
	}
	*/

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
