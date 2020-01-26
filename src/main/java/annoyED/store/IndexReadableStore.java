package annoyED.store;

import java.util.Vector;

public interface IndexReadableStore {

  NearestNeighborCandidates read(Datapoint datapoint);
  Vector<IndexTree> trees();
  void setParameters(int numTrees, int searchK);
}