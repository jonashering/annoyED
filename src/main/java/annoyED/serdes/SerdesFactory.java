package annoyED.serdes;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serde;

public class SerdesFactory {

  public static <T> Serde<T> from(Class<T> clazz) {
    return from(clazz, true);
  }

  public static <T> Serde<T> from(Class<T> clazz, boolean isKey) {
    Map<String, Object> serdeProps = new HashMap<>();
    serdeProps.put("JsonPOJOClass", clazz);

    JsonPOJOSerde<T> serde = new JsonPOJOSerde<T>();
    serde.configure(serdeProps, isKey);

    return serde;
  }
}


