package com.github.atorok.waldo

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import groovyx.gpars.GParsPool
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

// TODO: move this to a separate source set and run it on "check" only (not on "test")
class OverallIntegrationTest {

    def logger = LoggerFactory.getLogger(OverallIntegrationTest.class)
    private spout

    @Before
    public void setUp() throws Exception {
        spout = new AwsS3Spout(new AmazonS3Client(new AWSCredentialsProvider() {
            @Override
            AWSCredentials getCredentials() {
                return null
            }

            @Override
            void refresh() {

            }
        }))
    }

    @Test
    void testListing() {
        // override the default credentials provider, it serializes the parallel requests
        this.spout.each {
            logger.info("Drop with checksum: {}", it.overallChecksum)
        }
    }

    @Test
    void testListingWithExifParser() {
        def numberOfThreads = 15 * Runtime.getRuntime().availableProcessors()
        logger.info("Running on {} threads", numberOfThreads)
        GParsPool.withPool(
                numberOfThreads,
                { e -> logger.error("Could not process {}", e) }
        ) {
            spout.eachParallel { drop ->
                    try {
                        def startTime = System.currentTimeMillis();
                        drop.getMetadata().withCloseable({ inputStream ->
                            def adapter = new MetadataReaderAdapter(inputStream)
                            logger.info("found {} metadata keys in {} s",
                                    adapter.stream().collect(Collectors.toList()).size(),
                                    (System.currentTimeMillis() - startTime) / 1000
                            )
                        })
                    } catch (AmazonS3Exception e) {
                        if (e.getStatusCode() == 403) {
                            logger.info("Ignoring access denied to {}", drop, e)
                        } else {
                            throw e;
                        }
                    }
            }
        }
    }

}
