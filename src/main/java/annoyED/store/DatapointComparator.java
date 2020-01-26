package annoyED.store;

import java.util.Comparator;

public class DatapointComparator implements Comparator<Datapoint> {
    Datapoint reference = null;

    public DatapointComparator(Datapoint reference) {
        this.reference = reference;
    }

    public int compare(Datapoint a, Datapoint b) {
        Double diffA = this.reference.distTo(a);
        Double diffB = this.reference.distTo(b);
        if (diffA < diffB) {
            return -1;
        } else {
            return 1;
        }
    }
}