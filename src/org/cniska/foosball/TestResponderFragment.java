package org.cniska.foosball;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class TestResponderFragment extends RESTResponderFragment {

	public static final String KEY_GREETING = "greeting";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sendRequest();
	}

	private void sendRequest() {
		RESTServiceActivity activity = (RESTServiceActivity) getActivity();

		if (activity != null) {
			Bundle params = new Bundle();
			Intent intent = new Intent(activity, RESTService.class);
			intent.setData(Uri.parse("http://www.cniska.net/foosball/test.php"));
			intent.putExtra(RESTService.EXTRA_PARAMS, params);
			intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, getReceiver());
			activity.startService(intent);
		}
	}

	@Override
	public void onRESTResult(int statusCode, String result) {
		if (statusCode == HttpStatus.SC_OK && result != null) {
			Activity activity = getActivity();
			LinearLayout layout = (LinearLayout) activity.findViewById(R.id.rest_service);
			TextView view = new TextView(activity);
			view.setText(parseResult(result));
			layout.addView(view);
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
}
