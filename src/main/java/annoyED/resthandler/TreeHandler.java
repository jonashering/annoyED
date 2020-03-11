package annoyED.resthandler;

import java.io.OutputStream;
import java.util.Vector;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.kafka.streams.KafkaStreams;

import annoyED.serdes.JsonPOJOSerializer;
import annoyED.store.IndexReadableStore;
import annoyED.store.QueryableIndexStoreType;
import annoyED.tree.IndexTree;

public class TreeHandler implements HttpHandler {
    KafkaStreams streams;
  
    public TreeHandler(KafkaStreams streams) {
      this.streams = streams;
    }
  
    @Override
    public void handle(HttpExchange t) {
        final IndexReadableStore store = streams.store("AllData", new QueryableIndexStoreType());
        Vector<IndexTree> nc = store.trees();
        System.out.println("Got trees: " + String.valueOf(nc.size()));
        JsonPOJOSerializer<Vector<IndexTree>> ser = new JsonPOJOSerializer<Vector<IndexTree>>();
        System.out.println("Created Serializer");
        try {
          byte[] response = ser.serialize("sink-topic", nc);
          System.out.println("Got bytes: " + String.valueOf(response.length));
          ser.close();
          t.sendResponseHeaders(200, response.length);
          OutputStream os = t.getResponseBody();
          os.write(response);
          os.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        
    }
  
  }