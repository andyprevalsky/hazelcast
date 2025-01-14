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

package com.hazelcast.client.impl.protocol.codec.builtin;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.custom.ScheduledTaskHandlerCodec;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public final class ListScheduledTaskHandlerCodec {

    private ListScheduledTaskHandlerCodec() {
    }

    public static void encode(ClientMessage clientMessage, Collection<ScheduledTaskHandler> collection) {
        ListMultiFrameCodec.encode(clientMessage, collection, ScheduledTaskHandlerCodec::encode);
    }

    public static List<ScheduledTaskHandler> decode(ListIterator<ClientMessage.Frame> iterator) {
        return ListMultiFrameCodec.decode(iterator, ScheduledTaskHandlerCodec::decode);
    }
}
