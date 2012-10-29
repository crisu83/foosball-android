package org.cniska.foosball.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class StatisticsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "StatisticsActivity";

	// Enumerables
	// ----------------------------------------

	public enum SortColumn {
		PLAYER,
		WINS,
		LOSSES,
		GAMES_PLAYED,
		WIN_LOSS_RATIO,
		GOALS_FOR,
		GOALS_AGAINST,
		RATING
	}

	public enum SortDirection {
		ASCENDING,
		DESCENDING
	}

	// Inner classes
	// ----------------------------------------

	private class DataComparator implements Comparator<Data> {

		private SortColumn mColumn;
		private SortDirection mDirection;

		@Override
		public int compare(Data d1, Data d2) {
			if (mColumn != null) {
				switch (mColumn) {
					case WINS:
						return compareInt(d1.wins, d2.wins);
					case LOSSES:
						return compareInt(d1.losses, d2.losses);
					case GAMES_PLAYED:
						return compareInt(d1.gamesPlayed, d2.gamesPlayed);
					case GOALS_FOR:
						return compareInt(d1.goalsFor, d2.goalsFor);
					case GOALS_AGAINST:
						return compareInt(d1.goalsAgainst, d2.goalsAgainst);
					case RATING:
						return compareInt(d1.rating, d2.rating);
					case PLAYER:
					default:
						return compareString(d1.name, d2.name);
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
			if (mDirection == SortDirection.ASCENDING) {
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
			if (mDirection == SortDirection.ASCENDING && (value1 < value2)
					|| mDirection == SortDirection.DESCENDING && (value2 < value1)) {
				return 1;
			} else if (mDirection == SortDirection.ASCENDING && (value1 > value2)
					|| mDirection == SortDirection.DESCENDING && (value2 > value1)) {
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
			if (mDirection != null && column == this.mColumn) {
				mDirection = oppositeDirection();
			} else {
				mDirection = defaultDirection();
			}

			this.mColumn = column;
		}

		/**
		 * Returns the opposite sorting direction to the current.
		 * @return The direction.
		 */
		private SortDirection oppositeDirection() {
			return mDirection == SortDirection.ASCENDING ? SortDirection.DESCENDING : SortDirection.ASCENDING;
		}

		/**
		 * Returns the default sorting direction.
		 * @return The direction.
		 */
		public SortDirection defaultDirection() {
			return SortDirection.ASCENDING;
		}
	}

	private static class PlayerAdapter extends SimpleAdapter {
		public PlayerAdapter(Context context, ArrayList<HashMap<String, String>> data) {
			super(context, data, R.layout.statistics_item,
					new String[] {
						COLUMN_NAME,
						COLUMN_WINS,
						COLUMN_LOSSES,
						COLUMN_GAMES_PLAYED,
						COLUMN_GOALS_FOR,
						COLUMN_GOALS_AGAINST,
						COLUMN_RATING
					},
					new int[] {
						R.id.column_name,
						R.id.column_wins,
						R.id.column_losses,
						R.id.column_games_played,
						R.id.column_goals_for,
						R.id.column_goals_against,
						R.id.column_rating
					});
		}
	}

	/**
	 * This class represents a single item in the list view.
	 */
	private class Data {
		public long id;
		public String name;
		public int wins;
		public int losses;
		public int gamesPlayed;
		public int goalsFor;
		public int goalsAgainst;
		public int rating;
	}

	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_WINS = "wins";
	private static final String COLUMN_LOSSES = "losses";
	private static final String COLUMN_GAMES_PLAYED = "games_played";
	private static final String COLUMN_GOALS_FOR = "goals_for";
	private static final String COLUMN_GOALS_AGAINST = "goals_against";
	private static final String COLUMN_RATING = "rating";

	// Member variables
	// ----------------------------------------

	private PlayerAdapter mAdapter;
	private ListView mListView;
	private ArrayList<Data> mData;
	private DataComparator mComparator = new DataComparator();

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHomeButtonEnabled(true);
		setActionBarTitle(R.string.title_statistics);
		setContentView(R.layout.statistics);
		mListView = (ListView) findViewById(R.id.statistics_list);
		TextView emptyView = (TextView) findViewById(R.id.statistics_list_empty);
		mListView.setEmptyView(emptyView);
		setHeaderClickListeners();
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.statistics, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		// todo: change to use sub-query and do everything in a single query.
		return new CursorLoader(getApplicationContext(), DataContract.Players.CONTENT_URI,
				new String[] { DataContract.Players._ID, DataContract.Players.NAME },
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		if (cursor != null) {
			ContentResolver contentResolver = getContentResolver();
			mData = new ArrayList<Data>(cursor.getCount());

			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					Data item = new Data();

					item.id = cursor.getLong(cursor.getColumnIndex(DataContract.Players._ID));
					item.name = cursor.getString(cursor.getColumnIndex(DataContract.Players.NAME));

					Cursor c = contentResolver.query(
							Uri.withAppendedPath(DataContract.Players.CONTENT_URI, item.id + "/stats"),
							new String[] {
								"COUNT(*) AS " + COLUMN_GAMES_PLAYED,
								"SUM(" + DataContract.Stats.GOALS_FOR + ") AS " + COLUMN_GOALS_FOR,
								"SUM(" + DataContract.Stats.GOALS_AGAINST + ") AS " + COLUMN_GOALS_AGAINST,
								"SUM(" + DataContract.Stats.SCORE + ") AS " + COLUMN_WINS
							}, null, null, null);

					if (c.moveToFirst()) {
						item.gamesPlayed = c.getInt(c.getColumnIndex(COLUMN_GAMES_PLAYED));
						item.goalsFor = c.getInt(c.getColumnIndex(COLUMN_GOALS_FOR));
						item.goalsAgainst = c.getInt(c.getColumnIndex(COLUMN_GOALS_AGAINST));
						item.wins = c.getInt(c.getColumnIndex(COLUMN_WINS));
						item.losses = item.gamesPlayed - item.wins;
						c.close();
					}

					c = contentResolver.query(
							Uri.withAppendedPath(DataContract.Players.CONTENT_URI, item.id + "/rating"),
							new String[] { DataContract.Ratings.RATING }, null, null, null);

					if (c.moveToFirst()) {
						item.rating = c.getInt(c.getColumnIndex(DataContract.Ratings.RATING));
						c.close();
					}

					mData.add(item);
					cursor.moveToNext();
				}
			}

			updateListView();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
	}

	/**
	 * Updates the list view by rebuilding the data for the adapter.
	 */
	private void updateListView() {
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

		for (int i = 0, len = mData.size(); i < len; i++) {
			Data item = mData.get(i);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(COLUMN_NAME, item.name);
			map.put(COLUMN_WINS, String.valueOf(item.wins));
			map.put(COLUMN_LOSSES, String.valueOf(item.losses));
			map.put(COLUMN_GAMES_PLAYED, String.valueOf(item.gamesPlayed));
			map.put(COLUMN_GOALS_FOR, String.valueOf(item.goalsFor));
			map.put(COLUMN_GOALS_AGAINST, String.valueOf(item.goalsAgainst));
			map.put(COLUMN_RATING, String.valueOf(item.rating));
			data.add(map);
		}

		mAdapter = new PlayerAdapter(this, data);
		mListView.setAdapter(mAdapter);
	}

	/**
	 * Sets click listeners for the table header cells.
	 */
	private void setHeaderClickListeners() {
		TextView headerPlayer = (TextView) findViewById(R.id.heading_name);
		headerPlayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.PLAYER);
			}
		});

		TextView headerWins = (TextView) findViewById(R.id.heading_wins);
		headerWins.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WINS);
			}
		});

		TextView headerLosses = (TextView) findViewById(R.id.heading_losses);
		headerLosses.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.LOSSES);
			}
		});

		TextView headerGamesPlayed = (TextView) findViewById(R.id.heading_games_played);
		headerGamesPlayed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GAMES_PLAYED);
			}
		});

		TextView headerGoalsFor = (TextView) findViewById(R.id.heading_goals_for);
		headerGoalsFor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS_FOR);
			}
		});

		TextView headerGoalsAgainst = (TextView) findViewById(R.id.heading_goals_against);
		headerGoalsAgainst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS_AGAINST);
			}
		});

		TextView headerRating = (TextView) findViewById(R.id.heading_rating);
		headerRating.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.RATING);
			}
		});
	}

	/**
	 * Sorts the list view by the given column.
	 * @param column Sort column type.
	 */
	private void sortByColumn(SortColumn column) {
		mComparator.sortColumn(column);
		Collections.sort(mData, mComparator);
		updateListView();
	}
}