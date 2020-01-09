package annoyED;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Vector;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;

import annoyED.serdes.JsonPOJOSerializer;
import annoyED.store.Datapoint;
import annoyED.store.IndexReadableStore;
import annoyED.store.NearestNeighborCandidates;
import annoyED.store.QueryableIndexStoreType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class TestHandler implements HttpHandler {
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

  public class RestService {

    private KafkaStreams streams;
    private HttpServer server;

    RestService(final KafkaStreams streams, final HostInfo hostInfo) {
      this.streams = streams;
    }

    public String test() {
      return "Success";
    }

    void stop() throws Exception {
      if (server != null) {
        server.stop(0);
      }
    }

    void start(final int port) throws Exception {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      System.out.println("server started at " + port);
      server.createContext("/test", new TestHandler(streams));
      server.setExecutor(null);
      server.start();
    }

  }
