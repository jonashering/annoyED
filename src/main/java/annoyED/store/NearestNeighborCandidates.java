package annoyED.store;

import java.util.HashSet;

public class NearestNeighborCandidates {


  public Datapoint searchPoint;
  public HashSet<Datapoint> candidates;

  public NearestNeighborCandidates(Datapoint sP, HashSet<Datapoint> c) {
    this.searchPoint = sP;
    this.candidates = c;
  }

  public NearestNeighborCandidates() {
    this(new Datapoint(), new HashSet<Datapoint>());
  }
}
