package com.github.atorok.waldo.api;

import java.io.IOException;
import java.util.Iterator;

public interface PictureMetadataDB {
    void ingest(String key, Iterator<PictureMetadataEntry> contents) throws IOException;
}
