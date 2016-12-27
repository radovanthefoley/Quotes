package sk.rama.quotes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.io.IOException;
import java.net.URL;

import sk.rama.quotes.utils.Connection;

/**
 * Created by Radovan Masaryk on 12/26/2016.
 * <p>
 * This fragment will by activity as retain fragment as found in
 * http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */

public class QuoteLoadTaskRetainFragment extends Fragment {
    public static final String URL = "http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json";
    private static final String TAG = "QuoteLoadTaskRetainFragment";
    private QuoteTaskAware mCallbacks;

    public QuoteLoadTaskRetainFragment() {
    }

    public static QuoteLoadTaskRetainFragment findOrCreateQuoteLoadTaskRetainFragment(FragmentManager fm) {
        QuoteLoadTaskRetainFragment fragment = (QuoteLoadTaskRetainFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new QuoteLoadTaskRetainFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /*
    * onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is deprecated
    * Use onAttachToContext instead
    */
    @TargetApi(23)
    @Override
    public final void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
        mCallbacks = (QuoteTaskAware) context;
        //Log.i("tag", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        //Log.i("tag", "onDetach");
    }

    public void loadAsync() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = null;
                try {
                    result = Connection.downloadUrl(new URL(URL));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String response) {
                //Log.i("tag", "postExec " + mCallbacks);
                if (mCallbacks != null) {
                    mCallbacks.onResponseReceived(response);
                }
            }
        }.execute();
    }

    interface QuoteTaskAware {
        void onResponseReceived(String response);
    }
}

