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
        Integer index = this.random.nextInt(this.size() - 1);
        Datapoint a = this.data.get(index);
        Datapoint b = this.data.get(index+1);
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
    public IndexNode head;
    public Integer searchK;

    public IndexTree(Integer searchK) {
        this.head = new IndexNode();
        this.searchK = searchK;
    }

    public IndexTree() {
        this(5);
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
            current.split(d);
            System.err.println("We're growing too big");
        }
    }

    public Vector<Datapoint> getNeighborCandidates(Datapoint d) {
        IndexNode current = this.navigateToLeaf(d);
        return current.data;
    }
    
}