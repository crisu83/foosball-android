package org.cniska.foosball;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class RESTServiceActivity extends Activity {

	public static final String TAG_RESPONDER = RESTResponderFragment.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rest_service);

		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		TestResponderFragment responder = (TestResponderFragment) manager.findFragmentByTag(TAG_RESPONDER);

		if (responder == null) {
			responder = new TestResponderFragment();
			transaction.add(responder, TAG_RESPONDER);
		}

		transaction.commit();
	}
}
