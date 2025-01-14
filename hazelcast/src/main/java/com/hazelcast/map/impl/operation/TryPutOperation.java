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

package com.hazelcast.map.impl.operation;

import com.hazelcast.map.impl.MapDataSerializerHook;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.impl.operationservice.MutatingOperation;

import static com.hazelcast.map.impl.recordstore.RecordStore.DEFAULT_MAX_IDLE;
import static com.hazelcast.map.impl.recordstore.RecordStore.DEFAULT_TTL;

public class TryPutOperation extends BasePutOperation implements MutatingOperation {

    public TryPutOperation() {
    }

    public TryPutOperation(String name, Data dataKey, Data value, long timeout) {
        super(name, dataKey, value);
        setWaitTimeout(timeout);
    }

    @Override
    protected void runInternal() {
        recordStore.put(dataKey, dataValue, DEFAULT_TTL, DEFAULT_MAX_IDLE);
    }

    @Override
    public boolean shouldBackup() {
        return recordStore.getRecord(dataKey) != null;
    }

    @Override
    public void onWaitExpire() {
        sendResponse(false);
    }

    @Override
    public Object getResponse() {
        return true;
    }

    @Override
    public int getClassId() {
        return MapDataSerializerHook.TRY_PUT;
    }
}
