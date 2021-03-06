package sk.rama.quotes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

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

        // store quote button
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

                    @Override
                    protected void onPostExecute(Long id) {
                        super.onPostExecute(id);
                        Log.i(TAG, "Quote stored in db with id: " + id);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(QuoteActivity.this,
                                        "Quote saved as favorite", Toast.LENGTH_SHORT).show();
                            }
                        });
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

        // quote loader fragment
        quoteLoader = QuoteLoadTaskRetainFragment.findOrCreateQuoteLoadTaskRetainFragment(getFragmentManager());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // storage action starter
        if (id == R.id.action_storage) {
            Intent storageIntent = new Intent(this, StorageActivity.class);
            startActivity(storageIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    // called as onClick from quote_text text view
    public void loadNewQuote(View view) {
        Log.i(TAG, "loading new Quote");
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Log.i(TAG, "no internet connection");
            Toast.makeText(this, "No internet connection detected", Toast.LENGTH_LONG).show();
            return;
        }
        quoteLoader.loadAsync();
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

    private void renderQueue() {
        TextView text = (TextView) findViewById(R.id.quote_text);
        text.setText(actualFqd.quote + "\n\n- " + actualFqd.author + " -");
    }

}
