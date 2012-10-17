package org.cniska.foosball.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class BaseActivity extends FragmentActivity {

	private static final String TAG = "BaseActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		Logger.info(TAG, "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Logger.info(TAG, "Activity paused.");
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_cancel:
			case android.R.id.home:
				startMainActivity();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	protected void setHomeButtonEnabled(boolean enabled) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setHomeButtonEnabled(enabled);
		}
	}

	protected void startMainActivity() {
		Logger.info(TAG, "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}