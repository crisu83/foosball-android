package org.cniska.foosball.android;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import org.cniska.foosball.R;

public class StatisticsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int PLAYER_LOADER = 0x01;

	private CursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] bindFrom = { Player.NAME, Player.GOALS, Player.GOALS_AGAINST, Player.WINS, Player.LOSSES, Player.RATING };
		int[] bindTo = new int[] { R.id.column_name, R.id.column_goals, R.id.column_goals_against, R.id.column_wins, R.id.column_losses, R.id.column_rating };
		getLoaderManager().initLoader(PLAYER_LOADER, null, this);
		adapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.statistics_item,
				null,
				bindFrom,
				bindTo,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		setListAdapter(adapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { Player._ID, Player.NAME, Player.GOALS, Player.GOALS_AGAINST, Player.WINS, Player.LOSSES, Player.RATING };
		return new CursorLoader(getActivity(), Player.CONTENT_URI, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
