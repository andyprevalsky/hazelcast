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

package com.hazelcast.client.impl.protocol.codec.custom;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;

import java.util.ListIterator;

import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.fastForwardToEndFrame;
import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

@Generated("d069f93d699ea31d86c5bb9568684eee")
public final class QueryCacheEventDataCodec {
    private static final int SEQUENCE_FIELD_OFFSET = 0;
    private static final int EVENT_TYPE_FIELD_OFFSET = SEQUENCE_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int PARTITION_ID_FIELD_OFFSET = EVENT_TYPE_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int INITIAL_FRAME_SIZE = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;

    private QueryCacheEventDataCodec() {
    }

    public static void encode(ClientMessage clientMessage, com.hazelcast.map.impl.querycache.event.QueryCacheEventData queryCacheEventData) {
        clientMessage.add(BEGIN_FRAME);

        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[INITIAL_FRAME_SIZE]);
        encodeLong(initialFrame.content, SEQUENCE_FIELD_OFFSET, queryCacheEventData.getSequence());
        encodeInt(initialFrame.content, EVENT_TYPE_FIELD_OFFSET, queryCacheEventData.getEventType());
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, queryCacheEventData.getPartitionId());
        clientMessage.add(initialFrame);

        CodecUtil.encodeNullable(clientMessage, queryCacheEventData.getDataKey(), DataCodec::encode);
        CodecUtil.encodeNullable(clientMessage, queryCacheEventData.getDataNewValue(), DataCodec::encode);

        clientMessage.add(END_FRAME);
    }

    public static com.hazelcast.map.impl.querycache.event.DefaultQueryCacheEventData decode(ListIterator<ClientMessage.Frame> iterator) {
        // begin frame
        iterator.next();

        ClientMessage.Frame initialFrame = iterator.next();
        long sequence = decodeLong(initialFrame.content, SEQUENCE_FIELD_OFFSET);
        int eventType = decodeInt(initialFrame.content, EVENT_TYPE_FIELD_OFFSET);
        int partitionId = decodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET);

        com.hazelcast.nio.serialization.Data dataKey = CodecUtil.decodeNullable(iterator, DataCodec::decode);
        com.hazelcast.nio.serialization.Data dataNewValue = CodecUtil.decodeNullable(iterator, DataCodec::decode);

        fastForwardToEndFrame(iterator);

        return CustomTypeFactory.createQueryCacheEventData(dataKey, dataNewValue, sequence, eventType, partitionId);
    }
}
