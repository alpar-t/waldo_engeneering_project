package com.github.atorok.waldo;

import com.drew.metadata.Tag;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static java.lang.String.format;

public class AdaptedMetadataEntry implements PictureMetadataEntry {

    private final String directory;
    private final String name;
    private final String value;

    public AdaptedMetadataEntry(Tag tag) {
        directory = tag.getDirectoryName();
        name = tag.getTagName();
        value = tag.getDescription();
    }

    public AdaptedMetadataEntry(String directoryName, String error) {
        directory = directoryName;
        name = "METADATA_PARSING_ERROR";
        value = error;
    }

    public AdaptedMetadataEntry(Throwable e) {
        directory = "EXCEPTION";
        Throwable cause = ExceptionUtils.getRootCause(e);
        if (cause == null) {
            cause = e;
        }
        name = format("%s:%s", cause.getClass().getName(), cause.getMessage());
        value = ExceptionUtils.getStackTrace(e);
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("directory", directory)
                .append("name", name)
                .append("value", value)
                .toString();
    }
}
