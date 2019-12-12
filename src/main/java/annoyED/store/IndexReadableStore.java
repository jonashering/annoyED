package annoyED.store;


public interface IndexReadableStore {

  NearestNeighborCandidates read(Datapoint datapoint);
}