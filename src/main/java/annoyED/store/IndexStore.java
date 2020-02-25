package annoyED.store;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

import annoyED.serdes.JsonPojoSerde;

public class IndexStore implements StateStore, IndexWritableStore {
    private Vector<IndexTree> trees = new Vector<IndexTree>();
    private HashMap<Integer,Datapoint> data;
    private String name;
    private Serde<Datapoint> dataSerdes;
    private boolean open = false;


    /**
     * 0 = EUCLIDEAN
     * 1 = ANGULAR
     */
    private Integer distanceMetric = 0;

    public IndexStore(final String name, final Integer numTrees, final Integer distanceMetric) {
        this.name = name;
        this.data = new HashMap<Integer,Datapoint>(100000);
        for (int i = 0; i < numTrees; i++) {
            this.trees.add(new IndexTree());
        }
        this.distanceMetric = distanceMetric;

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
        if (this.distanceMetric == 1) {
            convertToUnitVector(datapoint);
        }
        List<List<Pair>> candidates = new Vector<List<Pair>>(datapoint.k);
        List<Integer> nn = new Vector<Integer>(datapoint.k);
        int[] indices = new int[this.trees.size()];
        for (IndexTree tree: this.trees()) {
            candidates.add(tree.getNeighborCandidates(datapoint, data));
        };
        while (nn.size() < datapoint.k) {
            int index = -1;
            Double d = Double.MAX_VALUE;
            for (int i = 0; i < candidates.size(); i++) {
                if (indices[i] > candidates.get(i).size() - 1) {
                    continue;
                }
                if (candidates.get(i).get(indices[i]).distance < d) {
                    d = candidates.get(i).get(indices[i]).distance;
                    index = i;
                }
            }
            if (index == -1) {
                break;
            }
            int point = candidates.get(index).get(indices[index]).index;
            if (nn.isEmpty() || nn.get(nn.size() - 1) != point) {
                nn.add(point);
            }
            indices[index]++;
        }

        return new NearestNeighbors(nn);
    }

    @Override
    public Vector<IndexTree> trees() {
        return this.trees;
    }

    private void convertToUnitVector(Datapoint d) {
        Double sum = 0D;
        for (Double a : d.vector) {
            sum += Math.pow(a, 2);
        }
        sum = Math.sqrt(sum);
        Vector<Double> newVec = new Vector<Double>(d.vector.size());
        for (Double a: d.vector) {
            newVec.add(a/sum);
        }
        d.vector = newVec;

    }

    @Override
    public void write(String key, Datapoint value) {
        int position = Integer.parseInt(key);
        if (position % 1000 == 0) {
            System.out.println(position);
        }
        if (this.distanceMetric == 1) {
            convertToUnitVector(value);
        }
        data.put(Integer.parseInt(key), value);
        for (int i = 0; i < this.trees.size(); i++) {
            this.trees.get(i).add(value, position, data);
        }
    }

    @Override
    public void setParameters(int numTrees, int size, int distanceMetric) {
        this.data = new HashMap<Integer,Datapoint>(size);
        this.trees = new Vector<IndexTree>(numTrees);
        for (int i = 0; i < numTrees; i++) {
            this.trees.add(new IndexTree());
        }
    }
  }

  