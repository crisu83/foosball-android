package org.cniska.foosball.android;

import android.accounts.*;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * This class is the application's main activity.
 */
public class MainActivity extends BaseActivity implements OnAccountsUpdateListener {

	public static final String TAG = "MainActivity";

	public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
	public static final String[] SERVICES = { "service_ah" };

	private AccountManager mAccountManager;
	
	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove the title bar and set app to full-screen mode.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mAccountManager = AccountManager.get(this);
		mAccountManager.addOnAccountsUpdatedListener(this, null, true);

		setContentView(R.layout.main);

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		final ArrayList<Account> googleAccounts = new ArrayList<Account>(accounts.length);

		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i].type.equals(ACCOUNT_TYPE_GOOGLE)) {
				googleAccounts.add(accounts[i]);
			}
		}

		mAccountManager.getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, SERVICES, new AccountManagerCallback<Account[]>() {
			@Override
			public void run(AccountManagerFuture<Account[]> future) {
				for (int i = 0, l = googleAccounts.size(); i < l; i++) {
					Account account = googleAccounts.get(i);
					if (ContentResolver.getIsSyncable(account, PlayerProvider.AUTHORITY) == 0) {
						ContentResolver.setIsSyncable(account, PlayerProvider.AUTHORITY, 1);
					}
				}
			}
		}, null);
	}

	/**
	 * Starts a new match.
	 * @param view
	 */
	public void startNewMatch(View view) {
		Logger.info(TAG, "Sending intent to start NewMatchActivity.");
		Intent intent = new Intent(this, NewMatchActivity.class);
		startActivity(intent);
	}

	/**
	 * Opens the playing statistics.
	 * @param view
	 */
	public void openStatistics(View view) {
		Logger.info(TAG, "Sending intent to start StatisticsActivity.");
		Intent intent = new Intent(this, StatisticsActivity.class);
		startActivity(intent);
	}

	/**
	 * Tests the REST service implementation.
	 * @param view
	 */
	public void testRest(View view) {
		Intent intent = new Intent(this, RESTServiceActivity.class);
		startActivity(intent);
	}
}
