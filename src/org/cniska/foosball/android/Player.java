package org.cniska.foosball.android;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {

	public static final String _ID = "_id";
	public static final String CREATED = "created";
	public static final String STATUS = "status";
	public static final String RESULT = "result";
	public static final String NAME = "name";
	public static final String GOALS = "goals";
	public static final String GOALS_AGAINST = "goals_against";
	public static final String WINS = "wins";
	public static final String LOSSES = "losses";
	public static final String RATING = "rating";

	public static final Uri CONTENT_URI = Uri.parse(
			"content://" + PlayerProvider.AUTHORITY + "/" + PlayerProvider.BASE_PATH);

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.foosball.player";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.foosball.player";

	public static final String DEFAULT_SORT_ORDER = "name ASC";

	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
		@Override
		public Player createFromParcel(Parcel source) {
			return new Player(source);
		}

		@Override
		public Player[] newArray(int size) {
			return new Player[size];
		}
	};

	// Member variables
	// ----------------------------------------

	private long id;
	private long created;
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

	private Player(Parcel source) {
		id = source.readLong();
		name = source.readString();
		goals = source.readInt();
		goalsAgainst = source.readInt();
		wins = source.readInt();
		losses = source.readInt();
		rating = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeInt(goals);
		dest.writeInt(goalsAgainst);
		dest.writeInt(wins);
		dest.writeInt(losses);
		dest.writeInt(rating);
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

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
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
