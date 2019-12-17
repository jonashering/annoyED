package annoyED.store;

import java.util.Vector;

public class IndexSplit {
    public Float distance;
    public Vector<Float> unitVector;

    public IndexSplit(Float distance, Vector<Float> unitVector) {
        this.distance = distance;
        this.unitVector = unitVector;
    }

    public IndexSplit(Datapoint a, Datapoint b) {
        if (a.vector.size() != b.vector.size()) {
            throw new Error("Not the same number of dimensions");
        } else {
            float len = 0;
            this.unitVector = new Vector<>();
            this.distance = 0f;
            Vector<Float> n = new Vector<>();
            Vector<Float> x = new Vector<>();
            for (int i = 0; i < a.vector.size(); i++) {
                Float diff = b.vector.get(i) - a.vector.get(i);
                n.add(diff);
                x.add(b.vector.get(i) + a.vector.get(i) / 2);
                len += diff;
            }
            for (int i = 0; i < n.size(); i++) {
                this.unitVector.add(n.get(i) / len);
                this.distance += x.get(i) * this.unitVector.get(i);
            }
        }
    }

    // >0 = left side
    // <0 = right side
    // =0 = on the split
    public Float sideOfSplit(Datapoint d) {
        Float a = 0f;
        for (int i = 0; i < this.unitVector.size(); i++) {
            a += this.unitVector.get(i) * d.vector.get(i);
        }
        return a - this.distance;
    }
}