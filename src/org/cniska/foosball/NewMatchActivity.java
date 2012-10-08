package org.cniska.foosball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.List;

public class NewMatchActivity extends Activity {

	// Static variables
	// ----------------------------------------

	public static final String EXTRA_PLAYER1 = "org.cniska.foosball.PLAYER1";
	public static final String EXTRA_PLAYER2 = "org.cniska.foosball.PLAYER2";
	public static final String EXTRA_PLAYER3 = "org.cniska.foosball.PLAYER3";
	public static final String EXTRA_PLAYER4 = "org.cniska.foosball.PLAYER4";
	public static final String EXTRA_SCORES_TO_WIN = "org.cniska.foosball.SCORES_TO_WIN";

	// Member variables
	// ----------------------------------------

	private SQLitePlayerDataSource data;

	private AutoCompleteTextView fieldPlayer1;
	private AutoCompleteTextView fieldPlayer2;
	private AutoCompleteTextView fieldPlayer3;
	private AutoCompleteTextView fieldPlayer4;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_match);

		data = new SQLitePlayerDataSource(this);
		data.open();

		List<Player> players = data.findAllPlayers();
		int numPlayers = players.size();

		String[] playerNames = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			playerNames[i] = players.get(i).getName();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, playerNames);

		fieldPlayer1 = (AutoCompleteTextView) findViewById(R.id.field_player1);
		fieldPlayer2 = (AutoCompleteTextView) findViewById(R.id.field_player2);
		fieldPlayer3 = (AutoCompleteTextView) findViewById(R.id.field_player3);
		fieldPlayer4 = (AutoCompleteTextView) findViewById(R.id.field_player4);

		fieldPlayer1.setThreshold(1);
		fieldPlayer2.setThreshold(1);
		fieldPlayer3.setThreshold(1);
		fieldPlayer4.setThreshold(1);

		fieldPlayer1.setAdapter(adapter);
		fieldPlayer2.setAdapter(adapter);
		fieldPlayer3.setAdapter(adapter);
		fieldPlayer4.setAdapter(adapter);

		Logger.info(getClass().getName(), "Activity created.");
	}

	/**
	 * Starts the match.
	 * @param view
	 */
	public void start(View view) {
		// Resolve the radio value.
		RadioGroup scoresToWin = (RadioGroup) findViewById(R.id.score_to_win);
		int checkedRadioId = scoresToWin.getCheckedRadioButtonId();
		RadioButton checkedRadio = (RadioButton) findViewById(checkedRadioId);

		// Make sure that there weren't any validation errors before continuing.
		if (validate()) {
			Logger.info(getClass().getName(), "Sending intent to start PlayMatchActivity.");
			Intent intent = new Intent(this, PlayMatchActivity.class);
			intent.putExtra(EXTRA_PLAYER1, fieldPlayer1.getText().toString().trim());
			intent.putExtra(EXTRA_PLAYER2, fieldPlayer2.getText().toString().trim());
			intent.putExtra(EXTRA_PLAYER3, fieldPlayer3.getText().toString().trim());
			intent.putExtra(EXTRA_PLAYER4, fieldPlayer4.getText().toString().trim());
			intent.putExtra(EXTRA_SCORES_TO_WIN, checkedRadio.getText().toString());
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		data.open();
		Logger.info(getClass().getName(), "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		data.close();
		Logger.info(getClass().getName(), "Activity paused.");
		super.onPause();
	}

	/**
	 * Validates the form.
	 * @return Whether the form is valid.
	 */
	private boolean validate() {
		// Make sure that player 1 is given.
		if (fieldPlayer1.getText().toString().isEmpty()) {
			fieldPlayer1.setError("Player 1 is required");
			return false;
		}

		// Make sure that player 2 is given.
		if (fieldPlayer2.getText().toString().isEmpty()) {
			fieldPlayer2.setError("Player 2 is required");
			return false;
		}

		return true;
	}
}