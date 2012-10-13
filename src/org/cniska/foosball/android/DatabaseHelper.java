package org.cniska.foosball.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class handles the database connection to the local SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	// Static variables
	// ----------------------------------------

	private static final String TAG = DatabaseHelper.class.getName();

	private static final String DATABASE_NAME = "foosball.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_PLAYERS = "players";

	private static final String CREATE_TABLE_PLAYER = "CREATE TABLE " + TABLE_PLAYERS + " (" +
			Player._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			Player.CREATED + " INTEGER NOT NULL, " +
			Player.STATUS + " INTEGER NOT NULL DEFAULT '0', " +
			Player.RESULT + " INTEGER NOT NULL DEFAULT '0', " +
			Player.NAME + " TEXT NOT NULL DEFAULT '0', " +
			Player.GOALS_FOR + " INTEGER NOT NULL DEFAULT '0', " +
			Player.GOALS_AGAINST + " INTEGER NOT NULL DEFAULT '0', " +
			Player.WINS + " INTEGER NOT NULL DEFAULT '0', " +
			Player.LOSSES + " INTEGER NOT NULL DEFAULT '0', " +
			Player.RATING + " INTEGER NOT NULL DEFAULT '" + EloRatingSystem.INITIAL_RATING + "');";

	private static final String DROP_TABLE_PLAYER = "DROP TABLE IF EXISTS " + TABLE_PLAYERS + ";";

	private static final String DATABASE_SCHEMA = CREATE_TABLE_PLAYER;

	// Methods
	// ----------------------------------------

	/**
	 * Creates a database helper.
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_SCHEMA);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.warn(TAG, "Upgrading database foosball from version " + oldVersion + " to " + newVersion +
				". All existing data will be destroyed.");
		db.execSQL(DROP_TABLE_PLAYER);
		onCreate(db);
	}
}
