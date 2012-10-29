package org.cniska.foosball.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

import java.util.*;

/**
 * This activity handles match creation.
 */
public class NewMatchActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Static variables
	// ----------------------------------------

	public static final String TAG = "NewMatchActivity";

	public static final String EXTRA_MATCH = "org.cniska.foosball.android.EXTRA_MATCH";

	private static final int AUTO_COMPLETE_THRESHOLD = 1;

	private static final int EDIT_TEXT_PLAYER_1 = 0;
	private static final int EDIT_TEXT_PLAYER_2 = 1;
	private static final int EDIT_TEXT_PLAYER_3 = 2;
	private static final int EDIT_TEXT_PLAYER_4 = 3;

	private static String[] PLAYER_PROJECTION = {
		DataContract.Players._ID,
		DataContract.Players.NAME
	};

	private static int[] sEditTextIds = new int[] {
		R.id.edit_text_player1,
		R.id.edit_text_player2,
		R.id.edit_text_player3,
		R.id.edit_text_player4
	};

	// Member variables
	// ----------------------------------------

	private AutoCompleteTextView[] mEditTexts = new AutoCompleteTextView[RawMatch.NUM_SUPPORTED_PLAYERS];
	private Map<String, Long> mNameIdMap = new HashMap<String, Long>(RawMatch.NUM_SUPPORTED_PLAYERS);

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHomeButtonEnabled(true);
		setActionBarTitle(R.string.title_new_match);
		setContentView(R.layout.new_match);

		// Collect the auto-complete views so that we can refer to them later.
		for (int i = 0; i < RawMatch.NUM_SUPPORTED_PLAYERS; i++) {
			mEditTexts[i] = (AutoCompleteTextView) findViewById(sEditTextIds[i]);
		}

		// Ask the loader manager to create a loader for loading the players.
		getSupportLoaderManager().initLoader(0, null, this);
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
			case R.id.menu_start:
				submitForm();
				return true;

			case R.id.menu_balance_teams:
				// todo: implement.
				return true;

			case R.id.menu_shuffle_teams:
				shuffleTeams();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getApplicationContext(), DataContract.Players.CONTENT_URI, PLAYER_PROJECTION,
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.moveToNext()) {
			// Collect the player names for the auto-completion.
			ArrayList<String> playerNames = new ArrayList<String>(data.getCount());

			while (!data.isAfterLast()) {
                long id = data.getLong(data.getColumnIndex(DataContract.Players._ID));
				String name = data.getString(data.getColumnIndex(DataContract.Players.NAME));
				playerNames.add(name);
				mNameIdMap.put(name, id);
				data.moveToNext();
			}

			// Create the auto-complete adapter.
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_dropdown_item, playerNames);

			// Bind auto-completion for the edit text views.
			for (int i = 0; i < mEditTexts.length; i++) {
				mEditTexts[i].setThreshold(AUTO_COMPLETE_THRESHOLD);
				mEditTexts[i].setAdapter(adapter);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	private void shuffleTeams() {
		ArrayList<String> playerNames = fetchPlayerNames();
		if (isAllNamesEntered(playerNames)) {
			Random random = new Random(System.currentTimeMillis());
			Collections.shuffle(playerNames, random);
			updatePlayerNames(playerNames);
		}
	}

	/**
	 * Submits the form.
	 */
	public void submitForm() {
		// Make sure that there weren't any validation errors before continuing.
		if (validateForm()) {
            String[] data = new String[RawMatch.NUM_SUPPORTED_PLAYERS];
			ArrayList<Long> playerIdList = new ArrayList<Long>();

            for (int i = 0; i < RawMatch.NUM_SUPPORTED_PLAYERS; i++) {
				String name = mEditTexts[i].getText().toString().trim();

				data[i] = name;

				// Makes sure that the edit text is filled out before doing anything with it.
				if (!TextUtils.isEmpty(name)) {
					if (!mNameIdMap.containsKey(name)) {
						// Each player name that isn't in the map is a new player and needs to be created.
						ContentValues values = new ContentValues();
						values.put(DataContract.Players.NAME, name);
						RawPlayer player = createPlayer(values);
						mNameIdMap.put(name, player.getId());

						ContentValues ratingValues = new ContentValues();
						ratingValues.put(DataContract.Ratings.PLAYER_ID, player.getId());
						ratingValues.put(DataContract.Ratings.RATING, EloRatingSystem.INITIAL_RATING);
						getContentResolver().insert(DataContract.Ratings.CONTENT_URI, ratingValues);
					}

					playerIdList.add(mNameIdMap.get(name));
				}
            }

			// Player setup can only be change if all four players are playing.
			/*
			if (!TextUtils.isEmpty(data[EDIT_TEXT_PLAYER_3]) && !TextUtils.isEmpty(data[EDIT_TEXT_PLAYER_4])) {
				RadioGroup playerSetup = (RadioGroup) findViewById(R.id.radio_group_player_positions);
				int checkedPlayerSetupRadioId = playerSetup.getCheckedRadioButtonId();

				switch (checkedPlayerSetupRadioId) {
					case R.id.radio_player_positions_balance:
						// todo: implement.
						break;

					case R.id.radio_player_positions_random:
						Random random = new Random(System.currentTimeMillis());
						Collections.shuffle(playerIdList, random);
						break;

					case R.id.radio_player_positions_current:
					default:
						break;
				}
			}
			*/

			RadioGroup scoresToWin = (RadioGroup) findViewById(R.id.radio_group_score_to_win);
			int checkedScoreToWinRadioId = scoresToWin.getCheckedRadioButtonId();
			RadioButton scoreToWinRadio = (RadioButton) findViewById(checkedScoreToWinRadioId);
            int numGoalsToWin = Integer.parseInt(scoreToWinRadio.getText().toString());

			CheckBox rankedCheckBox = (CheckBox) findViewById(R.id.check_box_match_ranked);
			boolean ranked = rankedCheckBox.isChecked();

			int numPlayers = playerIdList.size();
			long[] playerIds = new long[RawMatch.NUM_SUPPORTED_PLAYERS];
			for (int i = 0; i < playerIds.length; i++) {
				playerIds[i] = i < numPlayers ? playerIdList.get(i) : 0;
			}

            RawMatch match = new RawMatch();
            match.setNumGoalsToWin(numGoalsToWin);
			match.setPlayerIds(playerIds);
			match.setRanked(ranked);

			Logger.info(TAG, "Sending intent to start PlayMatchActivity.");
			Intent intent = new Intent(this, PlayMatchActivity.class);
			intent.putExtra(EXTRA_MATCH, match);
			startActivity(intent);
		}
	}

	private void updatePlayerNames(ArrayList<String> names) {
		for (int i = 0; i < mEditTexts.length; i++) {
			mEditTexts[i].setText(names.get(i));
		}
	}

	private ArrayList<String> fetchPlayerNames() {
		ArrayList<String> names = new ArrayList<String>(mEditTexts.length);
		for (int i = 0; i < mEditTexts.length; i++) {
			names.add(mEditTexts[i].getText().toString().trim());
		}
		return names;
	}

	private boolean isAllNamesEntered(ArrayList<String> names) {
		return !TextUtils.isEmpty(names.get(EDIT_TEXT_PLAYER_3)) && !TextUtils.isEmpty(names.get(EDIT_TEXT_PLAYER_4));
	}

	/**
	 * Creates a new player with the given values.
	 * @param values Content values.
	 * @return The player.
	 */
	private RawPlayer createPlayer(ContentValues values) {
		RawPlayer player = null;

		ContentResolver contentResolver = getContentResolver();
		Uri uri = contentResolver.insert(DataContract.Players.CONTENT_URI, values);
		Cursor cursor = contentResolver.query(uri, PLAYER_PROJECTION, null, null, null);

		if (cursor.moveToFirst()) {
			player = new RawPlayer(cursor);
		}

		return player;
	}

	/**
	 * Validates the form.
	 * @return Whether the form is valid.
	 */
	private boolean validateForm() {
		// Make sure that player 1 is given.
		if (TextUtils.isEmpty(mEditTexts[0].getText())) {
			mEditTexts[0].setError("Player 1 is required");
			return false;
		}

		// Make sure that player 2 is given.
		if (TextUtils.isEmpty(mEditTexts[1].getText())) {
			mEditTexts[1].setError("Player 2 is required");
			return false;
		}

		return true;
	}
}