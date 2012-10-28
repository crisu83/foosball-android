package org.cniska.foosball.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This activity handles match logging.
 */
public class PlayMatchActivity extends BaseActivity {

	private static final String TAG = "PlayMatchActivity";

	// Static variables
	// ----------------------------------------

	public static final String STATE_MATCH = "org.cniska.foosball.android.STATE_MATCH";

	// Member variables
	// ----------------------------------------

	PowerManager.WakeLock mWakeLock;

    private RawMatch mMatch;
    private TextView mHomeTeamScore;
    private TextView mAwayTeamScore;
    private boolean mMatchSaved = false;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getClass().getName());

        Intent intent = getIntent();
		mMatch = intent.getParcelableExtra(NewMatchActivity.EXTRA_MATCH);

        // Restore member variables if state has changed.
        if (savedInstanceState != null) {
            mMatch = savedInstanceState.getParcelable(STATE_MATCH);
        }

		setActionBarTitle(R.string.title_match);

		setContentView(R.layout.play_match);

		mHomeTeamScore = (TextView) findViewById(R.id.text_home_team_score);
		mAwayTeamScore = (TextView) findViewById(R.id.text_away_team_score);

		mHomeTeamScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addHomeTeamGoal(v);
			}
		});
		mAwayTeamScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addAwayTeamGoal(v);
			}
		});

		renderPlayerNames();
		renderMatchScore();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.play_match, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_undo:
				undoAction();
				return true;

			case R.id.menu_cancel:
				exitConfirmation();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MATCH, mMatch);
	}

	@Override
	protected void onResume() {
		mWakeLock.acquire(); // We do not want the screen to become locked while a match is on-going.
		super.onResume();
	}

	@Override
	protected void onPause() {
		mWakeLock.release(); // Allow screen locking when this activity is paused.
		super.onPause();
	}

	/**
	 * Adds a goal for the home team.
	 * @param view
	 */
	public void addHomeTeamGoal(View view) {
		if (mMatch.addHomeTeamGoal()) {
			updateMatchScore();
		}
	}

	/**
	 * Adds a goal the away team.
	 * @param view
	 */
	public void addAwayTeamGoal(View view) {
		if (mMatch.addAwayTeamGoal()) {
			updateMatchScore();
		}
	}

	/**
	 * Takes back the most recent action preformed.
	 * Called when the undo menu item is pressed.
	 */
	private void undoAction() {
		if (mMatch.undoAction()) {
        	updateMatchScore();
		}
	}

	/**
	 * Displays a confirm dialog to exit the match.
	 */
	public void exitConfirmation() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.dialog_message_exit)
				.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startMainActivity();
					}
				})
				.setNegativeButton(R.string.dialog_button_no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.create()
				.show();
	}

	/**
	 * Renders the player names below the team name.
	 */
	private void renderPlayerNames() {
		RawPlayer[] players = loadPlayers();
		String homeTeamNames, awayTeamNames, name;

		homeTeamNames = players[0].getName();
		name = players[2] != null ? players[2].getName() : null;
		if (!TextUtils.isEmpty(name)) {
			homeTeamNames += " / " + name;
		}

		TextView homeTeamView = (TextView) findViewById(R.id.text_home_team_player_names);
		if (homeTeamView != null) {
			homeTeamView.setText(homeTeamNames);
		}

		awayTeamNames = players[1].getName();
		name = players[3] != null ? players[3].getName() : null;
		if (!TextUtils.isEmpty(name)) {
			awayTeamNames += " / " + name;
		}

		TextView awayTeamView = (TextView) findViewById(R.id.text_away_team_player_names);
		if (awayTeamView != null) {
			awayTeamView.setText(awayTeamNames);
		}
	}

	/**
	 * Updates the match score and checks if either team has won the match.
	 */
	private void updateMatchScore() {
		renderMatchScore();

		// Check if either team has reached the number of goals to win.
		if (mMatch.hasEnded()) {
			end();
		}
	}

	/**
	 * Renders the match score.
	 */
	private void renderMatchScore() {
		mHomeTeamScore.setText(String.valueOf(mMatch.getNumHomeTeamGoals()));
		mAwayTeamScore.setText(String.valueOf(mMatch.getNumAwayTeamGoals()));
	}

	/**
	 * Loads the players in the current match.
	 * @return The players.
	 */
	private RawPlayer[] loadPlayers() {
		RawPlayer[] players = new RawPlayer[RawMatch.NUM_SUPPORTED_PLAYERS];

		if (mMatch != null) {
			int i;
			long[] playerIds = mMatch.getPlayerIds();
			ArrayList<String> placeholders = new ArrayList<String>();
			ArrayList<String> args = new ArrayList<String>();

			// Loop through the player ids and collect both placeholders and selection arguments.
			for (i = 0; i < playerIds.length; i++) {
				if (playerIds[i] > 0) {
					placeholders.add("?");
					args.add(String.valueOf(playerIds[i]));
				}
			}

			// Combine the arrays to build the "IN" clause for the query.
			String selection = "_id IN (" + TextUtils.join(",", placeholders) + ")";

			// Convert the args to a string array in order to use it for the query.
			int numArgs = args.size();
			String[] selectionArgs = new String[numArgs];
			for (i = 0; i < numArgs; i++) {
				selectionArgs[i] = args.get(i);
			}

			// Execute the query to get the player records.
			Cursor cursor = getContentResolver().query(
					DataContract.Players.CONTENT_URI,
					new String[] { DataContract.Players._ID, DataContract.Players.NAME },
					selection, selectionArgs, ""); // we need to remove the default ordering

			// Iterate through the cursor and append each player to the result.
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					RawPlayer player = new RawPlayer(cursor);
					long playerId = player.getId();
					for (i = 0; i < playerIds.length; i++) {
						if (playerIds[i] == playerId) {
							players[i] = player;
						}
					}
					cursor.moveToNext();
				}
				cursor.close();
			}
		}

		return players;
	}

	/**
	 * Saves player data and starts the match over activity.
	 * Called when the match is over.
	 */
	private void end() {
        Logger.info(TAG, "Sending intent to start MatchSummaryActivity.");
        Intent intent = new Intent(this, MatchSummaryActivity.class);
        intent.putExtra(NewMatchActivity.EXTRA_MATCH, mMatch);
        startActivity(intent);
	}
}