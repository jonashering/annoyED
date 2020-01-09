package annoyED.store;

import java.util.Vector;

public class Datapoint {


  public String datapointID;
  public Vector<Double> vector;

  public Datapoint() {
    this("", new Vector<Double>());
  }
  public Datapoint(String datapointId, Vector<Double> vector) {
    this.datapointID = datapointId;
    this.vector = vector;
  }
}
