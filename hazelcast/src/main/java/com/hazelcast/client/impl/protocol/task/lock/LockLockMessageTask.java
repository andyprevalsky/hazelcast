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

package com.hazelcast.client.impl.protocol.task.lock;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.LockLockCodec;
import com.hazelcast.client.impl.protocol.task.AbstractPartitionMessageTask;
import com.hazelcast.cp.internal.datastructures.unsafe.lock.InternalLockNamespace;
import com.hazelcast.cp.internal.datastructures.unsafe.lock.LockService;
import com.hazelcast.cp.internal.datastructures.unsafe.lock.operations.LockOperation;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.partition.strategy.StringPartitioningStrategy;
import com.hazelcast.security.permission.ActionConstants;
import com.hazelcast.security.permission.LockPermission;
import com.hazelcast.spi.impl.operationservice.Operation;

import java.security.Permission;
import java.util.concurrent.TimeUnit;

public class LockLockMessageTask
        extends AbstractPartitionMessageTask<LockLockCodec.RequestParameters> {

    public LockLockMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection);
    }

    @Override
    protected Operation prepareOperation() {
        Data key = serializationService.toData(parameters.name, StringPartitioningStrategy.INSTANCE);
        return new LockOperation(new InternalLockNamespace(parameters.name)
                , key, parameters.threadId, parameters.leaseTime, -1, parameters.referenceId);
    }

    @Override
    protected LockLockCodec.RequestParameters decodeClientMessage(ClientMessage clientMessage) {
        return LockLockCodec.decodeRequest(clientMessage);
    }

    @Override
    protected ClientMessage encodeResponse(Object response) {
        return LockLockCodec.encodeResponse();
    }

    @Override
    public String getServiceName() {
        return LockService.SERVICE_NAME;
    }

    @Override
    public Permission getRequiredPermission() {
        return new LockPermission(parameters.name, ActionConstants.ACTION_LOCK);
    }

    @Override
    public String getDistributedObjectName() {
        return parameters.name;
    }

    @Override
    public String getMethodName() {
        return "lock";
    }

    @Override
    public Object[] getParameters() {
        if (parameters.leaseTime == -1) {
            return null;
        }
        return new Object[]{parameters.leaseTime, TimeUnit.MILLISECONDS};
    }
}

