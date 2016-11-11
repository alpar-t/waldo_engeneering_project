package com.github.atorok.waldo

import com.amazonaws.services.s3.model.AmazonS3Exception
import org.junit.Test
import org.slf4j.LoggerFactory

import static org.junit.Assert.fail
// TODO: move this to a separate source set and run it on "check" only (not on "test")
class OverallIntegrationTest {

    def logger = LoggerFactory.getLogger(OverallIntegrationTest.class);

    @Test
    void testListing() {
        def spout = new AwsS3Spout()
        spout.each {
            logger.info("Drop with checksum: {}", it.overallChecksum)
        }
    }

    @Test
    void testListingWithExifParser() {
        def spout = new AwsS3Spout()
        spout.each {
            try {
                try {
                    (new MetadataReaderAdapter(it.getMetadata())).stream().forEach({ metadata ->
                        logger.info("dound metadata: {}", metadata)
                    })
                } catch (AmazonS3Exception e) {
                    if (e.getStatusCode() == 403) {
                        logger.info("Ignoring access denied to {}", it, e)
                    } else {
                        throw e;
                    }
                }
            }
            catch (Exception e) {
                logger.error("Could not process {}", it, e)
                fail("unexpected error")
            }
        }
    }

}
