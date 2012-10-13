package org.cniska.foosball.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service instantiates a sync adapter and returns its binder.
 */
public class SyncService extends Service {

	// Static variables
	// ----------------------------------------

	private static SyncAdapter sSyncAdapter = null;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();

		if (sSyncAdapter == null) {
			sSyncAdapter = new SyncAdapter(getApplicationContext(), true /* autoInitialize */);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sSyncAdapter.getSyncAdapterBinder();
	}
}
