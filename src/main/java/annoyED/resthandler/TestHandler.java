package annoyED.resthandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Vector;

import org.apache.kafka.streams.KafkaStreams;

import annoyED.serdes.JsonPOJOSerializer;
import annoyED.store.Datapoint;
import annoyED.store.IndexReadableStore;
import annoyED.store.NearestNeighborCandidates;
import annoyED.store.QueryableIndexStoreType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TestHandler implements HttpHandler {
    KafkaStreams streams;
  
    public TestHandler(KafkaStreams streams) {
      this.streams = streams;
    }
  
    @Override
    public void handle(HttpExchange t) throws IOException {
        String name = "Test-server";
        Random r = new Random();
        Vector<Double> v = new Vector<Double>();
        for (double j = 0d; j < 5d ; j++) {
            v.add(r.nextDouble());
        }
        Datapoint d = new Datapoint(name, v);
        final IndexReadableStore store = streams.store("AllData", new QueryableIndexStoreType());
        NearestNeighborCandidates nc = store.read(d);
        JsonPOJOSerializer<NearestNeighborCandidates> ser = new JsonPOJOSerializer<NearestNeighborCandidates>();
        byte[] response = ser.serialize("sink-topic", nc);
        ser.close();
        t.sendResponseHeaders(200, response.length);
        OutputStream os = t.getResponseBody();
        os.write(response);
        os.close();
    }
  
  }