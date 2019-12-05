package annoyED.store;

import java.util.Map;
import org.apache.kafka.streams.state.StoreBuilder;

public class IndexStoreBuilder implements StoreBuilder<IndexStore> {
    private String name = "AllData";
    private Map<String, String> config;

    @Override
    public StoreBuilder<IndexStore> withCachingEnabled() {
        return this;
    }

    @Override
    public StoreBuilder<IndexStore> withCachingDisabled() {
        return this;
    }

    @Override
    public StoreBuilder<IndexStore> withLoggingEnabled(Map<String, String> config) {
        this.config = config;
        return this;
    }

    @Override
    public StoreBuilder<IndexStore> withLoggingDisabled() {
        return this;
    }

    @Override
    public IndexStore build() {
        return new IndexStore(this.name);
    }

    @Override
    public Map<String, String> logConfig() {
        return this.config;
    }

    @Override
    public boolean loggingEnabled() {
        return false;
    }

    @Override
    public String name() {
        return this.name;
    }
    
}