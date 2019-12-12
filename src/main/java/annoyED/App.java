/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package annoyED;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import annoyED.store.Datapoint;
import annoyED.store.IndexStoreBuilder;
import annoyED.store.NearestNeighborCandidates;
import annoyED.serdes.SerdesFactory;

import java.util.Properties;


public class App {
    public static final String INPUT_TOPIC = "streams-point-input";
    public static final String OUTPUT_TOPIC = "streams-nearestneighbor-output";
    public static final String STORE_NAME = "CountsKeyValueStore";

    static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-nearestneighbor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }


    public Topology build() {

        
        IndexStoreBuilder inputSB = new IndexStoreBuilder("AllData",1, 3);
        Topology builder = new Topology();
        builder.addSource("Source", Serdes.String().deserializer(), SerdesFactory.from(Datapoint.class).deserializer(), "source-topic")
            .addProcessor("Process", () -> new NeighborProcessor(), "Source")
            .addStateStore(inputSB, "Process")
            .addSink("Sink", "sink-topic", SerdesFactory.from(Datapoint.class).serializer(), SerdesFactory.from(NearestNeighborCandidates.class).serializer() , "Process");
        return builder;


    }
}
