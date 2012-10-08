package org.cniska.foosball;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsActivity extends Activity {

	private static final int LAYOUT_WEIGHT_PLAYER = 65;
	private static final int LAYOUT_WEIGHT_GOALS = 10;
	private static final int LAYOUT_WEIGHT_WINS = 10;
	private static final int LAYOUT_WEIGHT_LOSSES = 10;
	private static final int LAYOUT_WEIGHT_RATIO = 15;

	private SQLitePlayerDataSource data;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		data = new SQLitePlayerDataSource(this);
		data.open();

		// Find all players and sort them according to their ratio.
		List<Player> players = data.findAllPlayers();
		Collections.sort(players, new PlayerRatioComparator());

		TableLayout layout = (TableLayout) findViewById(R.id.table_statistics);

		TextView headerPlayer = (TextView) findViewById(R.id.table_header_player);
		headerPlayer.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, LAYOUT_WEIGHT_PLAYER));

		TextView headerGoals = (TextView) findViewById(R.id.table_header_goals);
		headerGoals.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, LAYOUT_WEIGHT_GOALS));

		TextView headerWins = (TextView) findViewById(R.id.table_header_wins);
		headerWins.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, LAYOUT_WEIGHT_WINS));

		TextView headerLosses = (TextView) findViewById(R.id.table_header_losses);
		headerLosses.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, LAYOUT_WEIGHT_LOSSES));

		TextView headerRatio = (TextView) findViewById(R.id.table_header_ratio);
		headerRatio.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, LAYOUT_WEIGHT_RATIO));

		int playerCount = players.size();

		if (playerCount > 0) {
			for (int i = 0; i < playerCount; i++) {
				Player player = players.get(i);
				TableRow row = new TableRow(this);
				layout.addView(row);
				row.addView(createColumn(player.getName(), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT));
				row.addView(createColumn(String.valueOf(player.getGoals()), LAYOUT_WEIGHT_GOALS, Gravity.CENTER));
				row.addView(createColumn(String.valueOf(player.getWins()), LAYOUT_WEIGHT_WINS, Gravity.CENTER));
				row.addView(createColumn(String.valueOf(player.getLosses()), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER));
				row.addView(createColumn(String.format("%2.01f", player.calcRatio()), LAYOUT_WEIGHT_RATIO, Gravity.CENTER));
			}
		}
	}

	@Override
	protected void onResume() {
		data.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		data.close();
		super.onPause();
	}

	private TextView createColumn() {
		TextView column = new TextView(this);
		column.setLayoutParams(new TableRow.LayoutParams(
			TableRow.LayoutParams.MATCH_PARENT,
			TableRow.LayoutParams.WRAP_CONTENT
		));
		return column;
	}

	private TextView createColumn(String text, float weight, int gravity) {
		TextView column = new TextView(this);
		column.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, weight));
		column.setPadding(10, 10, 10, 10);
		column.setGravity(gravity);
		column.setText(text);
		return column;
	}

	private class PlayerRatioComparator implements Comparator<Player> {

		@Override
		public int compare(Player p1, Player p2) {
			float r1 = p1.calcRatio();
			float r2 = p2.calcRatio();
			if (r1 < r2) {
				return 11;
			} else if (r1 > r2) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}