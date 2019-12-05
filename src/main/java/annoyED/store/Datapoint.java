package annoyED.store;

import java.util.Vector;

public class Datapoint {


  public String datapointID;
  public Vector<Float> vector;

  public Datapoint() {
    this("", new Vector<Float>());
  }
  public Datapoint(String datapointId, Vector<Float> vector) {
    this.datapointID = datapointId;
    this.vector = vector;
  }
}
