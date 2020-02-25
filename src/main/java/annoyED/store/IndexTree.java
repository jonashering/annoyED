package annoyED.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

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
        Integer index = this.random.nextInt(this.size() - 1);
        Integer a = this.data.get(index);
        Integer b = this.data.get(index+1);
        this.split = new IndexSplit(d.get(a), d.get(b));
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
        Vector<Pair> ret = new Vector<>(current.data.size());
        for (Integer i : current.data) {
            ret.add(new Pair(i, d.distTo(data.get(i))));
        }
        Collections.sort(ret);
        return ret.subList(0, Math.min(ret.size(), d.k));
    }
    
}