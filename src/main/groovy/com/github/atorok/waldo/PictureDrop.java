package com.github.atorok.waldo;

import java.io.InputStream;

/**
 * Generic Interface for a picture to be processed
 */
public interface PictureDrop {

    /**
     * Provide an input stream to the metadata section only.
     *
     * The input stream might have additional data, but it can't must have the complete metadata section.
     * @return
     */
    InputStream getMetadata();

    String getOverallChecksum();

}
