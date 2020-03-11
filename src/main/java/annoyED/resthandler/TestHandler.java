package annoyED.resthandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.kafka.streams.KafkaStreams;

import annoyED.data.Datapoint;
import annoyED.data.NearestNeighbors;
import annoyED.serdes.JsonPOJODeserializer;
import annoyED.serdes.JsonPOJOSerializer;
import annoyED.store.IndexReadableStore;
import annoyED.store.QueryableIndexStoreType;

public class TestHandler implements HttpHandler {
    KafkaStreams streams;
    String encoding = "ISO-8859-1";
    Map<String, Object> serdeProps = new HashMap<>();

  
    public TestHandler(KafkaStreams streams) {
      this.streams = streams;
      this.serdeProps.put("JsonPOJOClass", Datapoint.class);
    }
  
    @Override
    public void handle(HttpExchange t) throws IOException {
      try {
        String query;
        InputStream in = t.getRequestBody();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte buf[] = new byte[4096];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            out.write(buf, 0, n);
        }
        query = new String(out.toByteArray(), encoding);
        System.out.println(query);
        in.close();
        JsonPOJODeserializer<Datapoint> des = new JsonPOJODeserializer<Datapoint>();
        des.configure(this.serdeProps, false);
        Datapoint d = des.deserialize("source-topic", query.getBytes());
        System.out.println(d.datapointID);
        final IndexReadableStore store = streams.store("AllData", new QueryableIndexStoreType());
        NearestNeighbors nc = store.read(d);
        JsonPOJOSerializer<NearestNeighbors> ser = new JsonPOJOSerializer<NearestNeighbors>();
        byte[] response = ser.serialize("sink-topic", nc);
        ser.close();
        des.close();
        t.sendResponseHeaders(200, response.length);
        OutputStream os = t.getResponseBody();
        os.write(response);
        os.close();
      } catch (Exception e) {
        e.printStackTrace();
        byte[] s = "Not working, Dude".getBytes();
        t.sendResponseHeaders(404, s.length);
        OutputStream os = t.getResponseBody();
        os.write(s);
        os.close();
      }
    }  
  }