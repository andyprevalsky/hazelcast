/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map.impl;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.PartitioningStrategyConfig;
import com.hazelcast.internal.eviction.ExpirationManager;
import com.hazelcast.internal.util.comparators.ValueComparator;
import com.hazelcast.map.impl.event.MapEventPublisher;
import com.hazelcast.map.impl.eviction.MapClearExpiredRecordsTask;
import com.hazelcast.map.impl.journal.MapEventJournal;
import com.hazelcast.map.impl.nearcache.MapNearCacheManager;
import com.hazelcast.map.impl.operation.MapOperationProvider;
import com.hazelcast.map.impl.query.QueryEngine;
import com.hazelcast.map.impl.query.QueryRunner;
import com.hazelcast.map.impl.query.ResultProcessorRegistry;
import com.hazelcast.map.impl.querycache.QueryCacheContext;
import com.hazelcast.map.impl.record.Record;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.map.impl.recordstore.RecordStoreMutationObserver;
import com.hazelcast.monitor.impl.LocalMapStatsImpl;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.partition.PartitioningStrategy;
import com.hazelcast.query.impl.IndexCopyBehavior;
import com.hazelcast.query.impl.IndexProvider;
import com.hazelcast.query.impl.getters.Extractors;
import com.hazelcast.query.impl.predicates.QueryOptimizer;
import com.hazelcast.spi.impl.eventservice.EventFilter;
import com.hazelcast.spi.impl.NodeEngine;
import com.hazelcast.spi.impl.operationservice.Operation;
import com.hazelcast.internal.util.collection.PartitionIdSet;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Context which is needed by a map service.
 * <p>
 * Shared instances, configurations of all
 * maps can be reached over this context.
 * <p>
 * Also this context provides some support methods which are used
 * in map operations and {@link RecordStore} implementations. For
 * example all {@link PartitionContainer} and {@link MapContainer}
 * instances can also be reached by using this interface.
 * <p>
 * It is also responsible for providing methods which are used by
 * lower layers of Hazelcast and exposed on {@link MapService}.
 *
 * @see MapManagedService
 */
public interface MapServiceContext extends MapServiceContextInterceptorSupport,
        MapServiceContextEventListenerSupport {

    Object toObject(Object data);

    Data toData(Object object, PartitioningStrategy partitionStrategy);

    Data toData(Object object);

    MapContainer getMapContainer(String mapName);

    Map<String, MapContainer> getMapContainers();

    PartitionContainer getPartitionContainer(int partitionId);

    void initPartitionsContainers();

    /**
     * Removes all record stores inside the supplied partition ID matching with
     * the supplied predicate.
     *
     * @param predicate            to find partitions to be removed
     * @param partitionId          partition ID
     * @param onShutdown           {@code true} if this method is called during map service shutdown,
     *                             otherwise set {@code false}
     * @param onRecordStoreDestroy {@code true} if this method is called during to destroy record store,
     *                             otherwise set {@code false}
     * @see MapManagedService#reset()
     * @see MapManagedService#shutdown(boolean)
     */
    void removeRecordStoresFromPartitionMatchingWith(Predicate<RecordStore> predicate, int partitionId,
                                                     boolean onShutdown, boolean onRecordStoreDestroy);

    MapService getService();

    void destroyMapStores();

    void flushMaps();

    void destroyMap(String mapName);

    void reset();

    /**
     * Releases internal resources solely managed by Hazelcast.
     * This method is called when MapService is shutting down.
     */
    void shutdown();

    RecordStore createRecordStore(MapContainer mapContainer, int partitionId, MapKeyLoader keyLoader);

    RecordStore getRecordStore(int partitionId, String mapName);

    RecordStore getRecordStore(int partitionId, String mapName, boolean skipLoadingOnCreate);

    RecordStore getExistingRecordStore(int partitionId, String mapName);

    PartitionIdSet getOwnedPartitions();

    /**
     * Reloads the cached collection of partitions owned by this node.
     */
    void reloadOwnedPartitions();

    AtomicInteger getWriteBehindQueueItemCounter();

    ExpirationManager getExpirationManager();

    void setService(MapService mapService);

    NodeEngine getNodeEngine();

    MapEventPublisher getMapEventPublisher();

    MapEventJournal getEventJournal();

    QueryEngine getQueryEngine(String name);

    QueryRunner getMapQueryRunner(String name);

    QueryOptimizer getQueryOptimizer();

    LocalMapStatsProvider getLocalMapStatsProvider();

    MapClearExpiredRecordsTask getClearExpiredRecordsTask();

    MapOperationProvider getMapOperationProvider(String mapName);

    IndexProvider getIndexProvider(MapConfig mapConfig);

    Extractors getExtractors(String mapName);

    void incrementOperationStats(long startTime, LocalMapStatsImpl localMapStats, String mapName, Operation operation);

    boolean removeMapContainer(MapContainer mapContainer);

    PartitioningStrategy getPartitioningStrategy(String mapName, PartitioningStrategyConfig config);

    void removePartitioningStrategyFromCache(String mapName);

    PartitionContainer[] getPartitionContainers();

    void onClusterStateChange(ClusterState newState);

    ResultProcessorRegistry getResultProcessorRegistry();

    MapNearCacheManager getMapNearCacheManager();

    QueryCacheContext getQueryCacheContext();

    UUID addListenerAdapter(ListenerAdapter listenerAdaptor, EventFilter eventFilter, String mapName);

    UUID addLocalListenerAdapter(ListenerAdapter listenerAdaptor, String mapName);

    IndexCopyBehavior getIndexCopyBehavior();

    /**
     * Returns the collection of the {@link RecordStoreMutationObserver}s
     * for the given map's partition that need to be added in record
     * store construction time in order to ensure no {@link RecordStore}
     * mutations are missed.
     *
     * @param mapName     The name of the map
     * @param partitionId The partition
     * @return The collection of the observers
     */
    Collection<RecordStoreMutationObserver<Record>> createRecordStoreMutationObservers(String mapName, int partitionId);

    ValueComparator getValueComparatorOf(InMemoryFormat inMemoryFormat);
}
