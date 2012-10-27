package org.cniska.foosball.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * This activity is displayed when a match ends.
 */
public class MatchSummaryActivity extends BaseActivity {

	private static final String TAG = "MatchSummaryActivity";

	// Static variables
	// ----------------------------------------

	private static final String STATE_OLD_PLAYER_RATINGS = "org.cniska.foosball.android.STATE_OLD_PLAYER_RATINGS";
	private static final String STATE_NEW_PLAYER_RATINGS = "org.cniska.foosball.android.STATE_NEW_PLAYER_RATINGS";
	private static final String STATE_DATA_SAVED = "org.cniska.foosball.android.STATE_DATA_SAVED";

	// Member variables
	// ----------------------------------------

    private RawMatch mMatch;
	private int[] mOldPlayerRatings;
	private int[] mNewPlayerRatings;
	private boolean mDataSaved = false;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mMatch = intent.getParcelableExtra(NewMatchActivity.EXTRA_MATCH);

		// Restore member variables if state has changed.
		if (savedInstanceState != null) {
			mMatch = savedInstanceState.getParcelable(PlayMatchActivity.STATE_MATCH);
			mOldPlayerRatings = savedInstanceState.getIntArray(STATE_OLD_PLAYER_RATINGS);
			mNewPlayerRatings = savedInstanceState.getIntArray(STATE_NEW_PLAYER_RATINGS);
			mDataSaved = savedInstanceState.getBoolean(STATE_DATA_SAVED);
		}

		setTitle(getString(R.string.text_team_won, mMatch.getWinningTeam() == RawMatch.TEAM_HOME
				? getString(R.string.text_home_team)
				: getString(R.string.text_away_team)));

		// Start listening for touches outside of this activity in order to prevent the modal from closing
		// when the user touches the modal backdrop.
		Window window = getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		saveData();

		setContentView(R.layout.match_summary);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(PlayMatchActivity.STATE_MATCH, mMatch);
		outState.putIntArray(STATE_OLD_PLAYER_RATINGS, mOldPlayerRatings);
		outState.putIntArray(STATE_NEW_PLAYER_RATINGS, mNewPlayerRatings);
		outState.putBoolean(STATE_DATA_SAVED, mDataSaved);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			// Prevent the activity from being closed by clicking the backdrop.
			case MotionEvent.ACTION_OUTSIDE:
				return true;

			default:
				return super.onTouchEvent(event);
		}
	}

	@Override
	public void onBackPressed() {
		// Do nothing, we do not want to allow the user to return the previous activity.
	}

	/**
	 * Saves all the match related data.
	 */
	private void saveData() {
		// Make sure that we don't save the data twice.
		if (!mDataSaved) {
			ContentResolver contentResolver = getContentResolver();
			Cursor cursor;
			int i;

			// Insert a new row in the match table.
			ContentValues matchValues = new ContentValues();
			matchValues.put(DataContract.Matches.DURATION, mMatch.getDuration());
			Uri uri = contentResolver.insert(DataContract.Matches.CONTENT_URI, matchValues);
			cursor = contentResolver.query(uri, new String[] { DataContract.Matches._ID }, null, null, null);

			if (cursor.moveToFirst()) {
				mMatch.setId(cursor.getLong(cursor.getColumnIndex(DataContract.Matches._ID)));
				cursor.close();
			}

			// Fetch match data.
			int numHomeTeamGoals = mMatch.getNumHomeTeamGoals();
			int numAwayTeamGoals = mMatch.getNumAwayTeamGoals();
			int winningTeam = mMatch.getWinningTeam();
			long[] playerIds = mMatch.getPlayerIds();

			mOldPlayerRatings = new int[playerIds.length];
			mNewPlayerRatings = new int[playerIds.length];

			// Loop through the players and fetch their current ratings.
			for (i = 0; i < playerIds.length; i++) {
				cursor = contentResolver.query(
						Uri.withAppendedPath(DataContract.Players.CONTENT_URI, playerIds[i] + "/rating"),
						new String[] { DataContract.Ratings.RATING }, null, null, "created DESC");

				if (cursor.moveToFirst()) {
					mOldPlayerRatings[i] = cursor.getInt(cursor.getColumnIndex(DataContract.Ratings.RATING));
					cursor.close();
				}
			}

			// Calculate the home- and away team rating, if there are two opposing players
			// the team rating is the average rating of both players.

			int homeTeamRating = mOldPlayerRatings[2] > 0
					? (mOldPlayerRatings[0] + mOldPlayerRatings[2]) / 2
					: mOldPlayerRatings[0];

			int awayTeamRating = mOldPlayerRatings[3] > 0
					? (mOldPlayerRatings[1] + mOldPlayerRatings[3]) / 2
					: mOldPlayerRatings[1];

			// Loop through the players and save their stats and update their rating.
			for (i = 0; i < playerIds.length; i++) {
				// Make sure that we only process players that are actually playing.
				// If the player id is zero it means that position is empty.
				if (playerIds[i] > 0) {
					boolean homeTeamPlayer = (i % 2) == 0;
					int oldRating = mOldPlayerRatings[i];
					int opponentRating = homeTeamPlayer ? awayTeamRating : homeTeamRating;
					boolean won = (homeTeamPlayer && winningTeam == RawMatch.TEAM_HOME)
							|| (!homeTeamPlayer && winningTeam == RawMatch.TEAM_AWAY);
					double score = won ? EloRatingSystem.SCORE_WIN : EloRatingSystem.SCORE_LOSS;

					ContentValues statsValues = new ContentValues();
					statsValues.put(DataContract.Stats.PLAYER_ID, playerIds[i]);
					statsValues.put(DataContract.Stats.MATCH_ID, mMatch.getId());
					statsValues.put(DataContract.Stats.GOALS_FOR, homeTeamPlayer ? numHomeTeamGoals : numAwayTeamGoals);
					statsValues.put(DataContract.Stats.GOALS_AGAINST, homeTeamPlayer ? numAwayTeamGoals : numHomeTeamGoals);
					statsValues.put(DataContract.Stats.SCORE, score);
					statsValues.put(DataContract.Stats.TEAM, homeTeamPlayer ? RawMatch.TEAM_HOME : RawMatch.TEAM_AWAY);
					contentResolver.insert(DataContract.Stats.CONTENT_URI, statsValues);

					if (mMatch.isRanked()) {
						mNewPlayerRatings[i] = EloRatingSystem.newRating(oldRating, opponentRating, score);
						ContentValues ratingValues = new ContentValues();
						ratingValues.put(DataContract.Ratings.PLAYER_ID, playerIds[i]);
						ratingValues.put(DataContract.Ratings.RATING, mNewPlayerRatings[i]);
						contentResolver.insert(DataContract.Ratings.CONTENT_URI, ratingValues);
					} else {
						mNewPlayerRatings = mOldPlayerRatings;
					}
				}
			}

			mDataSaved = true;
		}
	}

	/**
	 * Starts a rematch.
	 * @param view
	 */
	public void rematch(View view) {
		long[] playerIds, swappedIds;
		playerIds = mMatch.getPlayerIds();
		swappedIds = new long[playerIds.length];

		// Switch sides for the rematch.
		swappedIds[0] = playerIds[1];
		swappedIds[1] = playerIds[0];
		swappedIds[2] = playerIds[3];
		swappedIds[3] = playerIds[2];

		// Create a new match with swapped player ids.
		RawMatch match = new RawMatch();
		match.setNumGoalsToWin(mMatch.getNumGoalsToWin());
		match.setRanked(mMatch.isRanked());
		match.setPlayerIds(swappedIds);

		Logger.info(TAG, "Sending intent to start PlayMatchActivity.");
		Intent intent = new Intent(this, PlayMatchActivity.class);
		intent.putExtra(NewMatchActivity.EXTRA_MATCH, match);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * Exits the activity.
	 * @param view
	 */
	public void exit(View view) {
		startMainActivity();
	}
}