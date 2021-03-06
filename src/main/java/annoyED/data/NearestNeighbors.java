package annoyED.data;

import java.util.ArrayList;
import java.util.List;

public class NearestNeighbors {
    public List<Integer> list;


    public NearestNeighbors() {
        this.list = new ArrayList<Integer>();
    }

    public NearestNeighbors(List<Integer> list) {
        this.list = list;
    }

	public static NearestNeighbors fromPairs(List<Pair> dps) {
        NearestNeighbors nn = new NearestNeighbors();
        for (Pair p : dps) {
            nn.list.add(p.index);
        }
        return nn;
	}
}
