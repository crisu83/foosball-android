package org.cniska.foosball;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class SQLitePlayerDataSource extends SQLiteDataSource implements PlayerDataSource {

	// Member variables
	// ----------------------------------------

	private String[] columns = {
		SQLiteHelper.COLUMN_ID,
		SQLiteHelper.COLUMN_CREATED,
		SQLiteHelper.COLUMN_NAME,
		SQLiteHelper.COLUMN_GOALS,
		SQLiteHelper.COLUMN_GOALS_AGAINST,
		SQLiteHelper.COLUMN_WINS,
		SQLiteHelper.COLUMN_LOSSES,
		SQLiteHelper.COLUMN_RATING
	};

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new data source.
	 * @param context
	 */
	public SQLitePlayerDataSource(Context context) {
		super(context);
	}

	@Override
	public Player createPlayer(String name) {
		Player player = null;
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_CREATED, currentUnixTime());
		values.put(SQLiteHelper.COLUMN_NAME, name);
		values.put(SQLiteHelper.COLUMN_RATING, EloRatingSystem.INITIAL_RATING);
		long insertId = db.insert(SQLiteHelper.TABLE_PLAYER, null, values);
		String[] selectionArgs = new String[] { String.valueOf(insertId) };
		Cursor cursor = db.query(SQLiteHelper.TABLE_PLAYER, columns, SQLiteHelper.COLUMN_ID + "=?", selectionArgs, null, null, null);
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
		values.put(SQLiteHelper.COLUMN_NAME, player.getName());
		values.put(SQLiteHelper.COLUMN_GOALS, player.getGoals());
		values.put(SQLiteHelper.COLUMN_GOALS_AGAINST, player.getGoalsAgainst());
		values.put(SQLiteHelper.COLUMN_WINS, player.getWins());
		values.put(SQLiteHelper.COLUMN_LOSSES, player.getLosses());
		values.put(SQLiteHelper.COLUMN_RATING, player.getRating());
		String[] whereArgs = new String[] { String.valueOf(player.getId()) };
		return db.update(SQLiteHelper.TABLE_PLAYER, values, SQLiteHelper.COLUMN_ID + "=?", whereArgs);
	}

	@Override
	public Player findPlayerByName(String name) {
		Player player = null;
		String[] selectionArgs = new String[] { name };
		Cursor cursor = db.query(SQLiteHelper.TABLE_PLAYER, columns, SQLiteHelper.COLUMN_NAME + "=?", selectionArgs, null, null, null);
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
		Cursor cursor = db.query(SQLiteHelper.TABLE_PLAYER, columns, null, null, null, null, null);
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
		player.setName(cursor.getString(2));
		player.setGoals(cursor.getInt(3));
		player.setGoalsAgainst(cursor.getInt(4));
		player.setWins(cursor.getInt(5));
		player.setLosses(cursor.getInt(6));
		player.setRating(cursor.getInt(7));
		return player;
	}
}
