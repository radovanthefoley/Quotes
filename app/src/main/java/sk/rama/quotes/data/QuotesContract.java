package sk.rama.quotes.data;

import android.provider.BaseColumns;

/**
 * Created by Radovan Masaryk on 1/3/2017.
 * <p>
 * Data contract class
 */

public final class QuotesContract {
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + QuoteEntry.TABLE_NAME + " (" +
                    QuoteEntry._ID + " INTEGER PRIMARY KEY," +
                    QuoteEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    QuoteEntry.COLUMN_NAME_TEXT + " TEXT," +
                    QuoteEntry.COLUMN_NAME_URL + " TEXT)";
    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + QuoteEntry.TABLE_NAME;

    private QuotesContract() {
    }

    public static class QuoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "queue";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_URL = "url";
    }
}
