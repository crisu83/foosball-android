package org.cniska.foosball;

public class Player {

	// Member variables
	// ----------------------------------------

	private long id;
	private String name;
	private int goals;
	private int goalsAgainst;
	private int wins;
	private int losses;
	private int rating;

	// Methods
	// ----------------------------------------


	public Player() {
		goals = 0;
		goalsAgainst = 0;
		wins = 0;
		losses = 0;
		rating = EloRatingSystem.INITIAL_RATING;
	}

	/**
	 * Adds goals for the player.
	 * @param amount Amount of goals to add.
	 */
	public void addGoals(int amount) {
		goals += amount;
	}

	/**
	 * Adds goals against the player.
	 * @param amount Amount of goals to add.
	 */
	public void addGoalsAgainst(int amount) {
		goalsAgainst += amount;
	}

	/**
	 * Adds a win for the player.
	 */
	public void addWin() {
		wins++;
	}

	/**
	 * Adds a loss for the player.
	 */
	public void addLoss() {
		losses++;
	}

	/**
	 * Returns the amount of matches the player has played.
	 * @return The amount.
	 */
	public int matchesPlayed() {
		return wins + losses;
	}

	/**
	 * Calculates the player's win/loss ratio.
	 * @return The result.
	 */
	public float winLossRatio() {
		float ratio;
		if (wins > 0 && losses > 0) {
			ratio = ((float) wins) / losses;
		} else if (wins > 0 && losses == 0) {
			ratio = wins;
		} else {
			ratio = 0;
		}
		return ratio;
	}

	public String toString() {
		return getName();
	}

	// Getters and setters
	// ----------------------------------------

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGoals() {
		return goals;
	}

	public void setGoals(int goals) {
		this.goals = goals;
	}

	public int getGoalsAgainst() {
		return goalsAgainst;
	}

	public void setGoalsAgainst(int goalsAgainst) {
		this.goalsAgainst = goalsAgainst;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
}
