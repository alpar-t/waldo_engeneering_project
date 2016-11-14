package com.github.atorok.waldo

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.github.atorok.waldo.api.PictureMetadataDB
import org.slf4j.LoggerFactory

class Main {

    static def logger = LoggerFactory.getLogger(Main.class)

    public static void main(String[] args) {
        AwsS3Spout spout = new AwsS3Spout(
                new AmazonS3Client(
                        getNoCredentialsProvider()
                )
        )
        PictureMetadataDB db = new ElasticSearchDB();

        try {
            new ParallelMetadataWorkflow(spout, db)
                    .executeWithManagedExceptions();
        }
        finally {
            db.close();
        }
    }

    private static AWSCredentialsProvider getNoCredentialsProvider() {
        // The bucket is public, avoid delays and synchronization issues with the default providers
        new AWSCredentialsProvider() {
            @Override
            AWSCredentials getCredentials() {
                return null
            }

            @Override
            void refresh() {

            }
        }
    }

}
