package org.cniska.foosball.android;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.cniska.foosball.R;

/**
 * This activity handles match creation.
 */
public class NewMatchActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Static variables
	// ----------------------------------------

	public static final String TAG = NewMatchActivity.class.getName();

	private static final int PLAYER_LOADER = 0x01;

	public static final String EXTRA_NAME_PLAYER1 = "org.cniska.foosball.PLAYER1";
	public static final String EXTRA_NAME_PLAYER2 = "org.cniska.foosball.PLAYER2";
	public static final String EXTRA_NAME_PLAYER3 = "org.cniska.foosball.PLAYER3";
	public static final String EXTRA_NAME_PLAYER4 = "org.cniska.foosball.PLAYER4";
	public static final String EXTRA_SCORES_TO_WIN = "org.cniska.foosball.SCORES_TO_WIN";

	// Member variables
	// ----------------------------------------

	private AutoCompleteTextView mTextViewPlayer1;
	private AutoCompleteTextView mTextViewPlayer2;
	private AutoCompleteTextView mTextViewPlayer3;
	private AutoCompleteTextView mTextViewPlayer4;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_match);

		getLoaderManager().initLoader(PLAYER_LOADER, null, this);

		String[] projection = { Player.NAME };
		Cursor cursor = getContentResolver().query(Player.CONTENT_URI, projection, null, null, null);

		String[] playerNames = new String[cursor.getCount()];
		if (cursor.moveToNext()) {
			int i = 0;
			while (!cursor.isAfterLast()) {
				playerNames[i] = cursor.getString(0);
				cursor.moveToNext();
				i++;
			}
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_dropdown_item, playerNames);

		mTextViewPlayer1 = (AutoCompleteTextView) findViewById(R.id.edit_text_player1);
		mTextViewPlayer2 = (AutoCompleteTextView) findViewById(R.id.edit_text_player2);
		mTextViewPlayer3 = (AutoCompleteTextView) findViewById(R.id.edit_text_player3);
		mTextViewPlayer4 = (AutoCompleteTextView) findViewById(R.id.edit_text_player4);

		mTextViewPlayer1.setThreshold(1);
		mTextViewPlayer2.setThreshold(1);
		mTextViewPlayer3.setThreshold(1);
		mTextViewPlayer4.setThreshold(1);

		mTextViewPlayer1.setAdapter(adapter);
		mTextViewPlayer2.setAdapter(adapter);
		mTextViewPlayer3.setAdapter(adapter);
		mTextViewPlayer4.setAdapter(adapter);

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_new_match, menu);
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

	/**
	 * Starts the match.
	 * @param view
	 */
	public void start(View view) {
		// Resolve the radio value.
		RadioGroup scoresToWin = (RadioGroup) findViewById(R.id.radio_group_score_to_win);
		int checkedRadioId = scoresToWin.getCheckedRadioButtonId();
		RadioButton checkedRadio = (RadioButton) findViewById(checkedRadioId);

		// Make sure that there weren't any validation errors before continuing.
		if (validate()) {
			Logger.info(TAG, "Sending intent to start PlayMatchActivity.");
			Intent intent = new Intent(this, PlayMatchActivity.class);
			intent.putExtra(EXTRA_NAME_PLAYER1, mTextViewPlayer1.getText().toString().trim());
			intent.putExtra(EXTRA_NAME_PLAYER2, mTextViewPlayer2.getText().toString().trim());
			intent.putExtra(EXTRA_NAME_PLAYER3, mTextViewPlayer3.getText().toString().trim());
			intent.putExtra(EXTRA_NAME_PLAYER4, mTextViewPlayer4.getText().toString().trim());
			intent.putExtra(EXTRA_SCORES_TO_WIN, checkedRadio.getText().toString());
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		Logger.info(TAG, "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Logger.info(TAG, "Activity paused.");
		super.onPause();
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

	/**
	 * Validates the form.
	 * @return Whether the form is valid.
	 */
	private boolean validate() {
		// Make sure that player 1 is given.
		if (mTextViewPlayer1.getText().toString().isEmpty()) {
			mTextViewPlayer1.setError("Player 1 is required");
			return false;
		}

		// Make sure that player 2 is given.
		if (mTextViewPlayer2.getText().toString().isEmpty()) {
			mTextViewPlayer2.setError("Player 2 is required");
			return false;
		}

		return true;
	}
}