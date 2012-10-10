package org.cniska.foosball.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import org.cniska.foosball.R;

/**
 * This class is the application's main activity.
 */
public class MainActivity extends Activity {

	public static final String TAG = MainActivity.class.getName();
	
	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove the title bar and set app to full-screen mode.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		Logger.info(TAG, "Activity created.");
	}

	/**
	 * Starts a new match.
	 * @param view
	 */
	public void startNewMatch(View view) {
		Logger.info(TAG, "Sending intent to start NewMatchActivity.");
		Intent intent = new Intent(this, NewMatchActivity.class);
		startActivity(intent);
	}

	/**
	 * Opens the playing statistics.
	 * @param view
	 */
	public void openStatistics(View view) {
		Logger.info(TAG, "Sending intent to start StatisticsActivity.");
		Intent intent = new Intent(this, StatisticsActivity.class);
		startActivity(intent);
	}

	/**
	 * Tests the REST service implementation.
	 * @param view
	 */
	public void testRest(View view) {
		Intent intent = new Intent(this, RESTServiceActivity.class);
		startActivity(intent);
	}
}
