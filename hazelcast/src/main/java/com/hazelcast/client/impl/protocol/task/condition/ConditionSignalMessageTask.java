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

package com.hazelcast.client.impl.protocol.task.condition;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.ConditionSignalCodec;
import com.hazelcast.client.impl.protocol.task.AbstractPartitionMessageTask;
import com.hazelcast.cp.internal.datastructures.unsafe.lock.InternalLockNamespace;
import com.hazelcast.cp.internal.datastructures.unsafe.lock.LockService;
import com.hazelcast.cp.internal.datastructures.unsafe.lock.operations.SignalOperation;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.security.permission.ActionConstants;
import com.hazelcast.security.permission.LockPermission;
import com.hazelcast.spi.impl.operationservice.Operation;

import java.security.Permission;

public class ConditionSignalMessageTask
        extends AbstractPartitionMessageTask<ConditionSignalCodec.RequestParameters> {

    public ConditionSignalMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection);
    }

    @Override
    protected Operation prepareOperation() {
        final Data key = serializationService.toData(parameters.lockName);
        final InternalLockNamespace namespace = new InternalLockNamespace(parameters.lockName);
        return new SignalOperation(namespace, key, parameters.threadId, parameters.name, false);
    }

    @Override
    protected ConditionSignalCodec.RequestParameters decodeClientMessage(ClientMessage clientMessage) {
        return ConditionSignalCodec.decodeRequest(clientMessage);
    }

    @Override
    protected ClientMessage encodeResponse(Object response) {
        return ConditionSignalCodec.encodeResponse();
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
        return parameters.lockName;
    }

    @Override
    public String getMethodName() {
        return "signal";
    }

    @Override
    public Object[] getParameters() {
        return null;
    }
}
