package sk.rama.quotes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

        mAdapter = new ArrayAdapter<>(this, R.layout.list_quote_element);
        ListView quotesLV = (ListView) findViewById(R.id.list_queues);
        quotesLV.setAdapter(mAdapter);
        quotesLV.setTextFilterEnabled(true);

        // deletion
        quotesLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(StorageActivity.this);
                alert.setTitle("Delete");
                alert.setMessage("Do you want to delete the quote");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FullQuote fq = mAdapter.getItem(position);
                        quotesDS.deleteQuote(fq);
                        mAdapter.remove(fq);
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            }
        });

        // copy to clipboard
        quotesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView mTextView = (TextView) view;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("quote", mTextView.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(StorageActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // search
        FloatingActionButton searchButton = (FloatingActionButton) findViewById(R.id.quotes_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        renderQuotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_storage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_storage_help) {
            AlertDialog.Builder alert = new AlertDialog.Builder(StorageActivity.this);
            alert.setTitle("Help")
                    .setMessage(R.string.help_storage)
                    .setNeutralButton("OK", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
