package org.cniska.foosball.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class RatingWidgetService extends RemoteViewsService {

	private class RatingWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
		private Context mContext;
		private Cursor mCursor;

		public RatingWidgetFactory(Context context, Intent intent) {
			mContext = context;
		}

		@Override
		public void onCreate() {
		}

		@Override
		public void onDataSetChanged() {
			if (mCursor != null) {
				mCursor.close();
			}

			mCursor = mContext.getContentResolver().query(DataContract.Ratings.CONTENT_URI,
					new String[] { DataContract.Players.NAME, DataContract.Ratings.RATING },
					null, null, null);
		}

		@Override
		public void onDestroy() {
			if (mCursor != null) {
				mCursor.close();
			}
		}

		@Override
		public int getCount() {
			return mCursor.getCount();
		}

		@Override
		public RemoteViews getViewAt(int position) {
			String name = "Unknown";
			int rating = 0;

			if (mCursor.moveToPosition(position)) {
				final int nameIndex = mCursor.getColumnIndex(COLUMN_NAME);
				final int ratingIndex = mCursor.getColumnIndex(COLUMN_RATING);
				name = mCursor.getString(nameIndex);
				rating = mCursor.getInt(ratingIndex);
			}

			RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.rating_widget_item);
			view.setTextViewText(R.id.column_name, name);
			view.setTextViewText(R.id.column_rating, String.valueOf(rating));

			return view;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_RATING = "rating";

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new RatingWidgetFactory(this.getApplicationContext(), intent);
	}
}
