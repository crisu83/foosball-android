package org.cniska.foosball.android;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.cniska.foosball.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Enumerables
	// ----------------------------------------

	public enum SortColumn {
		PLAYER,
		GOALS,
		GOALS_AGAINST,
		WINS,
		LOSSES,
		WIN_LOSS_RATIO,
		RATING
	}

	public enum SortDirection {
		ASCENDING,
		DESCENDING
	}

	// Static variables
	// ----------------------------------------

	private static final int LAYOUT_WEIGHT_PLAYER = 30;
	private static final int LAYOUT_WEIGHT_GOALS = 10;
	private static final int LAYOUT_WEIGHT_GOALS_AGAINST = 10;
	private static final int LAYOUT_WEIGHT_WINS = 10;
	private static final int LAYOUT_WEIGHT_LOSSES = 10;
	private static final int LAYOUT_WEIGHT_WIN_LOSS_RATIO = 10;
	private static final int LAYOUT_WEIGHT_RATING = 20;

	// Member variables
	// ----------------------------------------

	private List<Player> players;
	private PlayerComparator comparator;
	private TableLayout layout;

	// Inner classes
	// ----------------------------------------

	private class PlayerComparator implements Comparator<Player> {

		private SortColumn column;
		private SortDirection direction;

		@Override
		public int compare(Player p1, Player p2) {
			if (column != null) {
				switch (column) {
					case GOALS:
						return compareInt(p1.getGoals(), p2.getGoals());
					case GOALS_AGAINST:
						return compareInt(p1.getGoalsAgainst(), p2.getGoalsAgainst());
					case WINS:
						return compareInt(p1.getWins(), p2.getWins());
					case LOSSES:
						return compareInt(p1.getLosses(), p2.getLosses());
					case WIN_LOSS_RATIO:
						return compareFloat(p1.winLossRatio(), p2.winLossRatio());
					case RATING:
						return compareInt(p1.getRating(), p2.getRating());
					case PLAYER:
					default:
						return compareString(p1.getName(), p2.getName());
				}
			} else {
				return 0;
			}
		}

		/**
		 * Compares two strings and returns the result.
		 * @param value1 String 1.
		 * @param value2 String 2.
		 * @return The result.
		 */
		private int compareString(String value1, String value2) {
			if (direction == SortDirection.ASCENDING) {
				return value1.compareTo(value2);
			} else {
				return value2.compareTo(value1);
			}
		}

		/**
		 * Compares two integers and returns the result.
		 * @param value1 Integer 1.
		 * @param value2 Integer 2.
		 * @return The result.
		 */
		private int compareInt(int value1, int value2) {
			return compareFloat(value1, value2);
		}

		/**
		 * Compares two floats and returns the result.
		 * @param value1 Float 1.
		 * @param value2 Float 2.
		 * @return The result.
		 */
		private int compareFloat(float value1, float value2) {
			if (direction == SortDirection.ASCENDING && (value1 < value2)
					|| direction == SortDirection.DESCENDING && (value2 < value1)) {
				return 1;
			} else if (direction == SortDirection.ASCENDING && (value1 > value2)
					|| direction == SortDirection.DESCENDING && (value2 > value1)) {
				return -1;
			} else {
				return 0;
			}
		}

		/**
		 * Changes the sorting order for the comparator.
		 * @param column Sort column type.
		 */
		public void sortColumn(SortColumn column) {
			if (direction != null && column == this.column) {
				direction = oppositeDirection();
			} else {
				direction = defaultDirection();
			}

			this.column = column;
		}

		/**
		 * Returns the opposite sorting direction to the current.
		 * @return The direction.
		 */
		private SortDirection oppositeDirection() {
			return direction == SortDirection.ASCENDING ? SortDirection.DESCENDING : SortDirection.ASCENDING;
		}

		/**
		 * Returns the default sorting direction.
		 * @return The direction.
		 */
		public SortDirection defaultDirection() {
			return SortDirection.ASCENDING;
		}
	}

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);


		Cursor cursor = getContentResolver().query(Player.CONTENT_URI, PlayerProvider.projectionArray, null, null, null);
		players = new ArrayList<Player>();

		if (cursor.moveToFirst()) {
			int i = 0;
			while (!cursor.isAfterLast()) {
				players.add(cursorToPlayer(cursor));
				cursor.moveToNext();
				i++;
			}
		}

		comparator = new PlayerComparator();

		layout = (TableLayout) findViewById(R.id.table_statistics);
		addTableHeaderRow(layout);
		addTablePlayerRows(layout);

		sortByColumn(SortColumn.PLAYER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.statistics_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_back:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Player.CONTENT_URI, PlayerProvider.projectionArray, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	private Player cursorToPlayer(Cursor cursor) {
		Player player = new Player();
		player.setId(cursor.getLong(0));
		player.setName(cursor.getString(4));
		player.setGoals(cursor.getInt(5));
		player.setGoalsAgainst(cursor.getInt(6));
		player.setWins(cursor.getInt(7));
		player.setLosses(cursor.getInt(8));
		player.setRating(cursor.getInt(9));
		return player;
	}

	/**
	 * Creates the table header row and adds it to the given table layout.
	 * @param layout The table layout.
	 */
	private void addTableHeaderRow(TableLayout layout) {
		TableRow row = new TableRow(this);

		TextView headerPlayer = createTableHeaderCell(getResources().getString(R.string.table_header_player), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT, 10);
		headerPlayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.PLAYER);
			}
		});
		row.addView(headerPlayer);

		TextView headerGoals = createTableHeaderCell(getResources().getString(R.string.table_header_goals), LAYOUT_WEIGHT_GOALS, Gravity.CENTER, 10);
		headerGoals.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS);
			}
		});
		row.addView(headerGoals);

		TextView headerGoalsAgainst = createTableHeaderCell(getResources().getString(R.string.table_header_goals_against), LAYOUT_WEIGHT_GOALS_AGAINST, Gravity.CENTER, 10);
		headerGoalsAgainst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS_AGAINST);
			}
		});
		row.addView(headerGoalsAgainst);

		TextView headerWins = createTableHeaderCell(getResources().getString(R.string.table_header_wins), LAYOUT_WEIGHT_WINS, Gravity.CENTER, 10);
		headerWins.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WINS);
			}
		});
		row.addView(headerWins);

		TextView headerLosses = createTableHeaderCell(getResources().getString(R.string.table_header_losses), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER, 10);
		headerLosses.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.LOSSES);
			}
		});
		row.addView(headerLosses);

		TextView headerWinLossRatio = createTableHeaderCell(getResources().getString(R.string.table_header_win_loss_ratio), LAYOUT_WEIGHT_WIN_LOSS_RATIO, Gravity.CENTER, 10);
		headerWinLossRatio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WIN_LOSS_RATIO);
			}
		});
		row.addView(headerWinLossRatio);

		TextView headerRating = createTableHeaderCell(getResources().getString(R.string.table_header_rating), LAYOUT_WEIGHT_RATING, Gravity.CENTER, 10);
		headerRating.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.RATING);
			}
		});
		row.addView(headerRating);

		layout.addView(row);
	}

	/**
	 * Creates the player rows and adds them to the given table layout.
	 * @param layout The table layout.
	 */
	private void addTablePlayerRows(TableLayout layout) {
		int playerCount = players.size();

		if (playerCount > 0) {
			for (int i = 0; i < playerCount; i++) {
				Player player = players.get(i);
				TableRow row = new TableRow(this);
				row.addView(createTableCell(player.getName(), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT, 10));
				row.addView(createTableCell(String.valueOf(player.getGoals()), LAYOUT_WEIGHT_GOALS, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getGoalsAgainst()), LAYOUT_WEIGHT_GOALS_AGAINST, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getWins()), LAYOUT_WEIGHT_WINS, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getLosses()), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER, 10));
				row.addView(createTableCell(String.format("%2.01f", player.winLossRatio()), LAYOUT_WEIGHT_WIN_LOSS_RATIO, Gravity.CENTER, 10));
				row.addView(createTableCell(String.valueOf(player.getRating()), LAYOUT_WEIGHT_RATING, Gravity.CENTER, 10));
				layout.addView(row);
			}
		}
	}

	/**
	 * Creates a single table header cell.
	 * @param text Cell text.
	 * @param weight Cell weight.
	 * @param gravity Cell gravity (think align).
	 * @param padding Cell padding.
	 * @return The cell.
	 */
	private TextView createTableHeaderCell(String text, float weight, int gravity, int padding) {
		TextView cell = createTableCell(text, weight, gravity, padding);
		cell.setTypeface(null, Typeface.BOLD);
		return cell;
	}

	/**
	 * Creates a single table cell.
	 * @param text Cell text.
	 * @param weight Cell weight.
	 * @param gravity Cell gravity (think align).
	 * @param padding Cell padding.
	 * @return The cell.
	 */
	private TextView createTableCell(String text, float weight, int gravity, int padding) {
		TextView cell = new TextView(this);
		cell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
		cell.setPadding(padding, padding, padding, padding);
		cell.setGravity(gravity);
		cell.setText(text);
		return cell;
	}

	/**
	 * Sorts the table by the given column.
	 * @param column Sort column type.
	 */
	private void sortByColumn(SortColumn column) {
		comparator.sortColumn(column);
		Collections.sort(players, comparator);
		layout.removeAllViews();
		addTableHeaderRow(layout);
		addTablePlayerRows(layout);
		layout.invalidate();
	}
}