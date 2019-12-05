package annoyED.store;

import java.util.Vector;

public class IndexSplit {
    public Float distance;
    public Vector<Float> unitVector;

    public IndexSplit(Float distance, Vector<Float> unitVector) {
        this.distance = distance;
        this.unitVector = unitVector;
    }

    // >0 = left side
    // <0 = right side
    // =0 = on the split
    public Integer sideOfSplit(Datapoint d) {
        // TODO: implement the check
        return 1;
    }
}