package annoyED.store;

import java.util.Random;
import java.util.Vector;

class IndexNode {
    public IndexSplit split = null;
    public Vector<Datapoint> data = null;
    public IndexNode leftChild = null;
    public IndexNode rightChild = null;
    private Random random = new Random();

    public IndexNode() {
        this.data = new Vector<Datapoint>();
    }

    public void add(Datapoint d){
        this.data.add(d);
    }

    public Integer size() {
        return this.data.size();
    }

    public Boolean hasChildren() {
        return (this.split != null);
    }

    public void split(Datapoint d) { // create split and move datapoints to leaf nodes
        this.add(d);
        Datapoint a = this.data.get(this.random.nextInt(this.size()));
        Datapoint b = this.data.get(this.random.nextInt(this.size()));
        this.split = new IndexSplit(a, b);
        this.leftChild = new IndexNode();
        this.rightChild = new IndexNode();
        for (int i = 0; i < this.size(); i++) {
            Datapoint c = this.data.get(i);
            if (this.split.sideOfSplit(c) <= 0){
                this.leftChild.data.add(c);
            } else {
                this.rightChild.data.add(c);
            }
        }
        this.data = null;
    }
}

public class IndexTree {
    private IndexNode head;
    private Integer searchK;

    public IndexTree(Integer searchK) {
        this.head = new IndexNode();
        this.searchK = searchK;
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

    public void add(Datapoint d) {
        IndexNode current = this.navigateToLeaf(d);
        if (current.size() < this.searchK){
            current.add(d);
        } else if (current.size() == this.searchK) {
            current.split(d);
        } else {
            throw new Error("You should not end up here!");
        }
    }

    public Vector<Datapoint> getNeighborCandidates(Datapoint d) {
        IndexNode current = this.navigateToLeaf(d);
        return current.data;
    }
    
}