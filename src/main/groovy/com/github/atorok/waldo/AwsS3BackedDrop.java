package com.github.atorok.waldo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.atorok.waldo.api.PictureDrop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class AwsS3BackedDrop implements PictureDrop {
    private final Logger logger = LoggerFactory.getLogger(AwsS3BackedDrop.class);

    // TODO: selected by binary search on small sample data, bound to fail
    public static final int METADATA_HEADER_SIZE = 1024 * 85;
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
        logger.info(
                "Requesting the first {} ({} bytes not downloaded) bytes of: {}",
                METADATA_HEADER_SIZE, summary.getSize() - METADATA_HEADER_SIZE, summary
        );
        long requestStart = System.currentTimeMillis();
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(summary.getBucketName(), summary.getKey());
        rangeObjectRequest.setRange(0, METADATA_HEADER_SIZE);

        S3Object objectPortion = client.getObject(rangeObjectRequest);
        logger.debug("Request for s3://{}/{} took {} seconds",
                summary.getBucketName(), summary.getKey(), (System.currentTimeMillis() - requestStart) / 1000.0
        );
        // TODO need a smarter solution here, perhaps an input stream that provides the file using multiple requests if needed
        return objectPortion.getObjectContent();
    }

    @Override
    public String getOverallChecksum() {
        return summary.getETag();
    }
}
