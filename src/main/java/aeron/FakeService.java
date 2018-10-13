package aeron;

import io.aeron.*;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.*;
import io.aeron.cluster.service.Cluster;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.slf4j.*;

public class FakeService implements ClusteredService {
    @Override
    public void onStart(Cluster cluster) {
        LOGGER.info("Start member {}", cluster.memberId());

    }

    @Override
    public void onSessionOpen(ClientSession session, long timestampMs) {
        LOGGER.info("Session open {}", session.id());
    }

    @Override
    public void onSessionClose(ClientSession session, long timestampMs, CloseReason closeReason) {
        LOGGER.info("Session close {}", session.id());
    }

    @Override
    public void onSessionMessage(ClientSession session, long correlationId, long timestampMs, DirectBuffer buffer, int offset, int length, Header header) {
        LOGGER.info("Message {}", correlationId);
        String content = buffer.getStringWithoutLengthAscii(offset, length);
        session.offer(correlationId, buffer, offset, length);
        LOGGER.info("Content {}", content);
    }

    @Override
    public void onTimerEvent(long correlationId, long timestampMs) {
        LOGGER.info("Timer {}", correlationId);
    }

    @Override
    public void onTakeSnapshot(Publication snapshotPublication) {
        LOGGER.info("snapshot");
    }

    @Override
    public void onLoadSnapshot(Image snapshotImage) {
        LOGGER.info("load snapshot");
    }

    @Override
    public void onRoleChange(Cluster.Role newRole) {
        LOGGER.info("New role {}", newRole);
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(FakeService.class);
}
