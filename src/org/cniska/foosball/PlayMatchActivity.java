package org.cniska.foosball;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlayMatchActivity extends Activity {

	// Enumerables
	// ----------------------------------------

	public enum TeamType { RED, BLUE };
	public enum PlayerType { PLAYER1, PLAYER2, PLAYER3, PLAYER4 };

	// Static variables
	// ----------------------------------------

	private static final String STATE_NUM_GOALS_TO_WIN = "org.cniska.foosball.NUM_GOALS_TO_WIN";
	private static final String STATE_NUM_GOALS_PLAYER1 = "org.cniska.foosball.NUM_GOALS_PLAYER1";
	private static final String STATE_NUM_GOALS_PLAYER2 = "org.cniska.foosball.NUM_GOALS_PLAYER2";
	private static final String STATE_NUM_GOALS_PLAYER3 = "org.cniska.foosball.NUM_GOALS_PLAYER3";
	private static final String STATE_NUM_GOALS_PLAYER4 = "org.cniska.foosball.NUM_GOALS_PLAYER4";

	// Member variables
	// ----------------------------------------

	PowerManager.WakeLock wakeLock;

	private String namePlayer1;
	private String namePlayer2;
	private String namePlayer3;
	private String namePlayer4;

	private int numGoalsToWin;
	private int numGoalsPlayer1 = 0;
	private int numGoalsPlayer2 = 0;
	private int numGoalsPlayer3 = 0;
	private int numGoalsPlayer4 = 0;

	private SQLitePlayerDataSource data;

	private Player player1;
	private Player player2;
	private Player player3;
	private Player player4;

	private List<PlayerType> history;

	private boolean activePlayer3 = false;
	private boolean activePlayer4 = false;
	private boolean ended = false;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		history = new ArrayList<PlayerType>();

		PowerManager powerManager = (PowerManager) getSystemService(this.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getClass().getName());

		data = new SQLitePlayerDataSource(this);
		data.open();

		// Restore member variables if state has changed.
		if (savedInstanceState != null) {
			numGoalsToWin = savedInstanceState.getInt(STATE_NUM_GOALS_TO_WIN);
			numGoalsPlayer1 = savedInstanceState.getInt(STATE_NUM_GOALS_PLAYER1);
			numGoalsPlayer2 = savedInstanceState.getInt(STATE_NUM_GOALS_PLAYER2);
			numGoalsPlayer3 = savedInstanceState.getInt(STATE_NUM_GOALS_PLAYER3);
			numGoalsPlayer4 = savedInstanceState.getInt(STATE_NUM_GOALS_PLAYER4);
		}

		// Process intent data.
		Intent intent = getIntent();
		namePlayer1 = intent.getStringExtra(NewMatchActivity.EXTRA_PLAYER1);
		namePlayer2 = intent.getStringExtra(NewMatchActivity.EXTRA_PLAYER2);
		namePlayer3 = intent.getStringExtra(NewMatchActivity.EXTRA_PLAYER3);
		namePlayer4 = intent.getStringExtra(NewMatchActivity.EXTRA_PLAYER4);
		numGoalsToWin = Integer.parseInt(intent.getStringExtra(NewMatchActivity.EXTRA_SCORES_TO_WIN));

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
		buttonPlayer1.setText(namePlayer1);
		buttonPlayer2.setText(namePlayer2);

		// Check whether player 3 is playing.
		if (!namePlayer3.isEmpty()) {
			buttonPlayer3.setText(namePlayer3);
			activePlayer3 = true;
		} else {
			// Player 3 is not playing, remove the corresponding button.
			LinearLayout layoutTeamRed = (LinearLayout) findViewById(R.id.layout_team_red);
			layoutTeamRed.removeView(buttonPlayer3);
		}

		// Check whether player 4 is playing.
		if (!namePlayer4.isEmpty()) {
			buttonPlayer4.setText(namePlayer4);
			activePlayer4 = true;
		} else {
			// Player 4 is not playing, remove the corresponding button.
			LinearLayout layoutTeamBlue = (LinearLayout) findViewById(R.id.layout_team_blue);
			layoutTeamBlue.removeView(buttonPlayer4);
		}

		Logger.info(getClass().getName(), "Activity created.");
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

		state.putInt(STATE_NUM_GOALS_TO_WIN, numGoalsToWin);
		state.putInt(STATE_NUM_GOALS_PLAYER1, numGoalsPlayer1);
		state.putInt(STATE_NUM_GOALS_PLAYER2, numGoalsPlayer2);
		state.putInt(STATE_NUM_GOALS_PLAYER3, numGoalsPlayer3);
		state.putInt(STATE_NUM_GOALS_PLAYER4, numGoalsPlayer4);

		Logger.info(getClass().getName(), "Activity state saved.");
	}

	@Override
	protected void onResume() {
		data.open();
		wakeLock.acquire();
		Logger.info(getClass().getName(), "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		data.close();
		wakeLock.release();
		Logger.info(getClass().getName(), "Activity paused.");
		super.onPause();
	}

	/**
	 * Adds a goal for player 1 (red team).
	 * @param view
	 */
	public void addGoalPlayer1(View view) {
		if (!ended) {
			numGoalsPlayer1++;
			history.add(PlayerType.PLAYER1);
			Logger.info(getClass().getName(), "Goal logged for player 1.");
			updateRedTeamScore();
		} else {
			Logger.error(getClass().getName(), "Failed to log goal for player 1 (match has ended).");
		}
	}

	/**
	 * Adds a goal for player 2 (blue team).
	 * @param view
	 */
	public void addGoalPlayer2(View view) {
		if (!ended) {
			numGoalsPlayer2++;
			history.add(PlayerType.PLAYER2);
			Logger.info(getClass().getName(), "Goal logged for player 2.");
			updateBlueTeamScore();
		} else {
			Logger.error(getClass().getName(), "Failed to log goal for player 2 (match has ended).");
		}
	}

	/**
	 * Adds a goal for player 3 (red team).
	 * @param view
	 */
	public void addGoalPlayer3(View view) {
		if (!ended) {
			if (activePlayer3) {
				numGoalsPlayer3++;
				history.add(PlayerType.PLAYER3);
				Logger.info(getClass().getName(), "Goal logged for player 3.");
				updateRedTeamScore();
			} else {
				Logger.error(getClass().getName(), "Failed to log goal for player 3 (player not playing).");
			}
		} else {
			Logger.error(getClass().getName(), "Failed to log goal for player 3 (match has ended).");
		}
	}

	/**
	 * Adds a goal for player 4 (blue team).
	 * @param view
	 */
	public void addGoalPlayer4(View view) {
		if (!ended) {
			if (activePlayer4) {
				numGoalsPlayer4++;
				history.add(PlayerType.PLAYER4);
				Logger.info(getClass().getName(), "Goal logged for player 4.");
				updateBlueTeamScore();
			} else {
				Logger.error(getClass().getName(), "Failed to log goal for player 4 (player not playing).");
			}
		} else {
			Logger.error(getClass().getName(), "Failed to log goal for player 4 (match has ended).");
		}
	}

	/**
	 * Updates the red team score and checks if the red team has won.
	 */
	private void updateRedTeamScore() {
		renderRedScore();

		if (redTeamGoals() >= numGoalsToWin) {
			end(TeamType.RED);
		}
	}

	/**
	 * Updates the blue team score and checks if the red team has won.
	 */
	private void updateBlueTeamScore() {
		renderBlueScore();

		if (blueTeamGoals() >= numGoalsToWin) {
			end(TeamType.BLUE);
		}
	}

	/**
	 * Returns the number of goals the red team has.
	 * @return The amount.
	 */
	private int redTeamGoals() {
		return numGoalsPlayer1 + numGoalsPlayer3;
	}

	/**
	 * Returns the number of goals the blue team has.
	 * @return The amount.
	 */
	private int blueTeamGoals() {
		return numGoalsPlayer2 + numGoalsPlayer4;
	}

	/**
	 * Takes back the most recent action preformed.
	 * Called when the undo menu item is pressed.
	 */
	private void undoAction() {
		int historyLength = history.size();

		if (historyLength > 0) {
			PlayerType playerType = history.remove(historyLength - 1);

			switch (playerType) {
				case PLAYER1:
					numGoalsPlayer1--;
					Logger.info(getClass().getName(), "Score removed from player 1.");
					updateRedTeamScore();
					break;
				case PLAYER2:
					numGoalsPlayer2--;
					Logger.info(getClass().getName(), "Score removed from player 2.");
					updateBlueTeamScore();
					break;
				case PLAYER3:
					numGoalsPlayer3--;
					Logger.info(getClass().getName(), "Score removed from player 3.");
					updateRedTeamScore();
					break;
				case PLAYER4:
					numGoalsPlayer4--;
					Logger.info(getClass().getName(), "Score removed from player 4.");
					updateBlueTeamScore();
					break;
				default:
					Logger.error(this.getClass().getName(), "Failed to undo action (type unknown).");
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
		Logger.info(getClass().getName(), "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	/**
	 * Renders the red team score.
	 */
	private void renderRedScore() {
		TextView redTeamScore = (TextView) findViewById(R.id.team_red_score);
		redTeamScore.setText(String.valueOf(numGoalsPlayer1 + numGoalsPlayer3));
	}

	/**
	 * Renders the blue team score.
	 */
	private void renderBlueScore() {
		TextView blueTeamScore = (TextView) findViewById(R.id.team_blue_score);
		blueTeamScore.setText(String.valueOf(numGoalsPlayer2 + numGoalsPlayer4));
	}

	/**
	 * Ends the match.
	 * @param teamType Winning team.
	 */
	private void end(TeamType teamType) {
		ended = true;

		loadPlayers();

		Logger.info(getClass().getName(), "Updating player records.");

		int goalsAgainstRed = activePlayer3 ? blueTeamGoals() / 2 : blueTeamGoals();
		int goalsAgainstBlue = activePlayer4 ? redTeamGoals() / 2 : redTeamGoals();

		player1 = updatePlayer(player1, numGoalsPlayer1, goalsAgainstRed, teamType == TeamType.RED);
		data.updatePlayer(player1);

		player2 = updatePlayer(player2, numGoalsPlayer2, goalsAgainstBlue, teamType == TeamType.BLUE);
		data.updatePlayer(player2);

		if (player3 != null) {
			player3 = updatePlayer(player3, numGoalsPlayer3, goalsAgainstRed, teamType == TeamType.RED);
			data.updatePlayer(player3);
		}

		if (player4 != null) {
			player4 = updatePlayer(player4, numGoalsPlayer4, goalsAgainstBlue, teamType == TeamType.BLUE);
			data.updatePlayer(player4);
		}

		quit();
	}

	/**
	 * Loads the player records from the data source.
	 */
	private void loadPlayers() {
		Logger.info(getClass().getName(), "Loading player records.");

		player1 = data.findPlayerByName(namePlayer1);
		player2 = data.findPlayerByName(namePlayer2);

		// Create a new record for player 1 if none was found.
		if (player1 == null) {
			player1 = data.createPlayer(namePlayer1);
		}

		// Create a new record for player 2 if none was found.
		if (player2 == null) {
			player2 = data.createPlayer(namePlayer2);
		}

		// Check whether player 3 is playing.
		if (!namePlayer3.isEmpty()) {
			player3 = data.findPlayerByName(namePlayer3);

			// Create a new record for player 3 if none was found.
			if (player3 == null) {
				player3 = data.createPlayer(namePlayer3);
			}
		}

		// Check whether player 4 is playing.
		if (!namePlayer4.isEmpty()) {
			player4 = data.findPlayerByName(namePlayer4);

			// Create a new record for player 4 if none was found.
			if (player4 == null) {
				player4 = data.createPlayer(namePlayer4);
			}
		}

		Logger.info(getClass().getName(), "Player records loaded.");
	}

	/**
	 * Updates the given player by adding goals and wins or losses.
	 * @param player Player to update.
	 * @param goals The amount of goals the player made in the match.
	 * @param won Whether the player won the match.
	 * @return The player.
	 */
	private Player updatePlayer(Player player, int goals, int goalsAgainst, boolean won) {
		player.addGoals(goals);
		player.addGoalsAgainst(goalsAgainst);

		if (won) {
			player.addWin();
		} else {
			player.addLoss();
		}

		return player;
	}
}