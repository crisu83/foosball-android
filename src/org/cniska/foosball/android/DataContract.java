package org.cniska.foosball.android;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the {@link DataProvider} and its clients.
 */
public final class DataContract {

	public static final String AUTHORITY = "org.cniska.foosball.android.DataContract";

	private DataContract() {
	}

	/**
	 * Audit columns which track audit data for database records.
	 */
	public interface AuditColumns {
		public static final String CREATED = "created";
	}

	/**
	 * Columns for {@link DataContract.Matches} that tracks match related data.
	 */
	public interface MatchColumns {
		public static final String DURATION = "duration";
		public static final String RANKED = "ranked";
		public static final String SCORE_TO_WIN = "score_to_win";
	}

	/**
	 * Matches table contract.
	 */
	public static final class Matches implements BaseColumns, AuditColumns, MatchColumns {
		public static final String TABLE_NAME = "match";

		private static final String SCHEME = "content://";
		public static final String CONTENT_PATH = "matches";

		public static final int ID_PATH_POSITION = 1;

		private static final Uri BASE_URI = Uri.parse(SCHEME + AUTHORITY);
		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, CONTENT_PATH);

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cniska.match";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cniska.match";

		public static final String DEFAULT_SORT_ORDER = "created DESC";
	}

	/**
	 * Columns for {@link DataContract.Players} that tracks player related data.
	 */
	public interface PlayerColumns {
		public static final String NAME = "name";
	}

	/**
	 * Players table contract.
	 */
	public static final class Players implements BaseColumns, AuditColumns, PlayerColumns {
		public static final String TABLE_NAME = "player";

		private static final String SCHEME = "content://";
		public static final String CONTENT_PATH = "players";

		public static final int ID_PATH_POSITION = 1;

		private static final Uri BASE_URI = Uri.parse(SCHEME + AUTHORITY);
		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, CONTENT_PATH);

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cniska.player";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cniska.player";

		public static final String DEFAULT_SORT_ORDER = "name ASC";
	}

	/**
	 * Columns for {@link DataContract.Stats} that tracks goal data.
	 */
	public interface StatsColumns {
		public static final String MATCH_ID			= "match_id";
		public static final String PLAYER_ID		= "player_id";
		public static final String GOALS_FOR		= "goals_for";
		public static final String GOALS_AGAINST	= "goals_against";
		public static final String SCORE			= "score";
		public static final String TEAM				= "team";
	}

	/**
	 * Stats table contract.
	 */
	public static final class Stats implements BaseColumns, AuditColumns, StatsColumns {
		public static final String TABLE_NAME = "stats";

		private static final String SCHEME = "content://";
		public static final String CONTENT_PATH = "stats";

		public static final int ID_PATH_POSITION = 1;

		private static final Uri BASE_URI = Uri.parse(SCHEME + AUTHORITY);
		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, CONTENT_PATH);

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cniska.stats";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cniska.stats";

		public static final String DEFAULT_SORT_ORDER = "_id ASC";
	}

	/**
	 * Columns for {@link DataContract.Ratings} that tracks player ratings.
	 */
	public interface RatingColumns {
		public static final String PLAYER_ID		= "player_id";
		public static final String RATING			= "rating";
	}

	/**
	 * Ratings table contract.
	 */
	public static final class Ratings implements BaseColumns, AuditColumns, RatingColumns {
		public static final String TABLE_NAME = "rating";

		private static final String SCHEME = "content://";
		public static final String CONTENT_PATH = "ratings";

		private static final Uri BASE_URI = Uri.parse(SCHEME + AUTHORITY);
		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, CONTENT_PATH);

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cniska.rating";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cniska.rating";

		public static final String DEFAULT_SORT_ORDER = "created DESC";
	}
}
