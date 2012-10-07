package org.cniska.foosball;

public class Player {

	// Member variables
	// ----------------------------------------

	private long id;
	private String name;
	private int goals;
	private int wins;
	private int losses;

	// Methods
	// ----------------------------------------

	/**
	 * Adds goals for the player.
	 * @param amount Amount of goals to add.
	 */
	public void addGoals(int amount) {
		goals += amount;
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
	 * Calculates the player's ratio (wins/losses).
	 * @return The ratio.
	 */
	public float calcRatio() {
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

	public String toString() {
		return name;
	}
}
