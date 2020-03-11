package annoyED;

import java.net.InetSocketAddress;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;

import annoyED.resthandler.ParameterHandler;
import annoyED.resthandler.QueryHandler;
import annoyED.resthandler.TreeHandler;
import com.sun.net.httpserver.HttpServer;


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
      server.createContext("/query", new QueryHandler(streams));
      server.createContext("/trees", new TreeHandler(streams));
      server.createContext("/params", new ParameterHandler(streams));
      server.setExecutor(null);
      server.start();
    }

  }
