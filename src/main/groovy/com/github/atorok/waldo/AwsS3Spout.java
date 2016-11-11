package com.github.atorok.waldo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class AwsS3Spout implements PictureSpout {

    public static final String JPEG = ".jpg";
    private final Logger logger = LoggerFactory.getLogger(AwsS3Spout.class);

    public static final int DEFAULT_MAX_KEYS_IN_REQUEST = 130;
    private final AmazonS3 client;
    private final String bucketName;
    private final int maxKeysToList;

    private final ListObjectsV2Request currentRequest;
    private ListObjectsV2Result currentResult;
    private Iterator<S3ObjectSummary> innerIterator;

    public AwsS3Spout(AmazonS3 client, String bucketName, int maxKeysToList) {
        this.client = client;
        this.bucketName = bucketName;
        this.maxKeysToList = maxKeysToList;
        this.currentRequest = new ListObjectsV2Request().withBucketName(this.bucketName).withMaxKeys(this.maxKeysToList);
    }

    public AwsS3Spout() {
        this(new AmazonS3Client());
    }

    public AwsS3Spout(AmazonS3 client) {
        this(client, "waldo-recruiting", DEFAULT_MAX_KEYS_IN_REQUEST);
    }

    @Override
    public boolean hasNext() {
        fetchResult();
        return innerIterator.hasNext();
    }

    @Override
    public PictureDrop next() {
        fetchResult();
        S3ObjectSummary next = innerIterator.next();
        if (! next.getStorageClass().equals(StorageClass.Standard.toString())) {
            logger.warn("Object in bucket {} is not in storage class {}: {}", bucketName, StorageClass.Standard, next);
        }
        // TODO: should we trust the keys/extensions and attempt to filter out images ?
        if (! next.getKey().toLowerCase().endsWith(JPEG)) {
            logger.info("Bucket {} has a key without the {} extension (will return it regardless): {} ", bucketName, JPEG, next);
        }
        return new AwsS3BackedDrop(next, client);
    }

    private void fetchResult() {
        if (innerIterator != null && innerIterator.hasNext()) {
            // no need to fetch yet
            return;
        }
        if (currentResult != null) {
            if (currentResult.isTruncated()) {
                logger.debug("Configuring continuation token on list request on {}", bucketName);
                currentRequest.setContinuationToken(currentResult.getNextContinuationToken());
            } else {
                // we are past the first request, which was not truncated, no need to fire an additional one
                return;
            }
        }
        logger.info("Listing S3 bucket {}", bucketName);
        currentResult = client.listObjectsV2(currentRequest);
        logger.info("Listing of S3 bucket {} returned {} results", bucketName, currentResult.getKeyCount());
        innerIterator = currentResult.getObjectSummaries().iterator();
    }


}
