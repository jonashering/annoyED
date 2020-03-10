package annoyED.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IndexNode {
    public IndexSplit split = null;
    public Vector<Integer> data = null;
    public IndexNode leftChild = null;
    public IndexNode rightChild = null;
    private Random random = new Random();

    public IndexNode() {
        this.data = new Vector<Integer>();
    }

    public void add(int position){
        this.data.add(position);
    }

    public Integer size() {
        return this.data.size();
    }

    public Boolean hasChildren() {
        return (this.split != null);
    }

    public void split(int dp, HashMap<Integer,Datapoint> d) { // create split and move datapoints to leaf nodes
        this.add(dp);
        List<Datapoint> splitDataPoints = this.getSplitCandidates(d);
        this.split = new IndexSplit(splitDataPoints.get(0), splitDataPoints.get(1));
        this.leftChild = new IndexNode();
        this.rightChild = new IndexNode();
        for (int i = 0; i < this.size(); i++) {
            Integer c = this.data.get(i);
            if (this.split.sideOfSplit(d.get(c)) <= 0){
                this.leftChild.data.add(c);
            } else {
                this.rightChild.data.add(c);
            }
        }
        this.data = null;
    }

    public List<Datapoint> getSplitCandidates(HashMap<Integer, Datapoint> dataPointTable) {
        List<Datapoint> resultList = new ArrayList<>(2); 
        if (false) { // TODO: set somewhere in a config file
            int firstIndex = this.random.nextInt(this.size());
            int secondIndex = this.random.nextInt(this.size() - 1);
            secondIndex += (secondIndex >= firstIndex) ? 1 : 0;
            resultList.add(dataPointTable.get(firstIndex));
            resultList.add(dataPointTable.get(secondIndex));
        } else {
            resultList = this.calculateTwoMeans(dataPointTable);
        }

        return resultList;
    }

    private List<Datapoint> calculateTwoMeans(HashMap<Integer, Datapoint> dataPointTable) {
        int maxIterations = 200 < this.size() ? 200 : this.size();
        List<Integer> iterationOrder = ((Vector<Integer>) this.data.clone()).subList(0, maxIterations);
        Collections.shuffle(iterationOrder);

        Datapoint firstCentroid = dataPointTable.get(iterationOrder.remove(0)).clone();
        Datapoint secondCentroid = dataPointTable.get(iterationOrder.remove(0)).clone();
        int firstCentroidCounter = 1, secondCentroidCounter = 1;

        for (int dataPointIndex : iterationOrder) {
            double distanceToFirstCentroid = firstCentroid.distTo(dataPointTable.get(dataPointIndex));
            double distanceToSecondCentroid = secondCentroid.distTo(dataPointTable.get(dataPointIndex));

            if (distanceToFirstCentroid < distanceToSecondCentroid) {
                for (int i = 0; i < firstCentroid.vector.size(); i++) {
                    firstCentroid.vector.set(
                        i,
                        (firstCentroid.vector.get(i) * firstCentroidCounter + dataPointTable.get(dataPointIndex).vector.get(i)) / (firstCentroidCounter + 1));
                }
                firstCentroidCounter++;
            } else if (distanceToFirstCentroid > distanceToSecondCentroid) {
                for (int i = 0; i < secondCentroid.vector.size(); i++) {
                    secondCentroid.vector.set(
                        i,
                        (secondCentroid.vector.get(i) * secondCentroidCounter + dataPointTable.get(dataPointIndex).vector.get(i)) / (secondCentroidCounter + 1));
                }
                secondCentroidCounter++;
            }
        }

        List<Datapoint> resultTuple = new ArrayList<>(2);
        resultTuple.add(firstCentroid);
        resultTuple.add(secondCentroid);

        return resultTuple;
    }
}

public class IndexTree {
    public IndexNode head;
    private Boolean first;
    private Integer _k;

    public IndexTree() {
        this.head = new IndexNode();
        this.first = true;
    }


    private IndexNode navigateToLeaf(Datapoint d) {
        IndexNode current = head;
        while (current.hasChildren()) { //navigate to correct leaf node
            if (current.split.sideOfSplit(d) < 0) {
                current = current.leftChild;
            } else {
                current = current.rightChild;
            }
        }
        return current;
    }

    public void add(Datapoint d, int position, HashMap<Integer,Datapoint> data) {
        if (this.first) {
            this.first = false;
            this._k = (d.vector.size() / 10) * 10 + 10; // round up to the next 10
            System.out.println("Set _k to " + this._k);
        }
        IndexNode current = this.navigateToLeaf(d);
        if (current.size() < this._k){
            current.add(position);
        } else if (current.size() >= this._k) {
            current.split(position, data);
        }
    }

    public List<Pair> getNeighborCandidates(Datapoint d, HashMap<Integer,Datapoint> data) {
        IndexNode current = this.navigateToLeaf(d);
        Stream<Pair> s = current.data.parallelStream().map(((i) -> {
            return new Pair(i, d.distTo(data.get(i)));
        }));
        List<Pair> ret = s.collect(Collectors.toList());
        Collections.sort(ret);
        return ret.subList(0, Math.min(ret.size(), d.k));
    }
    
}