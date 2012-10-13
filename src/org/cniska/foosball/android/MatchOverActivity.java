package org.cniska.foosball.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This activity is displayed when a match ends.
 */
public class MatchOverActivity extends Activity {

	// Static variables
	// ----------------------------------------

	private static final String TAG = MatchOverActivity.class.getName();

	// Member variables
	// ----------------------------------------

	private int mNumGoalsToWin = 10;
	private int mWinningTeam = 0;
	private ArrayList<String> mPlayerNames;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPlayerNames = new ArrayList<String>();

		Intent intent = getIntent();
		if (intent != null) {
			mNumGoalsToWin = intent.getIntExtra(NewMatchActivity.EXTRA_NUM_GOALS_TO_WIN, 10);
			mWinningTeam = intent.getIntExtra(PlayMatchActivity.EXTRA_WINNING_TEAM, 0);
			mPlayerNames = intent.getStringArrayListExtra(NewMatchActivity.EXTRA_PLAYER_NAMES);
		}

		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.match_over);

		if (mWinningTeam == PlayMatchActivity.TEAM_HOME) {
			renderWinningTeam(getResources().getString(R.string.text_home_team));
		} else {
			renderWinningTeam(getResources().getString(R.string.text_away_team));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Starts a rematch.
	 * @param view
	 */
	public void rematch(View view) {
		Logger.info(TAG, "Sending intent to start PlayMatchActivity.");
		Intent intent = new Intent(this, PlayMatchActivity.class);
		intent.putStringArrayListExtra(NewMatchActivity.EXTRA_PLAYER_NAMES, mPlayerNames);
		intent.putExtra(NewMatchActivity.EXTRA_NUM_GOALS_TO_WIN, mNumGoalsToWin);
		startActivity(intent);
	}

	/**
	 * Exits the activity.
	 */
	public void exit(View view) {
		Logger.info(TAG, "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	/**
	 * Renders the winning team.
	 * @param teamName Team name.
	 */
	private void renderWinningTeam(String teamName) {
		String teamWonText = getResources().getString(R.string.text_team_won);
		TextView winningTeamView = (TextView) findViewById(R.id.text_winning_team);
		winningTeamView.setText(String.format(teamWonText, teamName));
	}
}