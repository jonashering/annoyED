package annoyED.store;

import java.util.Comparator;
import java.util.HashMap;

public class DatapointComparator implements Comparator<Integer> {
    Datapoint reference = null;
    HashMap<Integer,Datapoint> data = null;


    public DatapointComparator(Datapoint reference, HashMap<Integer,Datapoint> data) {
        this.reference = reference;
        this.data = data;
    }

    public int compare(Integer a, Integer b) {
        Double diffA = this.reference.distTo(this.data.get(a));
        Double diffB = this.reference.distTo(this.data.get(b));
        if (diffA < diffB) {
            return -1;
        } else {
            return 1;
        }
    }
}