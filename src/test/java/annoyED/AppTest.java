/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package annoyED;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;



public class AppTest {

    private TopologyTestDriver testDriver;
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private IntegerDeserializer integerDeserializer = new IntegerDeserializer();
    private ConsumerRecordFactory<String, Integer> recordFactory = new ConsumerRecordFactory<String, Integer>(new StringSerializer(), new IntegerSerializer());

    @Before
    public void setup() {
        App app = new App();
        Topology topology = app.build();

        testDriver = new TopologyTestDriver(topology, App.getStreamsConfig());
    }

    @After
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final RuntimeException e) {
            System.out.println("Ignoring exception, test failing in Windows due this exception:" + e.getLocalizedMessage());
        }
    }

    @Test
    public void testOneWord() {
        testDriver.pipeInput(recordFactory.create("source-topic", "Test-5", 5));
        testDriver.pipeInput(recordFactory.create("source-topic", "Test-6", 6));

        ProducerRecord<String, Integer> outputRecord = testDriver.readOutput("sink-topic", stringDeserializer, integerDeserializer);

        OutputVerifier.compareKeyValue(outputRecord, "Test-5", 0);
        System.err.println(outputRecord.key());
        System.err.println(outputRecord.value());
        outputRecord = testDriver.readOutput(
                "sink-topic",
                stringDeserializer,
                integerDeserializer);
        OutputVerifier.compareKeyValue(outputRecord, "Test-6", 1);
        assertNull(testDriver.readOutput("sink-topic", stringDeserializer, integerDeserializer));
    }

}
