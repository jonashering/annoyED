package annoyED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import annoyED.serdes.SerdesFactory;
import annoyED.store.Datapoint;
import annoyED.store.DatapointComparator;
import annoyED.store.NearestNeighborCandidates;
import annoyED.store.NearestNeighbors;

public class NearestNeighborApp {

    static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "nearest-neighbor-finalizer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                Optional.ofNullable(System.getenv("BOOTSTRAP_SERVERS_CONFIG")).orElse("localhost:9092"));
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    static void createNNStream(final StreamsBuilder builder, final int k) {
        final KStream<Datapoint, NearestNeighborCandidates> stream = builder.stream("sink-topic", Consumed.with(SerdesFactory.from(Datapoint.class, true), SerdesFactory.from(NearestNeighborCandidates.class, false)));
        stream.mapValues((candidates) -> {
            List<Datapoint> sortedList = new ArrayList<>(candidates.candidates);
            DatapointComparator comp = new DatapointComparator(candidates.searchPoint);
            sortedList.sort(comp);
            NearestNeighbors nn = new NearestNeighbors(sortedList);
            return nn;
        }).to("nn-topic", Produced.with(SerdesFactory.from(Datapoint.class, true), SerdesFactory.from(NearestNeighbors.class, false)));;
    }

    public static void main(String[] args) throws Exception {
        final Properties props = getStreamsConfig();
        final int k = 5;
        final StreamsBuilder builder = new StreamsBuilder();
        createNNStream(builder, k);
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("nearest-neighbor-finalizer-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

}