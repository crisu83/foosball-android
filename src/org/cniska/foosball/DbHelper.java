package org.cniska.foosball;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	// Static variables
	// ----------------------------------------

	private static final String DATABASE_NAME = "foosball.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_PLAYER = "player";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_GOALS = "goals";
	public static final String COLUMN_WINS = "wins";
	public static final String COLUMN_LOSSES = "losses";

	// Database creation statement
	private static final String DATABASE_CREATE =
			"CREATE TABLE " + TABLE_PLAYER + " (" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
			COLUMN_GOALS + " INTEGER NOT NULL DEFAULT '0', " +
			COLUMN_WINS + " INTEGER NOT NULL DEFAULT '0', " +
			COLUMN_LOSSES + " INTEGER NOT NULL DEFAULT '0');";

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new database helper.
	 * @param context
	 */
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
		onCreate(db);
	}
}
