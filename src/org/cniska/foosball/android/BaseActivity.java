package org.cniska.foosball.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

/**
 * This activity provides some common functionality.
 * All activities should inherit this class if possible.
 */
public class BaseActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.info(getClass().getSimpleName(), "Creating activity.");
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		Logger.info(getClass().getSimpleName(), "Starting activity.");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Logger.info(getClass().getSimpleName(), "Resuming activity.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Logger.info(getClass().getSimpleName(), "Pausing activity.");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Logger.info(getClass().getSimpleName(), "Stopping activity.");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Logger.info(getClass().getSimpleName(), "Destroying activity.");
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Logger.info(getClass().getSimpleName(), "Saving activity state.");
		super.onSaveInstanceState(outState);
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

	/**
	 * Enables the home button if applicable.
	 * @param enabled
	 */
	protected void setHomeButtonEnabled(boolean enabled) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setDisplayHomeAsUpEnabled(enabled);
		}
	}

	/**
	 * Sets the action bar title with fallback to activity title on older devices..
	 * @param title Text.
	 */
	public void setActionBarTitle(CharSequence title) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setTitle(title);
		} else {
			super.setTitle(title);
		}
	}

	/**
	 * Sets the action bar title with fallback to activity title on older devices..
	 * @param id Resource id.
	 */
	public void setActionBarTitle(int id) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setTitle(id);
		} else {
			super.setTitle(id);
		}
	}

	/**
	 * Starts the application's main activity.
	 */
	protected void startMainActivity() {
		Logger.info(getClass().getSimpleName(), "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}