package annoyED.store;

public interface IndexReadableStore {

  Datapoint read(String key);
}