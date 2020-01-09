package annoyED.store;

import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.internals.StateStoreProvider;

public class QueryableIndexStoreType implements QueryableStoreType<IndexReadableStore> {


    public boolean accepts(final StateStore stateStore) {
        return stateStore instanceof IndexReadableStore;
    }


    public IndexReadableStore create(final StateStoreProvider storeProvider, final String storeName) {
        return new QueryableIndexStoreTypeWrapper(storeProvider, storeName, this);
    }
    
}