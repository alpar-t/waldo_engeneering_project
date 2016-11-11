package com.github.atorok.waldo

import org.junit.Test
import org.slf4j.LoggerFactory

// TODO: move this to a separate source set and run it on "check" only (not on "test")
class AwsS3SpoutIntegrationTest {

    def logger = LoggerFactory.getLogger(AwsS3SpoutIntegrationTest.class);

    @Test
    void testListing() {
        def spout = new AwsS3Spout()
        spout.each {
            logger.info("Drop with checksum: {}", it.overallChecksum)
        }
    }

}
