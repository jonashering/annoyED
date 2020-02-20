package annoyED;

import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;

import annoyED.serdes.SerdesFactory;
import annoyED.store.Datapoint;

public class Producer {

    public static void main(String[] args) {
        final Properties prodprops = new Properties();
        prodprops.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        final KafkaProducer<String, Datapoint> prod = new KafkaProducer<>(prodprops, Serdes.String().serializer(), SerdesFactory.from(Datapoint.class).serializer());
        Datapoint d = null;
        Random r = new Random();
        for (int i = 0; i < 10000; i++) {
            String name = String.valueOf(i);
            Vector<Double> v = new Vector<Double>();
            for (double j = 0d; j < 5d ; j++) {
                v.add(Double.valueOf(r.nextInt(10)));
            }
            d = new Datapoint(name, v, true, true);
            prod.send(new ProducerRecord<String,Datapoint>("source-topic", name, d));
        }
        prod.close();
    }
}