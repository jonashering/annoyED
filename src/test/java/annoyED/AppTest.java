/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package annoyED;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static annoyED.App.*;


public class AppTest {

    private TopologyTestDriver testDriver;
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private LongDeserializer longDeserializer = new LongDeserializer();
    private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());

    @Before
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        App.createNearestNeighborStream(builder);
        testDriver = new TopologyTestDriver(builder.build(), App.getStreamsConfig());
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
        testDriver.pipeInput(recordFactory.create(INPUT_TOPIC, null, "Hello"));

        ProducerRecord<String, Long> outputRecord = testDriver.readOutput(
                OUTPUT_TOPIC,
                stringDeserializer,
                longDeserializer);

        OutputVerifier.compareKeyValue(outputRecord, "hello", 1L);
        assertNull(testDriver.readOutput(OUTPUT_TOPIC, stringDeserializer, longDeserializer));
    }

}
