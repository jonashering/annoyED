package annoyED.store;

import annoyED.data.Datapoint;

public interface IndexWritableStore extends IndexReadableStore {

  void write(String key, Datapoint value);

}