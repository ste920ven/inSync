package com.example.insync;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	MediaPlayer buttonClick = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buttonClick = MediaPlayer.create(this, R.raw.buttonclick);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Will be called when the Create Session button is clicked
	public void createSession(View view){
		buttonClick.start();
		Intent intent = new Intent(this, CreateSession.class);
		startActivity(intent);
	}
	
	//Will be called when the Help! :( button is clicked
	public void helpScreen(View view){
		buttonClick.start();
		Intent intent = new Intent(this, HelpScreen.class);
		startActivity(intent);
	}
<<<<<<< HEAD
=======
	
	//Will be called when the Info button is clicked
	public void getInfo(View view){	
		buttonClick.start();
		Intent intent = new Intent(this, AboutScreen.class);
		startActivity(intent);
	}
>>>>>>> 1d2e74568ee16905947432161b1d839a32891d85
}
