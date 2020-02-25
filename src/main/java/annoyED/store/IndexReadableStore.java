package annoyED.store;

import java.util.Vector;

public interface IndexReadableStore {

  NearestNeighbors read(Datapoint datapoint);
  Vector<IndexTree> trees();
  void setParameters(int numTrees, int size, int distanceMetric);
}