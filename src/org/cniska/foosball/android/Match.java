package org.cniska.foosball.android;

import java.util.ArrayList;
import java.util.List;

public class Match {

	// Static variables
	// ----------------------------------------

	public static final String TAG = Match.class.getName();

	// Member variables
	// ----------------------------------------

	private int redTeamScore = 0;
	private int blueTeamScore = 0;
	private List<Player> players;

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new match.
	 */
	public Match() {
		players = new ArrayList<Player>(4);
	}

	/**
	 * Adds the given player to the match.
	 * @param player The player.
	 */
	public void addPlayer(Player player) {
		if (players.size() < 4) {
			players.add(player);
		} else {
			Logger.error(TAG, "Failed to add player (Limit exceeded).");
		}
	}

	// Getters and setters
	// ----------------------------------------

	public void setRedTeamScore(int redTeamScore) {
		this.redTeamScore = redTeamScore;
	}

	public void setBlueTeamScore(int blueTeamScore) {
		this.blueTeamScore = blueTeamScore;
	}
}
