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

public class PlayerProvider extends ContentProvider {

	private static final String TAG = PlayerProvider.class.getName();

	private DatabaseHelper databaseHelper;

	private static final int PLAYERS = 1;
	private static final int PLAYER_ID = 2;
	private static final int PLAYER_FILTER = 3;

	public static final String AUTHORITY ="org.cniska.foosball.android.PlayerProvider";
	public static final String BASE_PATH = "players";

	private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static Map<String, String> projectionMap;

	public static String[] projectionArray = {
		Player._ID,
		Player.CREATED,
		Player.STATUS,
		Player.RESULT,
		Player.NAME,
		Player.GOALS,
		Player.GOALS_AGAINST,
		Player.WINS,
		Player.LOSSES,
		Player.RATING
	};

	static {
		uriMatcher.addURI(AUTHORITY, BASE_PATH, PLAYERS);
		uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PLAYER_ID);

		projectionMap = new HashMap<String, String>();
		projectionMap.put(Player._ID, Player._ID);
		projectionMap.put(Player.CREATED, Player.CREATED);
		projectionMap.put(Player.STATUS, Player.STATUS);
		projectionMap.put(Player.RESULT, Player.RESULT);
		projectionMap.put(Player.NAME, Player.NAME);
		projectionMap.put(Player.GOALS, Player.GOALS);
		projectionMap.put(Player.GOALS_AGAINST, Player.GOALS_AGAINST);
		projectionMap.put(Player.WINS, Player.WINS);
		projectionMap.put(Player.LOSSES, Player.LOSSES);
		projectionMap.put(Player.RATING, Player.RATING);
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.TABLE_PLAYER);
		builder.setProjectionMap(projectionMap);

		switch (uriMatcher.match(uri)) {
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

		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
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
		if (uriMatcher.match(uri) != PLAYERS) {
			throwUnknownUriException(uri);
		}

		if (values.getAsLong(Player.CREATED) == null) {
			values.put(Player.CREATED, System.currentTimeMillis() / 1000L); // current unix time
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		long insertId = db.insert(DatabaseHelper.TABLE_PLAYER, null, values);

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
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		switch (uriMatcher.match(uri)) {
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

		int numAffectedRows = db.delete(DatabaseHelper.TABLE_PLAYER, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return numAffectedRows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch(uriMatcher.match(uri)) {
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

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int numAffectedRows = db.update(DatabaseHelper.TABLE_PLAYER, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return numAffectedRows;
	}

	private void throwUnknownUriException(Uri uri) throws IllegalArgumentException {
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}
}
