package org.cniska.foosball.android;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This activity allows for testing the REST service by connecting to a dummy script on a webserver.
 */
public class RESTServiceActivity extends Activity {

	// Static variables
	// ----------------------------------------

	public static final String TAG = "RESTServiceActivity";
	public static final String TAG_RESPONDER = "RESTResponderFragment";

	private static final String SERVER_URI = "http://www.cniska.net/foosball/test.php";

	// Methods
	// ----------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.rest_service);

		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		RESTResponderFragment responder = (RESTResponderFragment) manager.findFragmentByTag(TAG_RESPONDER);

		if (responder == null) {
			responder = new RESTResponderFragment() {

				public static final String KEY_GREETING = "greeting";

				@Override
				public void onActivityCreated(Bundle savedInstanceState) {
					super.onActivityCreated(savedInstanceState);
					sendRequest();
				}

				@Override
				public void onRESTResult(int statusCode, String result) {
					if (statusCode == HttpStatus.SC_OK && result != null) {
						Activity activity = getActivity();
						//LinearLayout layout = (LinearLayout) activity.findViewById(R.id.rest_service);
						TextView view = new TextView(activity);
						view.setText(parseResult(result));
						//layout.addView(view);
					}
				}

				private void sendRequest() {
					Activity activity = getActivity();

					if (activity != null) {
						Bundle params = new Bundle();
						Intent intent = new Intent(activity, RESTService.class);
						intent.setData(Uri.parse(SERVER_URI));
						intent.putExtra(RESTService.EXTRA_PARAMS, params);
						intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, getReceiver());
						activity.startService(intent);
					}
				}

				private String parseResult(String jsonString) {
					String result = null;

					try {
						JSONObject object = new JSONObject(jsonString);
						result = object.has(KEY_GREETING) ? object.getString(KEY_GREETING) : "";
					} catch (JSONException e) {
						e.printStackTrace();
					}

					return result;
				}
			};

			transaction.add(responder, TAG_RESPONDER);
		}

		transaction.commit();
	}
}
