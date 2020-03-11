package annoyED;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

import annoyED.data.Datapoint;
import annoyED.store.IndexStore;

public class NeighborProcessor implements Processor<String, Datapoint> {

    private ProcessorContext context;
    private IndexStore inputStore;

    @Override
    public void init(ProcessorContext context) {

        this.context = context;
        // retrieve the key-value store named "Counts"
        this.inputStore = (IndexStore) context.getStateStore("AllData");
    }

    @Override
    public void process(String name, Datapoint value) {
        if ( value.write) {

            context.forward(value, this.inputStore.read(value));
            context.commit();
        }
        if (value.persist) {
            this.inputStore.write(name, value);
        }

    }

    @Override
    public void close() {
        // nothing to do
    }
  }
  