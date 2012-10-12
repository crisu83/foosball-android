package org.cniska.foosball.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity handles match logging.
 */
public class PlayMatchActivity extends Activity {

	// Static variables
	// ----------------------------------------

	private static final String TAG =  PlayMatchActivity.class.getName();

	private static final String STATE_NUM_HOME_TEAM_GOALS = "org.cniska.foosball.android.STATE_NUM_HOME_TEAM_GOALS";
	private static final String STATE_NUM_AWAY_TEAM_GOALS = "org.cniska.foosball.android.STATE_NUM_AWAY_TEAM_GOALS";
	private static final String STATE_NUM_GOALS_TO_WIN = "org.cniska.foosball.android.STATE_NuM_GOALS_TO_WIN";

	private static final int TEAM_NONE = 0;
	private static final int TEAM_HOME = 1;
	private static final int TEAM_AWAY = 2;

	private static String[] PROJECTION = {
		Player._ID,
		Player.NAME,
		Player.GOALS_FOR,
		Player.GOALS_AGAINST,
		Player.WINS,
		Player.LOSSES,
		Player.RATING
	};

	// Member variables
	// ----------------------------------------

	PowerManager.WakeLock mWakeLock;

	private int mNumGoalsToWin = 10;
	private int mNumHomeTeamGoals = 0;
	private int mNumAwayTeamGoals = 0;
	private int mWinningTeam = TEAM_NONE;
	private ArrayList<String> mPlayerNames;
	private List<Integer> mHistory = new ArrayList<Integer>();
	private boolean mEnded = false;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getClass().getName());

		// Restore member variables if state has changed.
		if (savedInstanceState != null) {
			mNumGoalsToWin = savedInstanceState.getInt(STATE_NUM_GOALS_TO_WIN);
			mNumHomeTeamGoals = savedInstanceState.getInt(STATE_NUM_HOME_TEAM_GOALS);
			mNumAwayTeamGoals = savedInstanceState.getInt(STATE_NUM_AWAY_TEAM_GOALS);
		}

		// Process intent data if necessary.
		Intent intent = getIntent();
		if (intent != null) {
			mPlayerNames = intent.getStringArrayListExtra(NewMatchActivity.EXTRA_PLAYER_NAMES);
			mNumGoalsToWin = Integer.parseInt(intent.getStringExtra(NewMatchActivity.EXTRA_SCORES_TO_WIN));
		}

		setContentView(R.layout.play_match);

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

			case R.id.menu_exit:
				quitWithConfirmation();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putInt(STATE_NUM_GOALS_TO_WIN, mNumGoalsToWin);
		state.putInt(STATE_NUM_HOME_TEAM_GOALS, mNumHomeTeamGoals);
		state.putInt(STATE_NUM_AWAY_TEAM_GOALS, mNumAwayTeamGoals);
		Logger.info(TAG, "Activity state saved.");
	}

	@Override
	protected void onResume() {
		mWakeLock.acquire(); // We do not want the screen to become locked while a match is on-going.
		Logger.info(TAG, "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		mWakeLock.release(); // The screen maybe locked when this activity is paused.
		Logger.info(TAG, "Activity paused.");
		super.onPause();
	}

	/**
	 * Adds a goal for the home team.
	 * @param view
	 */
	public void addHomeTeamGoal(View view) {
		if (!mEnded) {
			mNumHomeTeamGoals++;
			mHistory.add(TEAM_HOME);
			Logger.info(TAG, "Goal logged for the home team.");
			updateMatchScore();
		} else {
			Logger.error(TAG, "Failed to log goal for the home team (match has ended).");
		}
	}

	/**
	 * Adds a goal the away team.
	 * @param view
	 */
	public void addAwayTeamGoal(View view) {
		if (!mEnded) {
			mNumAwayTeamGoals++;
			mHistory.add(TEAM_AWAY);
			Logger.info(TAG, "Goal logged for the away team.");
			updateMatchScore();
		} else {
			Logger.error(TAG, "Failed to log goal for the away team (match has ended).");
		}
	}

	/**
	 * Takes back the most recent action preformed.
	 * Called when the undo menu item is pressed.
	 */
	private void undoAction() {
		int historyLength = mHistory.size();

		if (historyLength > 0) {
			int historyItem = mHistory.remove(historyLength - 1);

			switch (historyItem) {
				case TEAM_HOME:
					mNumHomeTeamGoals--;
					Logger.info(TAG, "Score removed from the home team.");
					updateMatchScore();
					break;

				case TEAM_AWAY:
					mNumAwayTeamGoals--;
					Logger.info(TAG, "Score removed from the away team.");
					updateMatchScore();
					break;

				default:
					Logger.error(this.TAG, "Failed to undo action (type unknown).");
			}
		}
	}

	/**
	 * Updates the match score and checks if either team has won the match.
	 */
	private void updateMatchScore() {
		renderMatchScore();

		// Check if either team has reached the number of goals to win.
		if (mNumHomeTeamGoals >= mNumGoalsToWin) {
			mWinningTeam = TEAM_HOME;
			end();
		} else if (mNumAwayTeamGoals >= mNumGoalsToWin) {
			mWinningTeam = TEAM_AWAY;
			end();
		}
	}

	/**
	 * Renders the match score.
	 */
	private void renderMatchScore() {
		TextView scoreView = (TextView) findViewById(R.id.text_score);
		String score = getResources().getString(R.string.text_score);
		scoreView.setText(String.format(score, mNumHomeTeamGoals, mNumAwayTeamGoals));
	}

	/**
	 * Displays a confirm dialog to quit the match.
	 */
	public void quitWithConfirmation() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.dialog_message_quit)
				.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						quit();
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
	 * Ends the match and saves the statistics.
	 */
	private void end() {
		if (!mEnded) {
			mEnded = true;

			String namePlayer1, namePlayer2, namePlayer3, namePlayer4;
			Player player1, player2, player3, player4;

			namePlayer1 = mPlayerNames.get(0);
			player1 = findPlayerByName(namePlayer1);

			if (player1 == null) {
				player1 = createPlayer(namePlayer1);
			}

			namePlayer2 = mPlayerNames.get(1);
			player2 = findPlayerByName(namePlayer2);

			if (player2 == null) {
				player2 = createPlayer(namePlayer2);
			}

			namePlayer3 = mPlayerNames.get(2);
			player3 = findPlayerByName(namePlayer3);

			if (player3 == null) {
				player3 = createPlayer(namePlayer3);
			}

			namePlayer4 = mPlayerNames.get(3);
			player4 = findPlayerByName(namePlayer4);

			if (player4 == null) {
				player4 = createPlayer(namePlayer4);
			}

			if (player1 != null && player2 != null) {
				// Calculate the team ratings.
				int homeTeamRating = player3 != null ? (player1.getRating() + player3.getRating()) / 2 : player1.getRating();
				int awayTeamRating = player4 != null ? (player2.getRating() + player4.getRating()) / 2 : player2.getRating();

				updatePlayer(player1, mNumHomeTeamGoals, mNumAwayTeamGoals, awayTeamRating, mWinningTeam == TEAM_HOME);
				updatePlayer(player2, mNumAwayTeamGoals, mNumHomeTeamGoals, homeTeamRating, mWinningTeam == TEAM_AWAY);

				if (player3 != null) {
					updatePlayer(player3, mNumHomeTeamGoals, mNumAwayTeamGoals, awayTeamRating, mWinningTeam == TEAM_HOME);
				}

				if (player4 != null) {
					updatePlayer(player4, mNumAwayTeamGoals, mNumHomeTeamGoals, homeTeamRating, mWinningTeam == TEAM_AWAY);
				}
			}

			quit();
		}
	}

	/**
	 * Finds a player by its name.
	 * @param name Player name.
	 * @return The player.
	 */
	private Player findPlayerByName(String name) {
		Cursor cursor = getContentResolver().query(Player.CONTENT_URI, PROJECTION, Player.NAME + "=?",
				new String[] { name }, null);

		Player player = null;
		if (cursor.moveToFirst()) {
			player = cursorToPlayer(cursor);
		}
		return player;
	}

	/**
	 * Quits the match without saving.
	 */
	private void quit() {
		Logger.info(TAG, "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	/**
	 * Creates a new player record.
	 * @param name Player name.
	 * @return The player.
	 */
	private Player createPlayer(String name) {
		ContentValues values = new ContentValues();
		values.put(Player.NAME, name);
		Uri uri = getContentResolver().insert(Player.CONTENT_URI, values);
		Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null, null);

		Player player = null;
		if (cursor.moveToFirst()) {
			player = cursorToPlayer(cursor);
		}

		return player;
	}

	/**
	 * Updates the given player by adding goals and wins or losses.
	 * @param player Player to update.
	 * @param goalsFor Amount of goals for the player's team.
	 * @param goalsAgainst Amount of goals against the player's.
	 * @param opponentRating Rating of the opposing player(s).
	 * @param won Whether the player's team won the match.
	 * @return The updated player.
	 */
	private Player updatePlayer(Player player, int goalsFor, int goalsAgainst, int opponentRating, boolean won) {
		if (goalsFor > 0) {
			player.addGoalsFor(goalsFor);
		}

		if (goalsAgainst > 0) {
			player.addGoalsAgainst(goalsAgainst);
		}

		// Adjust the player's rating.
		int newRating = EloRatingSystem.newRating(player.getRating(), opponentRating,
				won ? EloRatingSystem.SCORE_WIN : EloRatingSystem.SCORE_LOSS);
		player.setRating(newRating);

		if (won) {
			player.addWin();
		} else {
			player.addLoss();
		}

		ContentValues values = new ContentValues();
		values.put(Player.NAME, player.getName());
		values.put(Player.GOALS_FOR, player.getGoalsFor());
		values.put(Player.GOALS_AGAINST, player.getGoalsAgainst());
		values.put(Player.WINS, player.getWins());
		values.put(Player.LOSSES, player.getLosses());
		values.put(Player.RATING, player.getRating());
		int numAffectedRows = getContentResolver().update(
				Uri.withAppendedPath(Player.CONTENT_URI, String.valueOf(player.getId())), values, null, null);

		if (numAffectedRows == 0) {
			Logger.error(TAG, "Failed to update player #" + player.getId() + " (player not found).");
		}

		return player;
	}

	/**
	 * Bind the cursor data to a player record.
	 * @param cursor The cursor.
	 * @return The record.
	 */
	private Player cursorToPlayer(Cursor cursor) {
		Player player = new Player();
		player.setId(cursor.getLong(0));
		player.setName(cursor.getString(1));
		player.setGoalsFor(cursor.getInt(2));
		player.setGoalsAgainst(cursor.getInt(3));
		player.setWins(cursor.getInt(4));
		player.setLosses(cursor.getInt(5));
		player.setRating(cursor.getInt(6));
		return player;
	}
}