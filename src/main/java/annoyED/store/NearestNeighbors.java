package annoyED.store;

import java.util.ArrayList;
import java.util.List;

public class NearestNeighbors {
    public List<Datapoint> list;


    public NearestNeighbors() {
        this.list = new ArrayList<Datapoint>();
    }

    public NearestNeighbors(List<Datapoint> list) {
        this.list = list;
    }
}
