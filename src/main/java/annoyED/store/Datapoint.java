package annoyED.store;

import java.util.Vector;

public class Datapoint {


  public String datapointID;
  public Vector<Double> vector;
  public Boolean persist = false;
  public boolean write;
  public Integer k;

  public Datapoint() {
    this("", new Vector<Double>(), false, true, 1000);
  }
  public Datapoint(String datapointId, Vector<Double> vector, Boolean persist, Boolean write, Integer k) {
    this.datapointID = datapointId;
    this.vector = vector;
    this.persist = persist;
    this.write = write;
    this.k = k;
  }

  public Datapoint(String datapointId, Vector<Double> vector) {
    this.datapointID = datapointId;
    this.vector = vector;
    this.persist = false;
    this.write = true;
    this.k = 1000;
  }

  public Double distTo(Datapoint e) {
    Double dist = 0d;
    for (int i = 0; i < this.vector.size(); i++) {
      dist += Math.pow(this.vector.get(i) - e.vector.get(i), 2);
    }

    return Math.sqrt(dist);
  }
}
