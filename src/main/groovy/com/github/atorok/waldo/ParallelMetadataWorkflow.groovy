package com.github.atorok.waldo

import com.github.atorok.waldo.api.PictureDrop
import com.github.atorok.waldo.api.PictureMetadataDB
import com.github.atorok.waldo.api.PictureSpout
import groovyx.gpars.GParsPool
import org.slf4j.LoggerFactory

class ParallelMetadataWorkflow {
    private logger = LoggerFactory.getLogger(ParallelMetadataWorkflow.class)
    private final PictureSpout spout
    private final PictureMetadataDB db

    ParallelMetadataWorkflow(PictureSpout spout, PictureMetadataDB db) {
        this.spout = spout
        this.db = db
    }

    void executeWithManagedExceptions() {
        int numberOfThreads = 15 * Runtime.getRuntime().availableProcessors()
        logger.info("Running on {} threads", numberOfThreads)
        GParsPool.withPool(numberOfThreads) {
            spout.eachParallel { PictureDrop drop ->
                try {
                    db.ingest(drop.getOverallChecksum(), parseMetadataFromDrop(drop));
                } catch (IOException e) {
                    logger.error("Failed to ingest results for {}", drop, e)
                }
            }
        }
        logger.info("Completed parsing all metadata")
    }

    private Iterator<AdaptedMetadataEntry> parseMetadataFromDrop(PictureDrop drop) {
        try {
            drop.getMetadata().withCloseable({ new MetadataReaderAdapter(it).stream().iterator() })
        } catch (Exception e) {
            return Collections.singleton(new AdaptedMetadataEntry(e)).iterator()
        }
    }
}
