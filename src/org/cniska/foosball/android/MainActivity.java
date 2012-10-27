package org.cniska.foosball.android;

import android.accounts.OnAccountsUpdateListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * This class is the application's main activity.
 */
public class MainActivity extends BaseActivity {

	public static final String TAG = "MainActivity";

	public static final String VERSION = "0.9.0";

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove the title bar and set app to full-screen mode.
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getActionBar().setTitle(null);

		setContentView(R.layout.main);

		// Set the app version number.
		TextView version = (TextView) findViewById(R.id.text_version);
		version.setText(String.format("v %s", VERSION));

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_manage_players:
				managePlayers();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
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
	 * Starts the statistics activity.
	 * @param view
	 */
	public void openStatistics(View view) {
		Logger.info(TAG, "Sending intent to start StatisticsActivity.");
		Intent intent = new Intent(this, StatisticsActivity.class);
		startActivity(intent);
	}

	/**
	 * Starts the manage players activity.
	 */
	public void managePlayers() {
		Logger.info(TAG, "Sending intent to start ManagePlayersActivity.");
		Intent intent = new Intent(this, ManagePlayersActivity.class);
		startActivity(intent);
	}
}
