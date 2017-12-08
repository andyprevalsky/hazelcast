/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.map.impl.recordstore;

import com.hazelcast.concurrent.lock.LockService;
import com.hazelcast.concurrent.lock.LockStore;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.map.impl.EntryCostEstimator;
import com.hazelcast.map.impl.MapContainer;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.journal.MapEventJournal;
import com.hazelcast.map.impl.mapstore.MapDataStore;
import com.hazelcast.map.impl.mapstore.MapStoreContext;
import com.hazelcast.map.impl.record.Record;
import com.hazelcast.map.impl.record.RecordComparator;
import com.hazelcast.map.impl.record.RecordFactory;
import com.hazelcast.map.impl.record.Records;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.query.impl.Indexes;
import com.hazelcast.query.impl.QueryableEntry;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.serialization.SerializationService;
import com.hazelcast.util.Clock;

import java.util.Collection;

import static com.hazelcast.map.impl.ExpirationTimeSetter.calculateMaxIdleMillis;
import static com.hazelcast.map.impl.ExpirationTimeSetter.calculateTTLMillis;
import static com.hazelcast.map.impl.ExpirationTimeSetter.pickTTL;
import static com.hazelcast.map.impl.ExpirationTimeSetter.setExpirationTime;


/**
 * Contains record store common parts.
 */
abstract class AbstractRecordStore implements RecordStore<Record> {

    protected final String name;
    protected final MapContainer mapContainer;
    protected final int partitionId;
    protected final MapServiceContext mapServiceContext;
    protected final SerializationService serializationService;
    protected final InMemoryFormat inMemoryFormat;
    protected final RecordFactory recordFactory;
    protected final RecordComparator recordComparator;
    protected final MapStoreContext mapStoreContext;
    protected final MapDataStore<Data, Object> mapDataStore;
    protected final LockStore lockStore;
    protected final MapEventJournal eventJournal;

    protected Storage<Data, Record> storage;

    private long hits;
    private long lastAccess;
    private long lastUpdate;

    protected AbstractRecordStore(MapContainer mapContainer, int partitionId) {
        this.name = mapContainer.getName();
        this.mapContainer = mapContainer;
        this.partitionId = partitionId;
        this.mapServiceContext = mapContainer.getMapServiceContext();
        this.serializationService = mapServiceContext.getNodeEngine().getSerializationService();
        this.inMemoryFormat = mapContainer.getMapConfig().getInMemoryFormat();
        this.recordFactory = mapContainer.getRecordFactoryConstructor().createNew(null);
        this.recordComparator = mapServiceContext.getRecordComparator(inMemoryFormat);
        this.mapStoreContext = mapContainer.getMapStoreContext();
        this.mapDataStore = mapStoreContext.getMapStoreManager().getMapDataStore(name, partitionId);
        this.lockStore = createLockStore();
        this.eventJournal = mapServiceContext.getEventJournal();
    }

    @Override
    public void init() {
        this.storage = createStorage(recordFactory, inMemoryFormat);
    }

    @Override
    public Record createRecord(Object value, long ttlMillis, long now) {
        MapConfig mapConfig = mapContainer.getMapConfig();
        Record record = recordFactory.newRecord(value);
        record.setCreationTime(now);
        record.setLastUpdateTime(now);
        long ttlMillisFromConfig = calculateTTLMillis(mapConfig);
        long ttl = pickTTL(ttlMillis, ttlMillisFromConfig);
        record.setTtl(ttl);

        long maxIdleMillis = calculateMaxIdleMillis(mapConfig);
        setExpirationTime(record, maxIdleMillis);
        updateStatsOnPut(true, now);
        return record;
    }

    @Override
    public Storage createStorage(RecordFactory recordFactory, InMemoryFormat memoryFormat) {
        return new StorageImpl(recordFactory, memoryFormat, serializationService);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MapContainer getMapContainer() {
        return mapContainer;
    }

    @Override
    public long getOwnedEntryCost() {
        return storage.getEntryCostEstimator().getEstimate();
    }

    protected long getNow() {
        return Clock.currentTimeMillis();
    }

    protected void updateRecord(Data key, Record record, Object value, long now) {
        updateStatsOnPut(false, now);
        record.onUpdate(now);
        eventJournal.writeUpdateEvent(mapContainer.getEventJournalConfig(), mapContainer.getObjectNamespace(), partitionId,
                record.getKey(), record.getValue(), value);
        storage.updateRecordValue(key, record, value);
    }

    @Override
    public int getPartitionId() {
        return partitionId;
    }

    protected void saveIndex(Record record, Object oldValue) {
        Data dataKey = record.getKey();
        Indexes indexes = mapContainer.getIndexes(partitionId);
        if (indexes.hasIndex()) {
            Object value = Records.getValueOrCachedValue(record, serializationService);
            QueryableEntry queryableEntry = mapContainer.newQueryEntry(dataKey, value);
            indexes.saveEntryIndex(queryableEntry, oldValue);
        }
    }


    protected void removeIndex(Record record) {
        Indexes indexes = mapContainer.getIndexes(partitionId);
        if (indexes.hasIndex()) {
            Data key = record.getKey();
            Object value = Records.getValueOrCachedValue(record, serializationService);
            indexes.removeEntryIndex(key, value);
        }
    }

    protected void removeIndex(Collection<Record> records) {
        Indexes indexes = mapContainer.getIndexes(partitionId);
        if (!indexes.hasIndex()) {
            return;
        }

        for (Record record : records) {
            removeIndex(record);
        }
    }

    protected LockStore createLockStore() {
        NodeEngine nodeEngine = mapServiceContext.getNodeEngine();
        LockService lockService = nodeEngine.getSharedService(LockService.SERVICE_NAME);
        if (lockService == null) {
            return null;
        }
        return lockService.createLockStore(partitionId, MapService.getObjectNamespace(name));
    }

    public int getLockedEntryCount() {
        return lockStore.getLockedEntryCount();
    }

    protected RecordStoreLoader createRecordStoreLoader(MapStoreContext mapStoreContext) {
        return mapStoreContext.getMapStoreWrapper() == null
                ? RecordStoreLoader.EMPTY_LOADER : new BasicRecordStoreLoader(this);
    }

    protected Data toData(Object value) {
        return mapServiceContext.toData(value);
    }

    public void setSizeEstimator(EntryCostEstimator entryCostEstimator) {
        this.storage.setEntryCostEstimator(entryCostEstimator);
    }

    @Override
    public void disposeDeferredBlocks() {
        storage.disposeDeferredBlocks();
    }

    public Storage<Data, ? extends Record> getStorage() {
        return storage;
    }

    @Override
    public long getHits() {
        return hits;
    }

    @Override
    public long getLastAccessTime() {
        return lastAccess;
    }

    @Override
    public long getLastUpdateTime() {
        return lastUpdate;
    }

    @Override
    public void increaseHits() {
        this.hits++;
    }

    @Override
    public void increaseHits(long hits) {
        this.hits += hits;
    }

    @Override
    public void decreaseHits(long hits) {
        this.hits -= hits;
    }

    @Override
    public void setLastAccessTime(long time) {
        this.lastAccess = Math.max(this.lastAccess, time);
    }

    @Override
    public void setLastUpdateTime(long time) {
        this.lastUpdate = Math.max(this.lastUpdate, time);
    }


    protected void updateStatsOnPut(boolean newRecord, long now) {
        setLastUpdateTime(now);

        if (!newRecord) {
            updateStatsOnGet(now);
        }
    }

    protected void updateStatsOnPut(long hits) {
        increaseHits(hits);
    }

    protected void updateStatsOnGet(long now) {
        setLastAccessTime(now);
        increaseHits();
    }

    protected void resetStats() {
        this.hits = 0;
        this.lastAccess = 0;
        this.lastUpdate = 0;
    }
}
