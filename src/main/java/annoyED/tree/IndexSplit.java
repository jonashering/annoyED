package annoyED.tree;

import java.util.Vector;

import annoyED.data.Datapoint;

public class IndexSplit {
    public Double distance;
    public Vector<Double> unitVector;

    public IndexSplit(Double distance, Vector<Double> unitVector) {
        this.distance = distance;
        this.unitVector = unitVector;
    }

    public IndexSplit(Datapoint a, Datapoint b) {
        if (a.vector.size() != b.vector.size()) {
            throw new Error("Not the same number of dimensions");
        } else {
            Double len = 0d;
            this.unitVector = new Vector<>();
            this.distance = 0d;
            Vector<Double> n = new Vector<>();
            Vector<Double> x = new Vector<>();
            for (int i = 0; i < a.vector.size(); i++) {
                Double diff = b.vector.get(i) - a.vector.get(i);
                n.add(diff);
                x.add((b.vector.get(i) + a.vector.get(i)) / 2);
                len += Math.pow(diff, 2);
            }
            for (int i = 0; i < n.size(); i++) {
                this.unitVector.add(n.get(i) / Math.sqrt(len));
                this.distance += x.get(i) * this.unitVector.get(i);
            }
        }
    }

    // >0 = left side
    // <0 = right side
    // =0 = on the split
    public Double sideOfSplit(Datapoint d) {
        Double a = 0d;
        for (int i = 0; i < this.unitVector.size(); i++) {
            a += this.unitVector.get(i) * d.vector.get(i);
        }
        return a - this.distance;
    }
}