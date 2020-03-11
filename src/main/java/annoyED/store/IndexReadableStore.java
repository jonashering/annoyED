package annoyED.store;

import java.util.Vector;

import annoyED.data.Datapoint;
import annoyED.data.NearestNeighbors;
import annoyED.tree.IndexTree;

public interface IndexReadableStore {

  NearestNeighbors read(Datapoint datapoint);
  Vector<IndexTree> trees();
  void setParameters(int numTrees, int size, int distanceMetric);
}