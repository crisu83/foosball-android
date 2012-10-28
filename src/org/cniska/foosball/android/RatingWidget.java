package org.cniska.foosball.android;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class RatingWidget extends AppWidgetProvider {

	public static class UpdateService extends Service {
		private static final String TAG = "RatingWidget.UpdateService";

		private static class PlayerAdapter extends SimpleAdapter {
			public PlayerAdapter(Context context, ArrayList<HashMap<String, String>> data) {
				super(context, data, R.layout.rating_widget_item,
						new String[] { COLUMN_PLAYER_ID, COLUMN_RATING },
						new int[] { R.id.column_name, R.id.column_rating });
			}
		}

		private class Data {
			public String name;
			public int rating;
			public long playerId;
		}

		private static final String COLUMN_NAME = "name";
		private static final String COLUMN_RATING = "rating";
		private static final String COLUMN_PLAYER_ID = "player_id";

		private static final int NUM_PLAYERS = 1;

		private PlayerAdapter mAdapter;
		private Data mData;

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			handleCommand(intent);
			return START_STICKY;
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null; // we do not need to bind this service.
		}

		private void handleCommand(Intent intent) {
			Context appContext = getApplicationContext();

			loadData(appContext);

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
			int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

			for (int i = 0; i < allWidgetIds.length; i++) {
				Intent clickIntent = new Intent(appContext, StatisticsActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, clickIntent, 0);

				RemoteViews remoteViews = new RemoteViews(appContext.getPackageName(), R.layout.rating_widget);
				remoteViews.setTextViewText(R.id.column_name, String.valueOf(mData.name));
				remoteViews.setTextViewText(R.id.column_rating, String.valueOf(mData.rating));
				remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);

				appWidgetManager.updateAppWidget(allWidgetIds[i], remoteViews);
			}

			stopSelf();
		}

		private void loadData(Context context) {
			mData = new Data();
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor;

			cursor = contentResolver.query(
					Uri.withAppendedPath(DataContract.Ratings.CONTENT_URI, "limit/" + NUM_PLAYERS),
					new String[]{DataContract.Ratings.PLAYER_ID, DataContract.Ratings.RATING},
					null, null, "rating DESC");

			if (cursor.moveToFirst()) {
				mData.rating = cursor.getInt(cursor.getColumnIndex(COLUMN_RATING));
				mData.playerId = cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYER_ID));
				cursor.close();
			}

			cursor = contentResolver.query(
					Uri.withAppendedPath(DataContract.Players.CONTENT_URI, String.valueOf(mData.playerId)),
					new String[] { DataContract.Players.NAME }, null, null, null);

			if (cursor.moveToFirst()) {
				mData.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
				cursor.close();
			}
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		ComponentName widget = new ComponentName(context, RatingWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widget);
		Intent intent = new Intent(context.getApplicationContext(), UpdateService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		context.startService(intent);
	}
}
