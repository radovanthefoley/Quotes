package sk.rama.quotes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import sk.rama.quotes.data.FullQuote;
import sk.rama.quotes.data.QuotesDbHelper;

import static sk.rama.quotes.data.QuotesContract.QuoteEntry;

public class QuoteActivity extends AppCompatActivity implements QuoteLoadTaskRetainFragment.QuoteTaskAware {

    public static final String TAG = "QuoteActivity";
    public static final String PREV_QUOTE = "PREV_QUOTE";
    public static final String ACTUAL_QUOTE = "ACTUAL_QUOTE";
    private static QuoteLoadTaskRetainFragment quoteLoader;
    private FullQuote actualFqd;
    private FullQuote prevFqd;
    private QuotesDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new QuotesDbHelper(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.store);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actualFqd == null) return;
                new AsyncTask<Void, Void, Long>() {
                    @Override
                    protected Long doInBackground(Void... params) {
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(QuoteEntry.COLUMN_NAME_AUTHOR, actualFqd.author);
                        values.put(QuoteEntry.COLUMN_NAME_TEXT, actualFqd.quote);
                        return db.insert(QuoteEntry.TABLE_NAME, null, values);
                    }
                }.execute();
            }
        });

        // handle prevQuote button
        if (savedInstanceState != null) {
            prevFqd = (FullQuote) savedInstanceState.getSerializable(PREV_QUOTE);
            actualFqd = (FullQuote) savedInstanceState.getSerializable(ACTUAL_QUOTE);
        }
        FloatingActionButton prevQuoteButton = (FloatingActionButton) findViewById(R.id.prev_quote);
        prevQuoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuoteActivity.this.actualFqd = QuoteActivity.this.prevFqd;
                QuoteActivity.this.prevFqd = null;
                QuoteActivity.this.renderQueue();
                QuoteActivity.this.handleButtonsVisibility();
            }
        });
        handleButtonsVisibility();

        quoteLoader = QuoteLoadTaskRetainFragment.findOrCreateQuoteLoadTaskRetainFragment(getFragmentManager());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_storage) {
            Intent storageIntent = new Intent(this, StorageActivity.class);
            startActivity(storageIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadNewQuote(View view) {
        Log.i(TAG, "loading new Quote");
        quoteLoader.loadAsync();
    }

    @Override
    public void onResponseReceived(String response) {
        Log.i(TAG, "response received: " + response + this);
        if (response == null) return;
        try {
            JSONObject object = new JSONObject(response);
            prevFqd = actualFqd;
            actualFqd = new FullQuote();
            actualFqd.quote = object.getString("quoteText");
            actualFqd.author = object.getString("quoteAuthor");
            actualFqd.url = object.getString("quoteLink");
        } catch (JSONException e) {
            Log.e(TAG, "json parsing error");
            e.printStackTrace();
            return;
        }
        handleButtonsVisibility();
        renderQueue();
    }

    private void renderQueue() {
        TextView text = (TextView) findViewById(R.id.quote_text);
        text.setText(actualFqd.quote + "\n\n- " + actualFqd.author + " -");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (prevFqd != null) outState.putSerializable(PREV_QUOTE, prevFqd);
        if (actualFqd != null) outState.putSerializable(ACTUAL_QUOTE, actualFqd);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    protected void handleButtonsVisibility() {
        FloatingActionButton storeQuoteButton = (FloatingActionButton) findViewById(R.id.store);
        if (actualFqd == null) {
            storeQuoteButton.setVisibility(View.INVISIBLE);
        } else {
            storeQuoteButton.setVisibility(View.VISIBLE);
        }
        FloatingActionButton prevQuoteButton = (FloatingActionButton) findViewById(R.id.prev_quote);
        if (prevFqd == null) {
            prevQuoteButton.setVisibility(View.INVISIBLE);
        } else {
            prevQuoteButton.setVisibility(View.VISIBLE);
        }
    }

}
