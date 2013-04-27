package com.example.insync;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;

public class BluetoothGuest extends Activity {
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private File fp;
	private Uri myUri = Uri.fromFile(fp);
	//CONSTANTS
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		fp = new File(extras.getString("filepath"));
		
		setContentView(R.layout.activity_bluetooth_guest);
		mediaPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mediaPlayer.prepare();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_guest, menu);
		return true;
	}
	
	public void controller(int code){
		/*
		 * if (code==CONSTANT)
		 * 		method()
		 */
	}
	
	public void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		} else {
			mediaPlayer.start();
		}
	}

	public void seekMedia(int loc) {
		mediaPlayer.seekTo(loc);
	}

}
