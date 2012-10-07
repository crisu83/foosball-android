package org.cniska.foosball;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

public class PlayerDataSource {

	// Member variables
	// ----------------------------------------

	private SQLiteDatabase db;
	private DbHelper helper;
	private String[] columns = {
		DbHelper.COLUMN_ID,
		DbHelper.COLUMN_NAME,
		DbHelper.COLUMN_GOALS,
		DbHelper.COLUMN_WINS,
		DbHelper.COLUMN_LOSSES
	};

	// Methods
	// ----------------------------------------

	public PlayerDataSource(Context context) {
		helper = new DbHelper(context);
	}

	public void open() throws SQLiteException {
		if (db == null) {
			db = helper.getWritableDatabase();
		}
	}

	public void close() {
		helper.close();
	}

	public Player createPlayer(String name) {
		Player player = null;
		ContentValues values = new ContentValues();
		values.put(DbHelper.COLUMN_NAME, name);
		long insertId = db.insert(DbHelper.TABLE_PLAYER, null, values);
		String[] selectionArgs = new String[] { String.valueOf(insertId) };
		Cursor cursor = db.query(DbHelper.TABLE_PLAYER, columns, DbHelper.COLUMN_ID + "=?", selectionArgs, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				player = cursorToPlayer(cursor);
			}
			cursor.close();
		}
		return player;
	}

	public int updatePlayer(Player player) {
		ContentValues values = new ContentValues();
		values.put(DbHelper.COLUMN_NAME, player.getName());
		values.put(DbHelper.COLUMN_GOALS, player.getGoals());
		values.put(DbHelper.COLUMN_WINS, player.getWins());
		values.put(DbHelper.COLUMN_LOSSES, player.getLosses());
		String[] whereArgs = new String[] { String.valueOf(player.getId()) };
		return db.update(DbHelper.TABLE_PLAYER, values, DbHelper.COLUMN_ID + "=?", whereArgs);
	}

	public Player findPlayerByName(String name) {
		Player player = null;
		String[] selectionArgs = new String[] { name };
		Cursor cursor = db.query(DbHelper.TABLE_PLAYER, columns, DbHelper.COLUMN_NAME + "=?", selectionArgs, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				player = cursorToPlayer(cursor);
			}
			cursor.close();
		}
		return player;
	}

	public List<Player> findAllPlayers() {
		List<Player> players = new ArrayList<Player>();
		Cursor cursor = db.query(DbHelper.TABLE_PLAYER, columns, null, null, null, null, null);
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
