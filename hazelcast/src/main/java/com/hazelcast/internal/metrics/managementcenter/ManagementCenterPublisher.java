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

package com.hazelcast.internal.metrics.managementcenter;

import com.hazelcast.internal.metrics.MetricsPublisher;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.LoggingService;

import javax.annotation.Nonnull;
import java.util.function.ObjLongConsumer;

/**
 * Renderer to serialize metrics to byte[] to be read by Management Center.
 * Additionally, it converts legacy metric names to {@code [metric=<oldName>]}.
 */
public class ManagementCenterPublisher implements MetricsPublisher {

    private final ILogger logger;
    private final ObjLongConsumer<byte[]> consumer;
    private final MetricsCompressor compressor;

    public ManagementCenterPublisher(@Nonnull LoggingService loggingService, @Nonnull ObjLongConsumer<byte[]> writeFn) {
        this.consumer = writeFn;
        this.logger = loggingService.getLogger(getClass());
        this.compressor = new MetricsCompressor();
    }

    @Override
    public String name() {
        return "Management Center Publisher";
    }

    @Override
    public void publishLong(String name, long value) {
        compressor.addLong(name, value);
    }

    @Override
    public void publishDouble(String name, double value) {
        compressor.addDouble(name, value);
    }

    @Override
    public void whenComplete() {
        int count = compressor.count();
        byte[] blob = compressor.getBlobAndReset();
        consumer.accept(blob, System.currentTimeMillis());
        logger.finest(String.format("Collected %,d metrics, %,d bytes", count, blob.length));
    }

    public int getCount() {
        return compressor.count();
    }
}
