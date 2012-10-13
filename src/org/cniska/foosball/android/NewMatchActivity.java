package org.cniska.foosball.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This activity handles match creation.
 */
public class NewMatchActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Static variables
	// ----------------------------------------

	public static final String TAG = NewMatchActivity.class.getName();

	public static final String EXTRA_PLAYER_NAMES = "org.cniska.foosball.android.EXTRA_PLAYER_NAMES";
	public static final String EXTRA_NUM_GOALS_TO_WIN = "org.cniska.foosball.android.EXTRA_NUM_GOALS_TO_WIN";

	private static final int NUM_SUPPORTED_PLAYERS = 4;
	private static final int AUTO_COMPLETE_THRESHOLD = 1;

	private static String[] PROJECTION = {
		Player.NAME,
	};

	private static int[] editTextIds = new int[] {
		R.id.edit_text_player1,
		R.id.edit_text_player2,
		R.id.edit_text_player3,
		R.id.edit_text_player4
	};

	// Member variables
	// ----------------------------------------

	private AutoCompleteTextView[] mEditTexts = new AutoCompleteTextView[4];

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_match);

		// Collect the auto-complete views so that we can refer to them later.
		for (int i = 0; i < NUM_SUPPORTED_PLAYERS; i++) {
			mEditTexts[i] = (AutoCompleteTextView) findViewById(editTextIds[i]);
		}

		// Ask the loader manager to start loading our player names.
		getSupportLoaderManager().initLoader(0, null, this);

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.new_match, menu);
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
		return new CursorLoader(getApplicationContext(), Player.CONTENT_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Collect the player names for the auto-completion.
		String[] playerNames = new String[data.getCount()];
		if (data.moveToNext()) {
			int i = 0;
			while (!data.isAfterLast()) {
				playerNames[i] = data.getString(0);
				data.moveToNext();
				i++;
			}
		}

		// Create the auto-complete adapter.
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_dropdown_item, playerNames);

		// Bind auto-completion for the edit text views.
		for (int i = 0; i < NUM_SUPPORTED_PLAYERS; i++) {
			mEditTexts[i].setThreshold(AUTO_COMPLETE_THRESHOLD);
			mEditTexts[i].setAdapter(adapter);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Submits the form.
	 * @param view
	 */
	public void submitForm(View view) {
		// Resolve the radio value.
		RadioGroup scoresToWin = (RadioGroup) findViewById(R.id.radio_group_score_to_win);
		int checkedRadioId = scoresToWin.getCheckedRadioButtonId();
		RadioButton checkedRadio = (RadioButton) findViewById(checkedRadioId);

		// Make sure that there weren't any validation errors before continuing.
		if (validateForm()) {
			// Collect the selected player names to pass them with the intent.
			ArrayList<String> playerNames = new ArrayList<String>(4);
			for (int i = 0; i < NUM_SUPPORTED_PLAYERS; i++) {
				playerNames.add(mEditTexts[i].getText().toString().trim());
			}

			// Randomize teams if necessary. This can only be done if all four players are playing.
			if (!TextUtils.isEmpty(playerNames.get(2)) && !TextUtils.isEmpty(playerNames.get(3))) {
				CheckBox randomTeams = (CheckBox) findViewById(R.id.check_box_random_teams);
				if (randomTeams.isChecked()) {
					Random random = new Random(System.currentTimeMillis());
					Collections.shuffle(playerNames, random);
				}
			}

			Logger.info(TAG, "Sending intent to start PlayMatchActivity.");
			Intent intent = new Intent(this, PlayMatchActivity.class);
			intent.putStringArrayListExtra(EXTRA_PLAYER_NAMES, playerNames);
			intent.putExtra(EXTRA_NUM_GOALS_TO_WIN, Integer.parseInt(checkedRadio.getText().toString()));
			startActivity(intent);
		}
	}

	/**
	 * Validates the form.
	 * @return Whether the form is valid.
	 */
	private boolean validateForm() {
		// Make sure that player 1 is given.
		if (mEditTexts[0].getText().toString().isEmpty()) {
			mEditTexts[0].setError("Player 1 is required");
			return false;
		}

		// Make sure that player 2 is given.
		if (mEditTexts[1].getText().toString().isEmpty()) {
			mEditTexts[1].setError("Player 2 is required");
			return false;
		}

		return true;
	}
}