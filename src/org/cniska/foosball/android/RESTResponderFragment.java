package org.cniska.foosball.android;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * This fragment allows for sending and receiving REST calls.
 */
public class RESTResponderFragment extends Fragment {

	// Member variables
	// ----------------------------------------

	private ResultReceiver mReceiver;

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new fragment.
	 */
	public RESTResponderFragment() {
		this.mReceiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultData != null && resultData.containsKey(RESTService.REST_RESULT)) {
					onRESTResult(resultCode, resultData.getString(RESTService.REST_RESULT));
				} else {
					onRESTResult(resultCode, null);
				}

			}
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	/**
	 * Called when a result is received.
	 * @param statusCode Http status code.
	 * @param result Result string.
	 */
	public void onRESTResult(int statusCode, String result) {
	}

	// Getters and setters
	// ----------------------------------------

	public ResultReceiver getReceiver() {
		return mReceiver;
	}
}
