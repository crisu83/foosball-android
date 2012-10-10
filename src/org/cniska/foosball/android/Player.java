package org.cniska.foosball.android;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a single player record.
 */
public class Player implements Parcelable {

	// Static variables
	// ----------------------------------------

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

	private long mId;
	private long mCreated;
	private String mName;
	private int mGoals;
	private int mGoalsAgainst;
	private int mWins;
	private int mLosses;
	private int mRating;

	// Methods
	// ----------------------------------------

	public Player() {
		mGoals = 0;
		mGoalsAgainst = 0;
		mWins = 0;
		mLosses = 0;
		mRating = EloRatingSystem.INITIAL_RATING;
	}

	private Player(Parcel source) {
		mId = source.readLong();
		mName = source.readString();
		mGoals = source.readInt();
		mGoalsAgainst = source.readInt();
		mWins = source.readInt();
		mLosses = source.readInt();
		mRating = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mName);
		dest.writeInt(mGoals);
		dest.writeInt(mGoalsAgainst);
		dest.writeInt(mWins);
		dest.writeInt(mLosses);
		dest.writeInt(mRating);
	}

	/**
	 * Adds goals for the player.
	 * @param amount Amount of goals to add.
	 */
	public void addGoals(int amount) {
		mGoals += amount;
	}

	/**
	 * Adds goals against the player.
	 * @param amount Amount of goals to add.
	 */
	public void addGoalsAgainst(int amount) {
		mGoalsAgainst += amount;
	}

	/**
	 * Adds a win for the player.
	 */
	public void addWin() {
		mWins++;
	}

	/**
	 * Adds a loss for the player.
	 */
	public void addLoss() {
		mLosses++;
	}

	/**
	 * Returns the amount of matches the player has played.
	 * @return The amount.
	 */
	public int matchesPlayed() {
		return mWins + mLosses;
	}

	/**
	 * Calculates the player's win/loss ratio.
	 * @return The result.
	 */
	public float winLossRatio() {
		float ratio;
		if (mWins > 0 && mLosses > 0) {
			ratio = ((float) mWins) / mLosses;
		} else if (mWins > 0 && mLosses == 0) {
			ratio = mWins;
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
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public long getCreated() {
		return mCreated;
	}

	public void setCreated(long created) {
		this.mCreated = created;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getGoals() {
		return mGoals;
	}

	public void setGoals(int goals) {
		this.mGoals = goals;
	}

	public int getGoalsAgainst() {
		return mGoalsAgainst;
	}

	public void setGoalsAgainst(int goalsAgainst) {
		this.mGoalsAgainst = goalsAgainst;
	}

	public int getWins() {
		return mWins;
	}

	public void setWins(int wins) {
		this.mWins = wins;
	}

	public int getLosses() {
		return mLosses;
	}

	public void setLosses(int losses) {
		this.mLosses = losses;
	}

	public int getRating() {
		return mRating;
	}

	public void setRating(int rating) {
		this.mRating = rating;
	}
}
