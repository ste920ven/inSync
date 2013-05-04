package com.example.android.inSync;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import com.example.android.inSync.R;

public class AboutScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_screen);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
