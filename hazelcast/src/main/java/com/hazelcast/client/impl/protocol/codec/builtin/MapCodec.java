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

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.hazelcast.client.impl.protocol.ClientMessage.BEGIN_FRAME;
import static com.hazelcast.client.impl.protocol.ClientMessage.END_FRAME;
import static com.hazelcast.client.impl.protocol.ClientMessage.NULL_FRAME;
import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.nextFrameIsDataStructureEndFrame;
import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.nextFrameIsNullEndFrame;

public final class MapCodec {

    private MapCodec() {
    }

    public static <K, V> void encode(ClientMessage clientMessage, Map<K, V> map,
                                     BiConsumer<ClientMessage, K> encodeKeyFunc,
                                     BiConsumer<ClientMessage, V> encodeValueFunc) {
        clientMessage.add(BEGIN_FRAME);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            encodeKeyFunc.accept(clientMessage, entry.getKey());
            encodeValueFunc.accept(clientMessage, entry.getValue());
        }
        clientMessage.add(END_FRAME);
    }

    public static <K, V> void encodeNullable(ClientMessage clientMessage, Map<K, V> map,
                                             BiConsumer<ClientMessage, K> encodeKeyFunc,
                                             BiConsumer<ClientMessage, V> encodeValueFunc) {
        if (map == null) {
            clientMessage.add(NULL_FRAME);
        } else {
            encode(clientMessage, map, encodeKeyFunc, encodeValueFunc);
        }
    }

    public static <K, V> Map<K, V> decode(ListIterator<ClientMessage.Frame> iterator,
                                          Function<ListIterator<ClientMessage.Frame>, K> decodeKeyFunc,
                                          Function<ListIterator<ClientMessage.Frame>, V> decodeValueFunc) {
        Map<K, V> result = new HashMap<>();
        //begin frame, map
        iterator.next();
        while (!nextFrameIsDataStructureEndFrame(iterator)) {
            K key = decodeKeyFunc.apply(iterator);
            V value = decodeValueFunc.apply(iterator);
            result.put(key, value);
        }
        //end frame, map
        iterator.next();
        return result;
    }

    public static <K, V> Map<K, V> decodeNullable(ListIterator<ClientMessage.Frame> iterator,
                                                  Function<ListIterator<ClientMessage.Frame>, K> decodeKeyFunc,
                                                  Function<ListIterator<ClientMessage.Frame>, V> decodeValueFunc) {
        return nextFrameIsNullEndFrame(iterator) ? null : decode(iterator, decodeKeyFunc, decodeValueFunc);
    }
}
