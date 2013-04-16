package com.example.insync;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class BluetoothHost extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_host);
		
		Bundle extras = getIntent().getExtras();
		String fp = extras.getString("filepath");
		
		final TextView uriTV = (TextView) findViewById(R.id.uriDisplayTV);
		uriTV.setText(fp);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
		return true;
	}

}
