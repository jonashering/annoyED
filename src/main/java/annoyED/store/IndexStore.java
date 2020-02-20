package annoyED.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import annoyED.serdes.JsonPojoSerde;

import org.apache.kafka.common.serialization.Serde;

public class IndexStore implements StateStore, IndexWritableStore {
    private Vector<IndexTree> trees = new Vector<IndexTree>();
    private HashMap<Integer,Datapoint> data;
    private String name;
    private Serde<Datapoint> dataSerdes;
    private boolean open = false;

    public IndexStore(final String name, final Integer numTrees) {
        this.name = name;
        this.data = new HashMap<Integer,Datapoint>(100000);
        for (int i = 0; i < numTrees; i++) {
            this.trees.add(new IndexTree());
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
    public NearestNeighbors read(Datapoint datapoint) {
        HashSet<Integer> union = new HashSet<Integer>();
        for (int i = 0; i < this.trees.size(); i++) {
            union.addAll(this.trees.get(i).getNeighborCandidates(datapoint));
        }
        DatapointComparator comp = new DatapointComparator(datapoint, this.data);
        List<Integer> dps = new ArrayList<Integer>(union);
        dps.sort(comp);
        dps.subList(0, Math.min(dps.size(),15));

        return new NearestNeighbors(dps);
    }

    @Override
    public Vector<IndexTree> trees() {
        return this.trees;
    }

    @Override
    public void write(String key, Datapoint value) {
        int position = Integer.parseInt(key);
        if (position % 1000 == 0) {
            System.out.println(position);
        }
        data.put(Integer.parseInt(key), value);
        for (int i = 0; i < this.trees.size(); i++) {
            this.trees.get(i).add(value, position, data);
        }
    }

    @Override
    public void setParameters(int numTrees, int size) {
        this.data = new HashMap<Integer,Datapoint>(size);
        this.trees = new Vector<IndexTree>(numTrees);
        for (int i = 0; i < numTrees; i++) {
            this.trees.add(new IndexTree());
        }
    }
  }

  