package annoyED.store;

import java.util.Vector;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.common.serialization.Serdes;

public class IndexStore implements StateStore, IndexWritableStore {
    private final Vector<String> vector = new Vector<String>();
    private String name;
    private boolean open = false;

    public IndexStore(final String name) {
        this.name = name;
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
        this.vector.clear();
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

        context.register(root, (key, value) -> {
            String sKey = new String(key);
            write(sKey, Serdes.Integer().deserializer().deserialize(sKey, value));
        });
        this.open = true;

    }


    @Override
    public Integer read(String key) {
        return this.vector.indexOf(key);
    }

    @Override
    public void write(String key, Integer value) {
        this.vector.add(key);

    }


  }

  