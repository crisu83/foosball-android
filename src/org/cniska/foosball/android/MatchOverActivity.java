package org.cniska.foosball.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This activity is displayed when a match ends.
 */
public class MatchOverActivity extends BaseActivity {

	// Static variables
	// ----------------------------------------

	private static final String TAG = "MatchOverActivity";

	// Member variables
	// ----------------------------------------

	private int mNumGoalsToWin = 10;
	private int mWinningTeam = 0;
	private ArrayList<String> mPlayerNames = new ArrayList<String>();

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent != null) {
			mNumGoalsToWin = intent.getIntExtra(NewMatchActivity.EXTRA_NUM_GOALS_TO_WIN, 10);
			mWinningTeam = intent.getIntExtra(PlayMatchActivity.EXTRA_WINNING_TEAM, 0);
			mPlayerNames = intent.getStringArrayListExtra(NewMatchActivity.EXTRA_PLAYER_NAMES);
		}

		getActionBar().setDisplayShowTitleEnabled(false);
		setHomeButtonEnabled(true);

		setContentView(R.layout.match_over);

		renderWinningTeam(getResources().getString(
				mWinningTeam == PlayMatchActivity.TEAM_HOME
				? R.string.text_home_team
				: R.string.text_away_team));

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				startMainActivity();
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
	 * @param view
	 */
	public void exit(View view) {
		startMainActivity();
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