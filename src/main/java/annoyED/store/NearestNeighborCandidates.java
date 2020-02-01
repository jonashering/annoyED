package annoyED.store;

import java.util.HashSet;

public class NearestNeighborCandidates {


  public Datapoint searchPoint;
  public HashSet<Integer> candidates;

  public NearestNeighborCandidates(Datapoint sP, HashSet<Integer> c) {
    this.searchPoint = sP;
    this.candidates = c;
  }

  public NearestNeighborCandidates() {
    this(new Datapoint(), new HashSet<Integer>());
  }
}
