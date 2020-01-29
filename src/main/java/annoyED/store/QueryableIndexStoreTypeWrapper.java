package annoyED.store;

import java.util.List;
import java.util.Vector;

import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.internals.StateStoreProvider;

public class QueryableIndexStoreTypeWrapper implements IndexReadableStore {

    private final QueryableStoreType<IndexReadableStore> customStoreType;
    private final String storeName;
    private final StateStoreProvider provider;

    public QueryableIndexStoreTypeWrapper(final StateStoreProvider provider, final String storeName, final QueryableStoreType<IndexReadableStore> customStoreType) {
        this.customStoreType = customStoreType;
        this.storeName = storeName;
        this.provider = provider;
    }

    @Override
    public NearestNeighborCandidates read(final Datapoint datapoint) {

        final List<IndexReadableStore> stores = provider.stores(storeName, customStoreType);
    // Try and find the value for the given key
        final IndexReadableStore rightStore = stores.stream().filter(store -> store.read(datapoint) != null).findFirst().orElse(null);
        NearestNeighborCandidates candidates = new NearestNeighborCandidates();
        if (rightStore != null) {
            candidates = rightStore.read(datapoint);
        }
    // Return the value if it exists
        
    return candidates;
    }

    @Override
    public Vector<IndexTree> trees() {
        final List<IndexReadableStore> stores = provider.stores(storeName, customStoreType);
        return stores.get(0).trees();
    }

    @Override
    public void setParameters(int numTrees, int searchK, int size) {
        final List<IndexReadableStore> stores = provider.stores(storeName, customStoreType);
        stores.get(0).setParameters(numTrees, searchK, size);

    }
}
