package com.github.atorok.waldo;

import com.github.atorok.waldo.api.PictureMetadataDB;
import com.github.atorok.waldo.api.PictureMetadataEntry;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import static java.lang.String.format;

public class ElasticSearchDB implements PictureMetadataDB, AutoCloseable {

    public static final String INDEX_NAME = "pictures";
    public static final String INDEX_TYPE = "METADATA";
    private final JestClient client;

    public ElasticSearchDB() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(
                new HttpClientConfig.Builder("http://localhost:9200").multiThreaded(true).build()
        );
        client = factory.getObject();
    }

    @Override
    public void ingest(String key, Iterator<PictureMetadataEntry> contents) throws IOException {
        HashMap<String, Object> source = new HashMap<>();
        while(contents.hasNext()) {
            PictureMetadataEntry current = contents.next();
            source.put(format("%s::%s", current.getDirectory(), current.getName()), current.getValue());
        }
        source.put("dateProcessed", new Date());
        Index index = new Index.Builder(source).index(INDEX_NAME).type(INDEX_TYPE).id(key).build();
        client.execute(index);
    }

    @Override
    public void close() {
        client.shutdownClient();
    }
}
