package annoyED.store;

public interface IndexWritableStore extends IndexReadableStore {

  void write(String key, Datapoint value);

}