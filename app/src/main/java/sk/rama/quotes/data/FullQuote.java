package sk.rama.quotes.data;

import java.io.Serializable;

/**
 * Created by Radovan Masaryk on 1/3/2017.
 */

public class FullQuote implements Serializable {
    public long id;
    public String quote;
    public String author;
    public String url;

    @Override
    public String toString() {
        return quote + "\n\n- " + author + " -";
    }
}
