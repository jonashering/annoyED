package annoyED.store;

import java.util.HashMap;
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
        Integer index1 = this.random.nextInt(this.size());
        Integer index2 = this.random.nextInt(this.size());
        while (index1 == index2) {
            index2 = this.random.nextInt(this.size());  
        }
        Integer a = this.data.get(index1);
        Integer b = this.data.get(index2);
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

    public void add(Datapoint d, int position, HashMap<Integer,Datapoint> data) {
        IndexNode current = this.navigateToLeaf(d);
        if (current.size() < this.searchK){
            current.add(position);
        } else if (current.size() == this.searchK) {
            current.split(position, data);
        } else {
            current.split(position, data);
            System.err.println("We're growing too big");
        }
    }

    public Vector<Integer> getNeighborCandidates(Datapoint d) {
        IndexNode current = this.navigateToLeaf(d);
        return current.data;
    }
    
}