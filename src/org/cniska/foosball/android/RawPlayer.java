package org.cniska.foosball.android;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a single player record.
 */
public class RawPlayer implements Parcelable {

	private static final String TAG = "RawPlayer";

	// Static variables
	// ----------------------------------------

	public static final Parcelable.Creator<RawPlayer> CREATOR = new Parcelable.Creator<RawPlayer>() {
		@Override
		public RawPlayer createFromParcel(Parcel source) {
			return new RawPlayer(source);
		}

		@Override
		public RawPlayer[] newArray(int size) {
			return new RawPlayer[size];
		}
	};

	// Member variables
	// ----------------------------------------

	private long mId;
	private long mCreated;
	private String mName;

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new player.
	 */
	public RawPlayer() {
	}

    /**
     * Creates a new player from the given cursor.
     * @param cursor
     */
    public RawPlayer(Cursor cursor) {
        int index;
        
        if ((index = cursor.getColumnIndex(DataContract.Players._ID)) != -1) {
            mId = cursor.getLong(index);
        }
        if ((index = cursor.getColumnIndex(DataContract.Players.CREATED)) != -1) {
            mCreated = cursor.getLong(index);
        }
        if ((index = cursor.getColumnIndex(DataContract.Players.NAME)) != -1) {
            mName = cursor.getString(index);
        }
    }

    /**
     * Creates a new player from the given parcel.
     * @param source
     */
	private RawPlayer(Parcel source) {
		this();
		mId = source.readLong();
		mName = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mName);
	}

	/**
	 * Returns the player as a string.
	 * @return The result.
	 */
	public String toString() {
		return mName;
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
}
