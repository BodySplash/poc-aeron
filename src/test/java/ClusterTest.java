/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import io.aeron.cluster.client.*;
import org.agrona.ExpandableArrayBuffer;
import org.junit.*;

public class ClusterTest {

    private AeronCluster aeronCluster;

    @Before
    public void setUp() {
        final EgressMessageListener listener =
                (correlationId, clusterSessionId, timestamp, buffer, offset, length, header) ->
                {
                    System.out.println("Message");
                };
        aeronCluster = AeronCluster.connect(
                new AeronCluster.Context()
                        .egressMessageListener(listener)
                        .ingressChannel("aeron:udp")
                        .clusterMemberEndpoints("0=localhost:9010,1=localhost:9011,2=localhost:9012"));
    }

    @After
    public void tearDown() {
        aeronCluster.close();
    }

    @Test
    public void testSomeLibraryMethod() {
        long correlationId = aeronCluster.nextCorrelationId();
        final ExpandableArrayBuffer msgBuffer = new ExpandableArrayBuffer();
        final String msg = "Hello World!";
        msgBuffer.putStringWithoutLengthAscii(0, msg);
        aeronCluster.offer(correlationId, msgBuffer, 0, msg.length());
    }
}
