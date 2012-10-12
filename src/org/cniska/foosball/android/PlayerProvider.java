package org.cniska.foosball.android;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This content provider provides access to the player records.
 */
public class PlayerProvider extends ContentProvider {

	// Static variables
	// ----------------------------------------

	private static final String TAG = PlayerProvider.class.getName();

	private static final int PLAYERS = 1;
	private static final int PLAYER_ID = 2;

	public static final String AUTHORITY ="org.cniska.foosball.android.PlayerProvider";
	public static final String BASE_PATH = "players";

	private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static Map<String, String> sProjectionMap;

	public static String[] sProjectionArray = {
		Player._ID,
		Player.CREATED,
		Player.STATUS,
		Player.RESULT,
		Player.NAME,
		Player.GOALS_FOR,
		Player.GOALS_AGAINST,
		Player.WINS,
		Player.LOSSES,
		Player.RATING
	};

	static {
		sUriMatcher.addURI(AUTHORITY, BASE_PATH, PLAYERS);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PLAYER_ID);

		sProjectionMap = new HashMap<String, String>();
		sProjectionMap.put(Player._ID, Player._ID);
		sProjectionMap.put(Player.CREATED, Player.CREATED);
		sProjectionMap.put(Player.STATUS, Player.STATUS);
		sProjectionMap.put(Player.RESULT, Player.RESULT);
		sProjectionMap.put(Player.NAME, Player.NAME);
		sProjectionMap.put(Player.GOALS_FOR, Player.GOALS_FOR);
		sProjectionMap.put(Player.GOALS_AGAINST, Player.GOALS_AGAINST);
		sProjectionMap.put(Player.WINS, Player.WINS);
		sProjectionMap.put(Player.LOSSES, Player.LOSSES);
		sProjectionMap.put(Player.RATING, Player.RATING);
	}

	// Member variables
	// ----------------------------------------

	private DatabaseHelper mDatabaseHelper;

	// Methods
	// ----------------------------------------

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.TABLE_PLAYERS);
		builder.setProjectionMap(sProjectionMap);

		switch (sUriMatcher.match(uri)) {
			// Find by _id
			case PLAYER_ID:
				builder.appendWhere(Player._ID + "=" + uri.getLastPathSegment());
				break;

			// No filter
			case PLAYERS:
				break;

			default:
				throwUnknownUriException(uri);
		}

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
			case PLAYER_ID:
				return Player.CONTENT_ITEM_TYPE;

			case PLAYERS:
				return Player.CONTENT_TYPE;

			default:
				throwUnknownUriException(uri);
		}

		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != PLAYERS) {
			throwUnknownUriException(uri);
		}

		if (values.getAsLong(Player.CREATED) == null) {
			values.put(Player.CREATED, System.currentTimeMillis() / 1000L); // current unix time
		}

		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

		long insertId = db.insert(DatabaseHelper.TABLE_PLAYERS, null, values);

		if (insertId > 0) {
			Uri playerUri = ContentUris.withAppendedId(Player.CONTENT_URI, insertId);
			getContext().getContentResolver().notifyChange(playerUri, null);
			return playerUri;
		} else {
			throw new SQLiteException("Failed to insert row into " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri)) {
			case PLAYERS:
				break;

			case PLAYER_ID:
				String where = Player._ID + "=" + uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					selection = where;
				} else {
					selection += " AND " + where;
				}
				break;

			default:
				throwUnknownUriException(uri);
		}

		int numAffectedRows = db.delete(DatabaseHelper.TABLE_PLAYERS, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return numAffectedRows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch(sUriMatcher.match(uri)) {
			case PLAYERS:
				break;

			case PLAYER_ID:
				String where = Player._ID + "=" + uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					selection = where;
				} else {
					selection += " AND " + where;
				}
				break;

			default:
				throwUnknownUriException(uri);
		}

		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int numAffectedRows = db.update(DatabaseHelper.TABLE_PLAYERS, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return numAffectedRows;
	}

	/**
	 * Throws an unknown uri exception when called.
	 * @param uri Unknown uri.
	 * @throws IllegalArgumentException
	 */
	private void throwUnknownUriException(Uri uri) throws IllegalArgumentException {
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}
}
