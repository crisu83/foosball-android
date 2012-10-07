package org.cniska.foosball;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

public class SQLitePlayerDataSource implements PlayerDataSource {

	// Member variables
	// ----------------------------------------

	private SQLiteDatabase db;
	private SQLiteDbHelper helper;
	private String[] columns = {
		SQLiteDbHelper.COLUMN_ID,
		SQLiteDbHelper.COLUMN_NAME,
		SQLiteDbHelper.COLUMN_GOALS,
		SQLiteDbHelper.COLUMN_WINS,
		SQLiteDbHelper.COLUMN_LOSSES
	};

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new data source.
	 * @param context
	 */
	public SQLitePlayerDataSource(Context context) {
		helper = new SQLiteDbHelper(context);
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

	@Override
	public Player createPlayer(String name) {
		Player player = null;
		ContentValues values = new ContentValues();
		values.put(SQLiteDbHelper.COLUMN_NAME, name);
		long insertId = db.insert(SQLiteDbHelper.TABLE_PLAYER, null, values);
		String[] selectionArgs = new String[] { String.valueOf(insertId) };
		Cursor cursor = db.query(SQLiteDbHelper.TABLE_PLAYER, columns, SQLiteDbHelper.COLUMN_ID + "=?", selectionArgs, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				player = cursorToPlayer(cursor);
			}
			cursor.close();
		}
		return player;
	}

	@Override
	public int updatePlayer(Player player) {
		ContentValues values = new ContentValues();
		values.put(SQLiteDbHelper.COLUMN_NAME, player.getName());
		values.put(SQLiteDbHelper.COLUMN_GOALS, player.getGoals());
		values.put(SQLiteDbHelper.COLUMN_WINS, player.getWins());
		values.put(SQLiteDbHelper.COLUMN_LOSSES, player.getLosses());
		String[] whereArgs = new String[] { String.valueOf(player.getId()) };
		return db.update(SQLiteDbHelper.TABLE_PLAYER, values, SQLiteDbHelper.COLUMN_ID + "=?", whereArgs);
	}

	@Override
	public Player findPlayerByName(String name) {
		Player player = null;
		String[] selectionArgs = new String[] { name };
		Cursor cursor = db.query(SQLiteDbHelper.TABLE_PLAYER, columns, SQLiteDbHelper.COLUMN_NAME + "=?", selectionArgs, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				player = cursorToPlayer(cursor);
			}
			cursor.close();
		}
		return player;
	}

	@Override
	public List<Player> findAllPlayers() {
		List<Player> players = new ArrayList<Player>();
		Cursor cursor = db.query(SQLiteDbHelper.TABLE_PLAYER, columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Player player = cursorToPlayer(cursor);
				players.add(player);
				cursor.moveToNext();
			}
		}
		cursor.close();
		return players;
	}

	/**
	 * Creates a player from the database record the given cursor is pointing at.
	 * @param cursor The cursor.
	 * @return The player.
	 */
	private Player cursorToPlayer(Cursor cursor) {
		Player player = new Player();
		player.setId(cursor.getLong(0));
		player.setName(cursor.getString(1));
		player.setGoals(cursor.getInt(2));
		player.setWins(cursor.getInt(3));
		player.setLosses(cursor.getInt(4));
		return player;
	}
}
