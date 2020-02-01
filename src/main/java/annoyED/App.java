/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package annoyED;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;

import annoyED.store.Datapoint;
import annoyED.store.IndexStoreBuilder;
import annoyED.store.NearestNeighbors;
import annoyED.serdes.SerdesFactory;

import java.util.Optional;
import java.util.Properties;

public class App {
    static final String DEFAULT_HOST = "localhost";
    static final Integer port = 5000;

    static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-nearestneighbor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 
        Optional.ofNullable(System.getenv("BOOTSTRAP_SERVERS_CONFIG")).orElse("localhost:9092")
        );
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, DEFAULT_HOST + ":" + port);
        // setting offset reset to earliest so that we can re-run the demo code with the
        // same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public Topology build() {

        IndexStoreBuilder inputSB = new IndexStoreBuilder("AllData", 4, 10);
        Topology builder = new Topology();
        builder.addSource("Source", Serdes.String().deserializer(), SerdesFactory.from(Datapoint.class).deserializer(),
                "source-topic").addProcessor("Process", () -> new NeighborProcessor(), "Source")
                .addStateStore(inputSB, "Process").addSink("Sink", "sink-topic",
                        SerdesFactory.from(Datapoint.class).serializer(),
                        SerdesFactory.from(NearestNeighbors.class).serializer(), "Process");
        return builder;

    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        final KafkaStreams streams = new KafkaStreams(app.build(), App.getStreamsConfig());
        streams.cleanUp();
        streams.start();

        final RestService restService = startRestProxy(streams, DEFAULT_HOST, port);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                restService.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    static RestService startRestProxy(final KafkaStreams streams,
                                                               final String host,
                                                               final int port) throws Exception {
        final HostInfo hostInfo = new HostInfo(host, port);
        final RestService restService = new RestService(streams, hostInfo);
        restService.start(port);
        return restService;
  }

}
