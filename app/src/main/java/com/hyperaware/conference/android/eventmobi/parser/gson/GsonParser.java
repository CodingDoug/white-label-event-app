package com.hyperaware.conference.android.eventmobi.parser.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hyperaware.conference.android.mechanics.ParseException;
import com.hyperaware.conference.android.mechanics.Parser;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Singleton;

@Singleton
public class GsonParser<T> implements Parser<T> {

    private final Class<T> clazz;

    public GsonParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T parse(InputStream is) throws ParseException {
        try {
            final GsonBuilder gb = new GsonBuilder();
            // Eventmobi serializes booleans as integer strings!  Yuck!
            final BooleanDeserializer bs = new BooleanDeserializer();
            gb.registerTypeAdapter(boolean.class, bs);
            gb.registerTypeAdapter(Boolean.class, bs);
            final Gson gson = gb.create();
            final T response = gson.fromJson(
                new InputStreamReader(is, GsonParserConstants.CHARSET),
                clazz
            );
            if (response != null) {
                return response;
            }
            else {
                throw new ParseException("Reader at EOF");
            }
        }
        catch (Exception e) {
            throw new ParseException(e);
        }
    }

}
