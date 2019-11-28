package annoyED;

import java.time.Duration;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.KeyValue;

public class NeighborProcessor implements Processor<String, Integer> {

    private ProcessorContext context;
    private KeyValueStore<String, Integer> inputStore;
    private KeyValueStore<String, String> outputStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        // keep the processor context locally because we need it in punctuate() and
        // commit()
        this.context = context;

        // retrieve the key-value store named "Counts"
        this.inputStore = (KeyValueStore) context.getStateStore("AllData");
        this.outputStore = (KeyValueStore) context.getStateStore("NearestNeighbor");

        // schedule a punctuate() method every second based on event-time
        // this.context.schedule(Duration.ofMillis(1), PunctuationType.STREAM_TIME, (timestamp) -> {
        //     KeyValueIterator<String, String> iter = this.outputStore.all();
        //     while (iter.hasNext()) {
        //         KeyValue<String, String> entry = iter.next();
        //         context.forward(entry.key, entry.value);
        //     }
        //     iter.close();

        //     // commit the current processing progress
        //     context.commit();
        // });
    }

    @Override
    public void process(String name, Integer value) {
        String outputName = "Not found";
        Integer outputValue = Integer.MAX_VALUE;
        KeyValueIterator<String, Integer> iter = this.inputStore.all();
        while (iter.hasNext()) {
            KeyValue<String, Integer> entry = iter.next();
            if ((Math.abs(entry.value - value) < Math.abs(outputValue - value)) && !(entry.key.equals(name))) {
                outputValue = entry.value;
                outputName = entry.key;
            }

            // context.forward(entry.key, entry.value.toString());
        }
        iter.close();
        this.outputStore.put(name,outputName);
        this.inputStore.put(name, value);

        context.forward(name, outputName);
        context.commit();

    }

    @Override
    public void close() {
        // nothing to do
    }
  }
  