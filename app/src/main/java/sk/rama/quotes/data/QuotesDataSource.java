package sk.rama.quotes.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import static sk.rama.quotes.data.QuotesContract.QuoteEntry;

/**
 * Created by Radovan Masaryk on 1/3/2017.
 * <p>
 * data abstraction object
 */

public class QuotesDataSource {
    private SQLiteDatabase database;
    private QuotesDbHelper mDbHelper;
    private String[] allColumns = {QuoteEntry.COLUMN_NAME_ID, QuoteEntry.COLUMN_NAME_AUTHOR,
            QuoteEntry.COLUMN_NAME_TEXT, QuoteEntry.COLUMN_NAME_URL};

    public QuotesDataSource(Context context) {
        mDbHelper = new QuotesDbHelper(context);
    }

    public void open() {
        database = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public List<FullQuote> getAllQuotes() {
        List<FullQuote> comments = new ArrayList<FullQuote>();

        Cursor cursor = database.query(QuoteEntry.TABLE_NAME,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FullQuote quote = cursorToComment(cursor);
            comments.add(quote);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    public void deleteQuote(FullQuote quote) {
        database.delete(QuoteEntry.TABLE_NAME, QuoteEntry.COLUMN_NAME_ID
                + " = " + quote.id, null);
    }

    private FullQuote cursorToComment(Cursor cursor) {
        FullQuote quote = new FullQuote();
        quote.id = cursor.getLong(0);
        quote.author = cursor.getString(1);
        quote.quote = cursor.getString(2);
        quote.url = cursor.getString(3);
        return quote;
    }
}
