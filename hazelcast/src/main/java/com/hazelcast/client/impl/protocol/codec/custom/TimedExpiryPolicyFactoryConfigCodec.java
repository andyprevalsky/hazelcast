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

@Generated("32c218d143cf9724455de4858f77fa50")
public final class TimedExpiryPolicyFactoryConfigCodec {

    private TimedExpiryPolicyFactoryConfigCodec() {
    }

    public static void encode(ClientMessage clientMessage, com.hazelcast.config.CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig timedExpiryPolicyFactoryConfig) {
        clientMessage.add(BEGIN_FRAME);

        StringCodec.encode(clientMessage, timedExpiryPolicyFactoryConfig.getExpiryPolicyType());
        DurationConfigCodec.encode(clientMessage, timedExpiryPolicyFactoryConfig.getDurationConfig());

        clientMessage.add(END_FRAME);
    }

    public static com.hazelcast.config.CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig decode(ListIterator<ClientMessage.Frame> iterator) {
        // begin frame
        iterator.next();

        String expiryPolicyType = StringCodec.decode(iterator);
        com.hazelcast.config.CacheSimpleConfig.ExpiryPolicyFactoryConfig.DurationConfig durationConfig = DurationConfigCodec.decode(iterator);

        fastForwardToEndFrame(iterator);

        return CustomTypeFactory.createTimedExpiryPolicyFactoryConfig(expiryPolicyType, durationConfig);
    }
}
