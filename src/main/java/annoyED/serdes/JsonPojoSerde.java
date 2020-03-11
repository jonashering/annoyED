package annoyED.serdes;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;


public class JsonPOJOSerde<T> implements Serde<T> {
  final private JsonPOJOSerializer<T> serializer;
  final private JsonPOJODeserializer<T> deserializer;

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    serializer.configure(configs, isKey);
    deserializer.configure(configs, isKey);
  }

  public JsonPOJOSerde() {
    this.serializer = new JsonPOJOSerializer<T>();
    this.deserializer = new JsonPOJODeserializer<T>();
  }

  @Override
  public void close() {
      serializer.close();
      deserializer.close();
  }

  @Override
  public Serializer<T> serializer() {
      return serializer;
  }

  @Override
  public Deserializer<T> deserializer() {
      return deserializer;
  }
}

