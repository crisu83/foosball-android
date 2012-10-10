package org.cniska.foosball;

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

	public static final String TAG = RESTService.class.getName();

	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	public static final int DELETE = 4;

	public static final String EXTRA_HTTP_VERB = "org.cniska.foosball.EXTRA_HTTP_VERB";
	public static final String EXTRA_PARAMS = "org.cniska.foosball.EXTRA_PARAMS";
	public static final String EXTRA_RESULT_RECEIVER = "org.cniska.foosball.EXTRA_RESULT_RECEIVER";

	public static final String REST_RESULT = "org.cniska.foosball.REST_RESULT";

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
					attachUriWithQuery(request, action, params);
					break;

				case GET:
				default:
					request = new HttpGet();
					attachUriWithQuery(request, action, params);
					break;
			}

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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) throws URISyntaxException {
		if (params != null) {
			Uri.Builder builder = uri.buildUpon();

			for (BasicNameValuePair param : paramsToList(params)) {
				builder.appendQueryParameter(param.getName(), param.getValue());
			}

			uri = builder.build();
		}
		request.setURI(new URI(uri.toString()));
	}

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

	private static String verbToString(int verb) {
		switch (verb) {
			case GET:
				return "GET";

			case POST:
				return "POST";

			case PUT:
				return "PUT";

			case DELETE:
				return "DELETE";

			default:
				return null;
		}
	}
}
