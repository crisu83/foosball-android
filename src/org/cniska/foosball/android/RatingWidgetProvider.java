package org.cniska.foosball.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class RatingWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int i = 0; i < appWidgetIds.length; i++) {
			Intent serviceIntent = new Intent(context, RatingWidgetService.class);
			serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.rating_widget);
			views.setRemoteAdapter(R.id.rating_widget_list, serviceIntent);
			views.setEmptyView(R.id.rating_widget_list, R.id.rating_widget_list_empty);

			Intent clickIntent = new Intent(context, StatisticsActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.layout, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
