package org.cniska.foosball.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.cniska.foosball.R;

import java.util.ArrayList;
import java.util.List;

public class PlayMatchActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Enumerables
	// ----------------------------------------

	public enum TeamType { RED, BLUE };

	// Static variables
	// ----------------------------------------

	private static final String TAG =  PlayMatchActivity.class.getName();

	private static final int PLAYER_LOADER = 0x01;

	private static final String STATE_PLAYERS = "org.cniska.foosball.players";
	private static final String STATE_NUM_PLAYER_GOALS = "org.cniska.foosball.numPlayerGoals";
	private static final String STATE_NUM_GOALS_TO_WIN = "org.cniska.foosball.numGoalsToWin";

	private static final int PLAYER1 = 0;
	private static final int PLAYER2 = 1;
	private static final int PLAYER3 = 2;
	private static final int PLAYER4 = 3;

	// Member variables
	// ----------------------------------------

	PowerManager.WakeLock mWakeLock;

	private Player[] mPlayers;
	private String[] mPlayerNames;
	private int[] mPlayerGoals;

	private int mNumGoalsToWin;

	private EloRatingSystem mRatingSystem;
	private List<Integer> mHistory;

	private boolean mEnded = false;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPlayerNames = new String[4];
		mPlayerGoals = new int[4];
		mPlayers = new Player[4];
		mHistory = new ArrayList<Integer>();

		getLoaderManager().initLoader(PLAYER_LOADER, null, this);

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getClass().getName());

		mRatingSystem = new EloRatingSystem();

		// Restore member variables if state has changed.
		if (savedInstanceState != null) {
			mNumGoalsToWin = savedInstanceState.getInt(STATE_NUM_GOALS_TO_WIN);
			mPlayerGoals = savedInstanceState.getIntArray(STATE_NUM_PLAYER_GOALS);
		}

		// Process intent data.
		Intent intent = getIntent();
		mPlayerNames[PLAYER1] = intent.getStringExtra(NewMatchActivity.EXTRA_NAME_PLAYER1);
		mPlayerNames[PLAYER2] = intent.getStringExtra(NewMatchActivity.EXTRA_NAME_PLAYER2);
		mPlayerNames[PLAYER3] = intent.getStringExtra(NewMatchActivity.EXTRA_NAME_PLAYER3);
		mPlayerNames[PLAYER4] = intent.getStringExtra(NewMatchActivity.EXTRA_NAME_PLAYER4);
		mNumGoalsToWin = Integer.parseInt(intent.getStringExtra(NewMatchActivity.EXTRA_SCORES_TO_WIN));

		// Set the view.
		setContentView(R.layout.play_match);

		// Render the current score.
		renderRedScore();
		renderBlueScore();

		// Find the buttons.
		Button buttonPlayer1 = (Button) findViewById(R.id.button_add_goal_player1);
		Button buttonPlayer2 = (Button) findViewById(R.id.button_add_goal_player2);
		Button buttonPlayer3 = (Button) findViewById(R.id.button_add_goal_player3);
		Button buttonPlayer4 = (Button) findViewById(R.id.button_add_goal_player4);

		// Update button texts.
		buttonPlayer1.setText(mPlayerNames[PLAYER1]);
		buttonPlayer2.setText(mPlayerNames[PLAYER2]);

		// Check whether player 3 is playing.
		if (isPlayerPlaying(PLAYER3)) {
			buttonPlayer3.setText(mPlayerNames[PLAYER3]);
		} else {
			// Player 3 is not playing, remove the corresponding button.
			LinearLayout layoutTeamRed = (LinearLayout) findViewById(R.id.layout_team_red);
			layoutTeamRed.removeView(buttonPlayer3);
		}

		// Check whether player 4 is playing.
		if (isPlayerPlaying(PLAYER4)) {
			buttonPlayer4.setText(mPlayerNames[PLAYER4]);
		} else {
			// Player 4 is not playing, remove the corresponding button.
			LinearLayout layoutTeamBlue = (LinearLayout) findViewById(R.id.layout_team_blue);
			layoutTeamBlue.removeView(buttonPlayer4);
		}

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.play_match_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_undo:
				undoAction();
				return true;
			case R.id.menu_quit:
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
		state.putIntArray(STATE_NUM_PLAYER_GOALS, mPlayerGoals);
		Logger.info(TAG, "Activity state saved.");
	}

	@Override
	protected void onResume() {
		mWakeLock.acquire();
		Logger.info(TAG, "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		mWakeLock.release();
		Logger.info(TAG, "Activity paused.");
		super.onPause();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Player.CONTENT_URI, PlayerProvider.projectionArray, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Adds a goal for player 1 (red team).
	 * @param view
	 */
	public void addGoalPlayer1(View view) {
		if (!mEnded) {
			mPlayerGoals[PLAYER1]++;
			mHistory.add(PLAYER1);
			Logger.info(TAG, "Goal logged for player 1.");
			updateRedTeamScore();
		} else {
			Logger.error(TAG, "Failed to log goal for player 1 (match has ended).");
		}
	}

	/**
	 * Adds a goal for player 2 (blue team).
	 * @param view
	 */
	public void addGoalPlayer2(View view) {
		if (!mEnded) {
			mPlayerGoals[PLAYER2]++;
			mHistory.add(PLAYER2);
			Logger.info(TAG, "Goal logged for player 2.");
			updateBlueTeamScore();
		} else {
			Logger.error(TAG, "Failed to log goal for player 2 (match has ended).");
		}
	}

	/**
	 * Adds a goal for player 3 (red team).
	 * @param view
	 */
	public void addGoalPlayer3(View view) {
		if (!mEnded) {
			if (isPlayerPlaying(PLAYER3)) {
				mPlayerGoals[PLAYER3]++;
				mHistory.add(PLAYER3);
				Logger.info(TAG, "Goal logged for player 3.");
				updateRedTeamScore();
			} else {
				Logger.error(TAG, "Failed to log goal for player 3 (player not playing).");
			}
		} else {
			Logger.error(TAG, "Failed to log goal for player 3 (match has ended).");
		}
	}

	/**
	 * Adds a goal for player 4 (blue team).
	 * @param view
	 */
	public void addGoalPlayer4(View view) {
		if (!mEnded) {
			if (isPlayerPlaying(PLAYER4)) {
				mPlayerGoals[PLAYER4]++;
				mHistory.add(PLAYER4);
				Logger.info(TAG, "Goal logged for player 4.");
				updateBlueTeamScore();
			} else {
				Logger.error(TAG, "Failed to log goal for player 4 (player not playing).");
			}
		} else {
			Logger.error(TAG, "Failed to log goal for player 4 (match has ended).");
		}
	}

	/**
	 * Updates the red team score and checks if the red team has won.
	 */
	private void updateRedTeamScore() {
		renderRedScore();

		if (redTeamGoals() >= mNumGoalsToWin) {
			end(TeamType.RED);
		}
	}

	/**
	 * Updates the blue team score and checks if the red team has won.
	 */
	private void updateBlueTeamScore() {
		renderBlueScore();

		if (blueTeamGoals() >= mNumGoalsToWin) {
			end(TeamType.BLUE);
		}
	}

	/**
	 * Returns whether the player with the given index is playing.
	 * @param index Player index (0 = p1, 1 = p2, 2 = p3, 3 = p4).
	 * @return The result.
	 */
	private boolean isPlayerPlaying(int index) {
		return !TextUtils.isEmpty(mPlayerNames[index]);
	}

	/**
	 * Returns the number of goals the red team has.
	 * @return The amount.
	 */
	private int redTeamGoals() {
		return mPlayerGoals[PLAYER1] + mPlayerGoals[PLAYER3];
	}

	/**
	 * Returns the number of goals the blue team has.
	 * @return The amount.
	 */
	private int blueTeamGoals() {
		return mPlayerGoals[PLAYER2] + mPlayerGoals[PLAYER4];
	}

	/**
	 * Returns the red team rating.
	 * @return The rating.
	 */
	private int redTeamRating() {
		return mPlayers[PLAYER3] != null
				? (mPlayers[PLAYER1].getRating() + mPlayers[PLAYER3].getRating()) / 2
				: mPlayers[PLAYER1].getRating();
	}

	/**
	 * Returns the blue team rating.
	 * @return The rating.
	 */
	private int blueTeamRating() {
		return mPlayers[PLAYER4] != null
				? (mPlayers[PLAYER2].getRating() + mPlayers[PLAYER4].getRating()) / 2
				: mPlayers[PLAYER2].getRating();
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
				case PLAYER1:
					mPlayerGoals[PLAYER1]--;
					Logger.info(TAG, "Score removed from player 1.");
					updateRedTeamScore();
					break;
				case PLAYER2:
					mPlayerGoals[PLAYER2]--;
					Logger.info(TAG, "Score removed from player 2.");
					updateBlueTeamScore();
					break;
				case PLAYER3:
					mPlayerGoals[PLAYER3]--;
					Logger.info(TAG, "Score removed from player 3.");
					updateRedTeamScore();
					break;
				case PLAYER4:
					mPlayerGoals[PLAYER4]--;
					Logger.info(TAG, "Score removed from player 4.");
					updateBlueTeamScore();
					break;
				default:
					Logger.error(this.TAG, "Failed to undo action (type unknown).");
			}
		}
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
	 * Quits the match without saving.
	 */
	private void quit() {
		Logger.info(TAG, "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	/**
	 * Renders the red team score.
	 */
	private void renderRedScore() {
		TextView redTeamScore = (TextView) findViewById(R.id.team_red_score);
		redTeamScore.setText(String.valueOf(redTeamGoals()));
	}

	/**
	 * Renders the blue team score.
	 */
	private void renderBlueScore() {
		TextView blueTeamScore = (TextView) findViewById(R.id.team_blue_score);
		blueTeamScore.setText(String.valueOf(blueTeamGoals()));
	}

	/**
	 * Ends the match.
	 * @param teamType Winning team.
	 */
	private void end(TeamType teamType) {
		mEnded = true;

		loadPlayers();

		Logger.info(TAG, "Updating player records.");

		if (mPlayers[PLAYER1] != null) {
			updatePlayer(mPlayers[PLAYER1], mPlayerGoals[PLAYER1], mPlayers[PLAYER3] == null ? blueTeamGoals() : 0,
					blueTeamRating(), teamType == TeamType.RED);
		}

		if (mPlayers[PLAYER2] != null) {
			updatePlayer(mPlayers[PLAYER2], mPlayerGoals[PLAYER2], mPlayers[PLAYER4] == null ? redTeamGoals() : 0,
					redTeamRating(), teamType == TeamType.BLUE);
		}

		if (mPlayers[PLAYER3] != null) {
			updatePlayer(mPlayers[PLAYER3], mPlayerGoals[PLAYER3], blueTeamGoals(),
					blueTeamRating(), teamType == TeamType.RED);
		}

		if (mPlayers[PLAYER4] != null) {
			updatePlayer(mPlayers[PLAYER4], mPlayerGoals[PLAYER4], redTeamGoals(),
					redTeamRating(), teamType == TeamType.BLUE);
		}

		quit();
	}


	/**
	 * Loads the player records from the database.
	 */
	private void loadPlayers() {
		Logger.info(TAG, "Loading player records.");

		Cursor cursor = getContentResolver().query(Player.CONTENT_URI, PlayerProvider.projectionArray, null, null, null);

		if (cursor.moveToFirst()) {
			int i = 0;
			while (!cursor.isAfterLast()) {
				mPlayers[i] = cursorToPlayer(cursor);
				cursor.moveToNext();
				i++;
			}
		}

		for (int i = 0; i < 4; i++) {
			if (mPlayers[i] == null && !TextUtils.isEmpty(mPlayerNames[i])) {
				mPlayers[i] = createPlayer(mPlayerNames[i]);
			}
		}

		Logger.info(TAG, "Player records loaded.");
	}

	private Player cursorToPlayer(Cursor cursor) {
		Player player = new Player();
		player.setId(cursor.getLong(0));
		player.setName(cursor.getString(4));
		player.setGoals(cursor.getInt(5));
		player.setGoalsAgainst(cursor.getInt(6));
		player.setWins(cursor.getInt(7));
		player.setLosses(cursor.getInt(8));
		player.setRating(cursor.getInt(9));
		return player;
	}

	private Player createPlayer(String name) {
		ContentValues values = new ContentValues();
		values.put(Player.NAME, name);
		Uri uri = getContentResolver().insert(Player.CONTENT_URI, values);
		Cursor cursor = getContentResolver().query(uri, PlayerProvider.projectionArray, null, null, null);
		Player player = null;
		if (cursor.moveToFirst()) {
			player = cursorToPlayer(cursor);
		}
		return player;
	}

	/**
	 * Updates the given player by adding goals and wins or losses.
	 * @param player Player to update.
	 * @param goals The amount of goals the player made in the match.
	 * @param opponentRating The rating of the opposing player(s).
	 * @param won Whether the player won the match.
	 * @return The player.
	 */
	private Player updatePlayer(Player player, int goals, int goalsAgainst, int opponentRating, boolean won) {
		if (goals > 0) {
			player.addGoals(goals);
		}

		if (goalsAgainst > 0) {
			player.addGoalsAgainst(goalsAgainst);
		}

		// Adjust the player's rating.
		int newRating = mRatingSystem.newRating(
				player.getRating(),
				opponentRating,
				won ? EloRatingSystem.SCORE_WIN : EloRatingSystem.SCORE_LOSS);
		player.setRating(newRating);

		if (won) {
			player.addWin();
		} else {
			player.addLoss();
		}

		ContentValues values = new ContentValues();
		values.put(Player.NAME, player.getName());
		values.put(Player.GOALS, player.getGoals());
		values.put(Player.GOALS_AGAINST, player.getGoalsAgainst());
		values.put(Player.WINS, player.getWins());
		values.put(Player.LOSSES, player.getLosses());
		values.put(Player.RATING, player.getRating());
		int numAffectedRows = getContentResolver().update(
				Uri.withAppendedPath(Player.CONTENT_URI, String.valueOf(player.getId())),
				values,
				null,
				null);

		if (numAffectedRows == 0) {
			Logger.error(TAG, "Failed to update player #" + player.getId() + " (player not found).");
		}

		return player;
	}
}