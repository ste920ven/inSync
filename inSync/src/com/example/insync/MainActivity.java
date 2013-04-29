package com.example.insync;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	MediaPlayer buttonClick = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buttonClick = MediaPlayer.create(this, R.raw.buttonclick);

		final Button testStreamButton = (Button) findViewById(R.id.testButton);
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

	public void test(View view) {
		buttonClick.start();
		Intent intent = new Intent(this, BluetoothGuest.class);
		startActivity(intent);
	}
}
