package org.cniska.foosball;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public abstract class SQLiteDataSource {

	// Member variables
	// ----------------------------------------

	protected SQLiteDatabase db;
	protected SQLiteHelper helper;

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new data source.
	 * @param context
	 */
	public SQLiteDataSource(Context context) {
		helper = new SQLiteHelper(context);
	}

	/**
	 * Opens the data source.
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException {
		if (db == null) {
			db = helper.getWritableDatabase();
		}
	}

	/**
	 * Closes the data source.
	 */
	public void close() {
		helper.close();
	}

	/**
	 * Returns the current time as a unix timestamp.
	 * @return Unix time.
	 */
	protected long currentUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}
}
