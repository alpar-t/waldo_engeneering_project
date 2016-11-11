package com.github.atorok.waldo

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.github.atorok.waldo.api.PictureMetadataDB
import groovyx.gpars.GParsPool
import org.slf4j.LoggerFactory

class Main {

    static def logger = LoggerFactory.getLogger(Main.class)

    public static void main(String[] args) {
        int numberOfThreads = 15 * Runtime.getRuntime().availableProcessors()
        AwsS3Spout spout = new AwsS3Spout(new AmazonS3Client(getNoCredentialsProvider()))
        PictureMetadataDB db = new ElasticSearchDB();

        logger.info("Running on {} threads", numberOfThreads)
        GParsPool.withPool(numberOfThreads) {
            spout.eachParallel { drop ->
                try {
                    db.ingest(drop.getOverallChecksum(), parseMetadataFromDrop(drop));
                } catch (IOException e) {
                    logger.error("Failed to ingest results for {}", drop, e)
                }
            }
        }
        logger.info("Completed parsing all metadata")
        db.close();
    }

    private static Iterator<AdaptedMetadataEntry> parseMetadataFromDrop(drop) {
        try {
            drop.getMetadata().withCloseable({ inputStream ->
                return new MetadataReaderAdapter(inputStream).stream().iterator();
            })
        } catch (Exception e) {
            return Collections.singleton(new AdaptedMetadataEntry(e)).iterator()
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
