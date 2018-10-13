package aeron;

import io.aeron.archive.*;
import io.aeron.cluster.*;
import io.aeron.cluster.service.*;
import io.aeron.driver.*;
import org.agrona.CloseHelper;
import org.agrona.concurrent.*;
import org.slf4j.*;

import java.io.File;
import java.nio.file.Paths;

public class Cluster {

    public static final String BASE_PATH = Paths.get(".").toAbsolutePath().resolve("aeron").toString();
    public static final String ARCHIVE_PATH = Paths.get(".").toAbsolutePath().resolve("aeron").resolve("archive").toString();
    public static final String CONSENSUS_PATH = Paths.get(".").toAbsolutePath().resolve("aeron").resolve("consensus").toString();

    public void start() {
        try (ClusteredMediaDriver driver = runMediaDriver()) {
            LOGGER.info("Media driver up and running");
            final ClusteredService echoService = new FakeService();
            ClusteredServiceContainer container = ClusteredServiceContainer.launch(
                    new ClusteredServiceContainer.Context()
                            .aeronDirectoryName(BASE_PATH)
                            .clusterDir(new File(CONSENSUS_PATH))
                            .clusteredService(echoService)
                            .errorHandler(Throwable::printStackTrace));
            LOGGER.info("Waiting for shutdown");
            SigInt.register(driver.consensusModule().context().terminationHook()::run);
            driver.consensusModule().context().shutdownSignalBarrier().await();
            LOGGER.info("Shutdown");
            stop(driver, container);
        }

    }

    private ClusteredMediaDriver runMediaDriver() {
        return ClusteredMediaDriver.launch(
                new MediaDriver.Context()
                        .aeronDirectoryName(BASE_PATH)
                        .threadingMode(ThreadingMode.SHARED)
                        .termBufferSparseFile(true)
                        .errorHandler(Throwable::printStackTrace)
                        .dirDeleteOnStart(true),
                new Archive.Context()
                        .aeronDirectoryName(BASE_PATH)
                        .archiveDir(new File(ARCHIVE_PATH))
                        .maxCatalogEntries(MAX_CATALOG_ENTRIES)
                        .threadingMode(ArchiveThreadingMode.SHARED)
                        .deleteArchiveOnStart(true),
                new ConsensusModule.Context()
                        .aeronDirectoryName(BASE_PATH)
                        .clusterDir(new File(CONSENSUS_PATH))
                        .errorHandler(Throwable::printStackTrace)
                        .deleteDirOnStart(true));
    }

    public void stop(ClusteredMediaDriver driver, ClusteredServiceContainer container) {
        CloseHelper.close(container);
        CloseHelper.close(driver);

        if (null != driver) {
            driver.consensusModule().context().deleteDirectory();
            driver.archive().context().deleteArchiveDirectory();
            driver.mediaDriver().context().deleteAeronDirectory();
        }
    }


    private static final long MAX_CATALOG_ENTRIES = 1024;
    public static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);
}
