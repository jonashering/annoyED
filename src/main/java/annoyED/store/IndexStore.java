package annoyED.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import annoyED.serdes.JsonPojoSerde;

import org.apache.kafka.common.serialization.Serde;

public class IndexStore implements StateStore, IndexWritableStore {
    private Vector<IndexTree> trees = new Vector<IndexTree>();
    private ArrayList<Datapoint> data;
    private String name;
    private Integer searchK;
    private Serde<Datapoint> dataSerdes;
    private boolean open = false;

    public IndexStore(final String name, final Integer numTrees, final Integer searchK) {
        this.name = name;
        this.searchK = searchK;
        this.data = new ArrayList<Datapoint>(10000);
        for (int i = 0; i < numTrees; i++) {
            this.trees.add(new IndexTree(this.searchK));
        }

    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void flush() {
        // do nothing cause in memory

    }

    @Override
    public void close() {
        this.trees.clear();
        this.open = false;

    }

    @Override
    public boolean persistent() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public void init(ProcessorContext context, StateStore root) {

        dataSerdes = new JsonPojoSerde<Datapoint>();
        context.register(root, (key, value) -> {
            String sKey = new String(key);
            write(sKey, dataSerdes.deserializer().deserialize(sKey, value));
        });
        this.open = true;

    }


    @Override
    public NearestNeighborCandidates read(Datapoint datapoint) {
        HashSet<Integer> union = new HashSet<Integer>();
        for (int i = 0; i < this.trees.size(); i++) {
            union.addAll(this.trees.get(i).getNeighborCandidates(datapoint));
        }
        HashSet<Datapoint> dps = new HashSet<Datapoint>(union.size());
        for (int i : union) {
            dps.add(this.data.get(i));
        }
        dps.remove(datapoint);
        return new NearestNeighborCandidates(datapoint,dps);
    }

    @Override
    public Vector<IndexTree> trees() {
        return this.trees;
    }

    @Override
    public void write(String key, Datapoint value) {
        int position = data.size();
        data.add(value);
        for (int i = 0; i < this.trees.size(); i++) {
            this.trees.get(i).add(value, position, data);
        }
    }

    @Override
    public void setParameters(int numTrees, int searchK, int size) {
        this.searchK = searchK;
        this.data = new ArrayList<Datapoint>(size);
        this.trees = new Vector<IndexTree>(numTrees);
        for (int i = 0; i < numTrees; i++) {
            this.trees.add(new IndexTree(this.searchK));
        }
    }
  }

  