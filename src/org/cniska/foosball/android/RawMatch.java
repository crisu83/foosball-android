package org.cniska.foosball.android;

import android.os.Parcel;
import android.os.Parcelable;

public class RawMatch implements Parcelable {

	private static final String TAG = "RawMatch";

	// Static variables
	// ----------------------------------------

	public static final Parcelable.Creator<RawMatch> CREATOR = new Parcelable.Creator<RawMatch>() {
		@Override
		public RawMatch createFromParcel(Parcel source) {
			return new RawMatch(source);
		}

		@Override
		public RawMatch[] newArray(int size) {
			return new RawMatch[size];
		}
	};

    private static final int MAX_HISTORY_LENGTH = 32;

    public static final int NUM_SUPPORTED_PLAYERS = 4;
    public static final int DEFAULT_NUM_GOALS_TO_WIN = 10;

    public static final int TEAM_NONE = 0;
    public static final int TEAM_HOME = 1;
    public static final int TEAM_AWAY = 2;

	// Member variables
	// ----------------------------------------

    private long mId;
	private long mCreated;
	private long mStartTime;
	private long mDuration;
    private int mNumHomeTeamGoals = 0;
    private int mNumAwayTeamGoals = 0;
    private int mNumGoalsToWin = DEFAULT_NUM_GOALS_TO_WIN;
    private int mWinningTeam = TEAM_NONE;
    private int mHistoryIndex = 0;
	private boolean mRanked = true;
	private int[] mHistory = new int[MAX_HISTORY_LENGTH];
	private long[] mPlayerIds = new long[NUM_SUPPORTED_PLAYERS];

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new match.
	 */
    public RawMatch() {
		mStartTime = System.currentTimeMillis() / 1000L; // unix timestamp
    }

	/**
	 * Creates a new match from parcel.
	 * @param source Parcel
	 */
    public RawMatch(Parcel source) {
        this();
		mId = source.readLong();
		mCreated = source.readLong();
        mNumHomeTeamGoals = source.readInt();
        mNumAwayTeamGoals = source.readInt();
        mNumGoalsToWin = source.readInt();
        mWinningTeam = source.readInt();
        mHistoryIndex = source.readInt();
		mRanked = source.readByte() == 1;
		source.readIntArray(mHistory);
		source.readLongArray(mPlayerIds);
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
		parcel.writeLong(mId);
		parcel.writeLong(mCreated);
        parcel.writeInt(mNumHomeTeamGoals);
        parcel.writeInt(mNumAwayTeamGoals);
        parcel.writeInt(mNumGoalsToWin);
        parcel.writeInt(mWinningTeam);
        parcel.writeInt(mHistoryIndex);
		parcel.writeByte((byte) (mRanked ? 1 : 0));
		parcel.writeIntArray(mHistory);
		parcel.writeLongArray(mPlayerIds);
	}

	/**
	 * Adds a goal for the home team.
	 * @return True if the goal was added, otherwise false.
	 */
    public boolean addHomeTeamGoal() {
        if (!hasEnded()) {
            mNumHomeTeamGoals++;
            addHistoryItem(TEAM_HOME);
			if (hasHomeTeamWon()) {
				mWinningTeam = TEAM_HOME;
			}
            return true;
        }
        return false;
    }

	/**
	 * Adds a goal for the away team.
	 * @return True if the goal was added, otherwise false.
	 */
    public boolean addAwayTeamGoal() {
        if (!hasEnded()) {
            mNumAwayTeamGoals++;
            addHistoryItem(TEAM_AWAY);
			if (hasAwayTeamWon()) {
				mWinningTeam = TEAM_AWAY;
				mDuration = (System.currentTimeMillis() / 1000) - mStartTime;
			}
            return true;
        }
        return false;
    }

	/**
	 * Adds an item to the action history.
	 * @param item History item.
	 */
    private void addHistoryItem(int item) {
        mHistory[mHistoryIndex++] = item;
    }

	/**
	 * Removes the newest item from the action history.
	 * @return History item.
	 */
    private int popHistoryItem() {
        return mHistory[mHistoryIndex--];
    }

	/**
	 * Takes back the most recent action.
	 * @return True if the action was undone, otherwise false.
	 */
    public boolean undoAction() {
        if (mHistoryIndex > 0) {
            int historyItem = popHistoryItem();

            switch (historyItem) {
                case TEAM_HOME:
                    mNumHomeTeamGoals--;
                    Logger.info(TAG, "Score removed from the home team.");
                    return true;

                case TEAM_AWAY:
                    mNumAwayTeamGoals--;
                    Logger.info(TAG, "Score removed from the away team.");
                    return true;

                default:
                    Logger.error(this.TAG, "Failed to undo action (type unknown).");
            }
        }
		return false;
    }

	/**
	 * Returns whether the match has ended or not.
	 * @return The result.
	 */
    public boolean hasEnded() {
        return hasHomeTeamWon() || hasAwayTeamWon();
    }

	/**
	 * Returns whether the home team has won or not.
	 * @return The result.
	 */
	private boolean hasHomeTeamWon() {
		return mNumHomeTeamGoals == mNumGoalsToWin;
	}

	/**
	 * Returns whether the away team has won or not.
	 * @return The result.
	 */
	private boolean hasAwayTeamWon() {
		return mNumAwayTeamGoals == mNumGoalsToWin;
	}

	// Getters and setters
	// ----------------------------------------

	public long getId() {
		return mId;
	}

	public void setId(long mId) {
		this.mId = mId;
	}

	public long getCreated() {
		return mCreated;
	}

	public void setCreated(long created) {
		mCreated = created;
	}

	public long getDuration() {
		return mDuration;
	}

	public int getNumHomeTeamGoals() {
        return mNumHomeTeamGoals;
    }

    public int getNumAwayTeamGoals() {
        return mNumAwayTeamGoals;
    }

	public int getNumGoalsToWin() {
		return mNumGoalsToWin;
	}

	public void setNumGoalsToWin(int numGoalsToWin) {
        mNumGoalsToWin = numGoalsToWin;
    }

    public int getWinningTeam() {
        return mWinningTeam;
    }

	public boolean isRanked() {
		return mRanked;
	}

	public void setRanked(boolean ranked) {
		mRanked = ranked;
	}

	public long[] getPlayerIds() {
		return mPlayerIds;
	}

	public void setPlayerIds(long[] playerIds) {
		mPlayerIds = playerIds;
	}
}
