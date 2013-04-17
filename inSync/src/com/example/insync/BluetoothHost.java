package com.example.insync;

import android.os.Bundle;
import android.app.Activity;
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
				//sendFile(fp);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
		return true;
	}

	public void sendFile(){
		}

}
