package annoyED.resthandler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.kafka.streams.KafkaStreams;
import annoyED.store.IndexReadableStore;
import annoyED.store.QueryableIndexStoreType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ParameterHandler implements HttpHandler {
    KafkaStreams streams;
    String encoding = "ISO-8859-1";
  
    public ParameterHandler(KafkaStreams streams) {
      this.streams = streams;
    }
  
    @Override
    public void handle(HttpExchange t) {
      try {
        final IndexReadableStore store = streams.store("AllData", new QueryableIndexStoreType());
        String query;
        InputStream in = t.getRequestBody();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte buf[] = new byte[4096];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            out.write(buf, 0, n);
        }
        query = new String(out.toByteArray(), encoding);
        String[] parts = query.split(";");
        store.setParameters(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
          byte[] response = "Successfully set params".getBytes();
          t.sendResponseHeaders(200, response.length);
          OutputStream os = t.getResponseBody();
          os.write(response);
          os.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
        
    }
  
  }