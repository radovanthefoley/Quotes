package sk.rama.quotes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import sk.rama.quotes.data.FullQuote;
import sk.rama.quotes.data.QuotesDataSource;

public class StorageActivity extends AppCompatActivity {

    private QuotesDataSource quotesDS;
    private ArrayAdapter<FullQuote> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        quotesDS = new QuotesDataSource(this);
        quotesDS.open();

        mAdapter = new ArrayAdapter<FullQuote>(this, R.layout.list_quote_element);
        ListView quotesLV = (ListView) findViewById(R.id.list_queues);
        quotesLV.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        renderQuotes();
    }

    @Override
    protected void onDestroy() {
        quotesDS.close();
        super.onDestroy();
    }

    private void renderQuotes() {
        List<FullQuote> allQuotes = quotesDS.getAllQuotes();
        mAdapter.clear();
        mAdapter.addAll(allQuotes);
    }
}
