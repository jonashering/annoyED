package annoyED.data;

public class Pair implements Comparable<Pair> {
    public Integer index;
    public Double distance;

    public Pair(Integer index, Double distance) {
        this.index = index;
        this.distance = distance;
    }

    public int compareTo(Pair other) {
        return distance.compareTo(other.distance);
    }
}