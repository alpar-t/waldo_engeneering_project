package com.github.atorok.waldo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.InputStream;

/**
 *
 */
public class AwsS3BackedDrop implements PictureDrop {
    private final S3ObjectSummary summary;
    private final AmazonS3 client;

    public AwsS3BackedDrop(S3ObjectSummary summary, AmazonS3 client) {
        this.summary = summary;
        this.client = client;
    }

    public S3ObjectSummary getSummary() {
        return summary;
    }

    @Override
    public InputStream getMetadata() {
        return null;
    }

    @Override
    public String getOverallChecksum() {
        return summary.getETag();
    }
}
