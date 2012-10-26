package org.cniska.foosball.android;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * This activity handles match logging.
 */
public class PlayMatchActivity extends BaseActivity {

	private static final String TAG = "PlayMatchActivity";

	// Static variables
	// ----------------------------------------

	private static final String STATE_MATCH = "org.cniska.foosball.android.STATE_MATCH";

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

		getActionBar().setTitle(getString(R.string.title_match));

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

		Logger.info(TAG, "Activity created.");
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
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
        state.putParcelable(STATE_MATCH, mMatch);
		Logger.info(TAG, "Activity state saved.");
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
						exit();
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
	 * Displays a confirm dialog to end the match.
	 */
	public void endConfirmation() {
		new AlertDialog.Builder(this)
				.setMessage(endMessage())
				.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						end();
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

	private String endMessage() {
        int winningTeam = mMatch.getWinningTeam();
		return String.format(getString(R.string.dialog_message_match_ended), winningTeam == RawMatch.TEAM_HOME
				? getString(R.string.text_home_team)
				: getString(R.string.text_away_team));
	}

	/**
	 * Updates the match score and checks if either team has won the match.
	 */
	private void updateMatchScore() {
		renderMatchScore();

		// Check if either team has reached the number of goals to win.
		if (mMatch.hasEnded()) {
			endConfirmation();
		}
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

	private RawPlayer[] loadPlayers() {
		RawPlayer[] players = new RawPlayer[RawMatch.NUM_SUPPORTED_PLAYERS];

		if (mMatch != null) {
			long[] playerIds = mMatch.getPlayerIds();

			for (int i = 0; i < playerIds.length; i++) {
				if (playerIds[i] > 0) {
					Cursor cursor = getContentResolver().query(
							Uri.withAppendedPath(DataContract.Players.CONTENT_URI, String.valueOf(playerIds[i])),
							new String[] { DataContract.Players._ID, DataContract.Players.NAME }, null, null, null);

					if (cursor.moveToFirst()) {
						players[i] = new RawPlayer(cursor);
					}
				}
			}
		}

		return players;
	}

	/**
	 * Renders the match score.
	 */
	private void renderMatchScore() {
		mHomeTeamScore.setText(String.valueOf(mMatch.getNumHomeTeamGoals()));
		mAwayTeamScore.setText(String.valueOf(mMatch.getNumAwayTeamGoals()));
	}

	/**
	 * Saves player data and starts the match over activity.
	 * Called when the match is over.
	 */
	private void end() {
		saveMatch(); // Save match related data.

        Logger.info(TAG, "Sending intent to start MatchOverActivity.");
        Intent intent = new Intent(this, MatchOverActivity.class);
        intent.putExtra(NewMatchActivity.EXTRA_MATCH, mMatch);
        startActivity(intent);
	}

	/**
	 * Saves all the match related data.
	 */
	private void saveMatch() {
		if (!mMatchSaved) {
			ContentResolver contentResolver = getContentResolver();
			Cursor cursor;

			ContentValues matchValues = new ContentValues();
			matchValues.put(DataContract.Matches.DURATION, mMatch.getDuration());
			Uri uri = contentResolver.insert(DataContract.Matches.CONTENT_URI, matchValues);
			cursor = contentResolver.query(uri, new String[] { DataContract.Matches._ID }, null, null, null);

			if (cursor.moveToFirst()) {
				mMatch.setId(cursor.getLong(0));
				cursor.close();
			}

			int i;
			int numHomeTeamGoals = mMatch.getNumHomeTeamGoals();
			int numAwayTeamGoals = mMatch.getNumAwayTeamGoals();
			int winningTeam = mMatch.getWinningTeam();
			long[] playerIds = mMatch.getPlayerIds();

			int[] playerRatings = new int[RawMatch.NUM_SUPPORTED_PLAYERS];

			for (i = 0; i < playerIds.length; i++) {
				cursor = contentResolver.query(Uri.withAppendedPath(DataContract.Players.CONTENT_URI, playerIds[i] + "/rating"),
						new String[] { DataContract.Ratings.RATING }, null, null, null);

				if (cursor.moveToFirst()) {
					playerRatings[i] = cursor.getInt(0);
					cursor.close();
				}
			}

			int homeTeamRating = playerRatings[2] > 0 ? (playerRatings[0] + playerRatings[2]) / 2 : playerRatings[0];
			int awayTeamRating = playerRatings[3] > 0 ? (playerRatings[1] + playerRatings[3]) / 2 : playerRatings[1];

			for (i = 0; i < playerIds.length; i++) {
				long playerId = playerIds[i];
				if (playerId > 0) {
					boolean homeTeamPlayer = (i % 2) == 0;
					int oldRating = playerRatings[i];
					int opponentRating = homeTeamPlayer ? awayTeamRating : homeTeamRating;
					boolean won = (homeTeamPlayer && winningTeam == RawMatch.TEAM_HOME)
							|| (!homeTeamPlayer && winningTeam == RawMatch.TEAM_AWAY);
					double score = won ? EloRatingSystem.SCORE_WIN : EloRatingSystem.SCORE_LOSS;
					int rating = EloRatingSystem.newRating(oldRating, opponentRating, score);

					ContentValues ratingValues = new ContentValues();
					ratingValues.put(DataContract.Ratings.PLAYER_ID, playerId);
					ratingValues.put(DataContract.Ratings.RATING, rating);
					contentResolver.insert(DataContract.Ratings.CONTENT_URI, ratingValues);

					ContentValues statsValues = new ContentValues();
					statsValues.put(DataContract.Stats.PLAYER_ID, playerId);
					statsValues.put(DataContract.Stats.MATCH_ID, mMatch.getId());
					statsValues.put(DataContract.Stats.GOALS_FOR, homeTeamPlayer ? numHomeTeamGoals : numAwayTeamGoals);
					statsValues.put(DataContract.Stats.GOALS_AGAINST, homeTeamPlayer ? numAwayTeamGoals : numHomeTeamGoals);
					statsValues.put(DataContract.Stats.SCORE, won);
					statsValues.put(DataContract.Stats.TEAM, homeTeamPlayer ? RawMatch.TEAM_HOME : RawMatch.TEAM_AWAY);
					contentResolver.insert(DataContract.Stats.CONTENT_URI, statsValues);
				}
			}

			mMatchSaved = true;
		}
	}

	/**
	 * Exits the match without saving.
	 */
	private void exit() {
		Logger.info(TAG, "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}