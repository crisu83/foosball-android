package org.cniska.foosball.android;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RESTService extends IntentService {

	// Static variables
	// ----------------------------------------

	public static final String TAG = "RESTService";

	// Http verbs
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	public static final int DELETE = 4;

	// Extras
	public static final String EXTRA_HTTP_VERB = "org.cniska.foosball.android.EXTRA_HTTP_VERB";
	public static final String EXTRA_PARAMS = "org.cniska.foosball.android.EXTRA_PARAMS";
	public static final String EXTRA_RESULT_RECEIVER = "org.cniska.foosball.android.EXTRA_RESULT_RECEIVER";

	public static final String REST_RESULT = "org.cniska.foosball.android.REST_RESULT";

	// Methods
	// ----------------------------------------

	/**
	 * Creates a new service.
	 */
	public RESTService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Uri action = intent.getData();
		Bundle extras = intent.getExtras();

		if (action == null || extras == null || !extras.containsKey(EXTRA_RESULT_RECEIVER)) {
			return;
		}

		int verb = extras.getInt(EXTRA_HTTP_VERB, GET);
		Bundle params = extras.getParcelable(EXTRA_PARAMS);
		ResultReceiver receiver = extras.getParcelable(EXTRA_RESULT_RECEIVER);

		try {
			HttpRequestBase request;

			switch (verb) {
				case POST:
					request = new HttpPost();
					request.setURI(new URI(action.toString()));
					if (params != null) {
						UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(params));
						((HttpPost) request).setEntity(formEntity);
					}
					break;

				case PUT:
					request = new HttpPut();
					request.setURI(new URI(action.toString()));
					if (params != null) {
						UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(params));
						((HttpPut) request).setEntity(formEntity);
					}
					break;

				case DELETE:
					request = new HttpDelete();
					setURIWithParams(request, action, params);
					break;

				case GET:
				default:
					request = new HttpGet();
					setURIWithParams(request, action, params);
					break;
			}

			Logger.debug(TAG, "Executing request " + verbToString(verb) + ":" + action.toString());

			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			StatusLine status = response.getStatusLine();
			int statusCode = status != null ? status.getStatusCode() : -1;

			if (entity != null) {
				Bundle data = new Bundle();
				data.putString(REST_RESULT, EntityUtils.toString(entity));
				receiver.send(statusCode, data);
			} else {
				receiver.send(statusCode, null);
			}
		} catch (URISyntaxException e) {
			Logger.error(TAG, "Failed to handle intent. (URI syntax is incorrect).");
			receiver.send(0, null);
		} catch (ClientProtocolException e) {
			Logger.error(TAG, "Failed to handle intent.");
			receiver.send(0, null);
		} catch (IOException e) {
			Logger.error(TAG, "Failed to handle intent.");
			receiver.send(0, null);
		}
	}

	/**
	 * Adds the query string to the given Http request.
	 * @param request Http request.
	 * @param uri Uri reference.
	 * @param params Query parameters.
	 * @throws URISyntaxException
	 */
	private void setURIWithParams(HttpRequestBase request, Uri uri, Bundle params) throws URISyntaxException {
		if (params != null) {
			Uri.Builder builder = uri.buildUpon();

			for (BasicNameValuePair param : paramsToList(params)) {
				builder.appendQueryParameter(param.getName(), param.getValue());
			}

			uri = builder.build();
		}
		request.setURI(new URI(uri.toString()));
	}

	/**
	 * Converts the given parameter bundle to a list.
	 * @param params Parameter bundle.
	 * @return The list.
	 */
	private List<BasicNameValuePair> paramsToList(Bundle params) {
		int numParams = params.size();
		ArrayList<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>(numParams);

		for (String key : params.keySet()) {
			Object value = params.get(key);
			if (value != null) {
				list.add(new BasicNameValuePair(key, value.toString()));
			}
		}

		return list;
	}

	/**
	 * Returns the given HTTP verb constant as a string.
	 * @param verb Verb constant.
	 * @return The verb.
	 */
	private static String verbToString(int verb) {
		switch (verb) {
			case GET:		return "GET";
			case POST:		return "POST";
			case PUT:		return "PUT";
			case DELETE:	return "DELETE";
			default:		return null;
		}
	}
}
