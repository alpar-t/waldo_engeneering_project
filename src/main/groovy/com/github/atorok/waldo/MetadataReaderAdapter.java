package com.github.atorok.waldo;

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Adapt the metadata reader library to provide a generic flow of metadata entries
 *
 * The entries will include any possible processing errors.
 */
public class MetadataReaderAdapter {

    private final Logger logger = LoggerFactory.getLogger(MetadataReaderAdapter.class);
    private final InputStream in;

    public MetadataReaderAdapter(InputStream in) {
        this.in = in;
    }

    public Stream<PictureMetadataEntry> stream() throws IOException {
        Metadata metadata;
        try {
            // could look at magic numbers here to skip files that are not jpg
            // could also parse metadata for all, but that's  less efficient as it requires more of the file to be present
            metadata = JpegMetadataReader.readMetadata(this.in);
        } catch (ImageProcessingException e) {
            return Stream.of(new AdaptedMetadataEntry(e));
        } catch (EOFException e) {
            logger.warn("Encountered EOF: we might not have enough of the file header");
            throw e;
        }
        return StreamSupport.stream(metadata.getDirectories().spliterator(), false)
                .flatMap( directory ->
                        Stream.concat(
                                directory.getTags().stream().map(tag ->
                                        new AdaptedMetadataEntry(tag)
                                ),
                                StreamSupport.stream(directory.getErrors().spliterator(), false).map( error ->
                                        new AdaptedMetadataEntry(directory.getName(), error)
                                )
                        )
                );
    }

}
