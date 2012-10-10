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

	public static final String EXTRA_NAME_PLAYER1 = "org.cniska.foosball.android.EXTRA_NAME_PLAYER1";
	public static final String EXTRA_NAME_PLAYER2 = "org.cniska.foosball.android.EXTRA_NAME_PLAYER2";
	public static final String EXTRA_NAME_PLAYER3 = "org.cniska.foosball.android.EXTRA_NAME_PLAYER3";
	public static final String EXTRA_NAME_PLAYER4 = "org.cniska.foosball.android.EXTRA_NAME_PLAYER4";
	public static final String EXTRA_SCORES_TO_WIN = "org.cniska.foosball.android.EXTRA_SCORES_TO_WIN";

	// Member variables
	// ----------------------------------------

	private AutoCompleteTextView[] mEditTexts;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEditTexts = new AutoCompleteTextView[4];

		setContentView(R.layout.new_match);

		mEditTexts[0] = (AutoCompleteTextView) findViewById(R.id.edit_text_player1);
		mEditTexts[1] = (AutoCompleteTextView) findViewById(R.id.edit_text_player2);
		mEditTexts[2] = (AutoCompleteTextView) findViewById(R.id.edit_text_player3);
		mEditTexts[3] = (AutoCompleteTextView) findViewById(R.id.edit_text_player4);

		getLoaderManager().initLoader(0, null, this);

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
			intent.putExtra(EXTRA_NAME_PLAYER1, mEditTexts[0].getText().toString().trim());
			intent.putExtra(EXTRA_NAME_PLAYER2, mEditTexts[1].getText().toString().trim());
			intent.putExtra(EXTRA_NAME_PLAYER3, mEditTexts[2].getText().toString().trim());
			intent.putExtra(EXTRA_NAME_PLAYER4, mEditTexts[3].getText().toString().trim());
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
		String[] projection = { Player.NAME };
		return new CursorLoader(getApplicationContext(), Player.CONTENT_URI, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		String[] playerNames = new String[data.getCount()];
		if (data.moveToNext()) {
			int i = 0;
			while (!data.isAfterLast()) {
				playerNames[i] = data.getString(0);
				data.moveToNext();
				i++;
			}
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_dropdown_item, playerNames);

		for (int i = 0; i < 4; i++) {
			mEditTexts[i].setThreshold(1);
			mEditTexts[i].setAdapter(adapter);
		}
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