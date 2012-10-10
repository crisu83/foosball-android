package org.cniska.foosball;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public abstract class RESTResponderFragment extends Fragment {

	// Member variables
	// ----------------------------------------

	private ResultReceiver receiver;

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new fragment.
	 */
	public RESTResponderFragment() {
		this.receiver = new ResultReceiver(new Handler()) {
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
	public abstract void onRESTResult(int statusCode, String result);

	// Getters and setters
	// ----------------------------------------

	public ResultReceiver getReceiver() {
		return receiver;
	}
}
