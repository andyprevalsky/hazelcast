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

package com.hazelcast.security;

import java.util.Properties;

/**
 * ICredentialsFactory is used to create {@link Credentials} objects to be used
 * during node authentication before connection is accepted by the master node.
 */
public interface ICredentialsFactory {

    /**
     * Configures {@link ICredentialsFactory}.
     *
     * @param clusterName the cluster's name
     * @param clusterPassword the cluster's password
     * @param properties  properties that will be used to pass custom configurations by user
     */
    void configure(String clusterName, String clusterPassword, Properties properties);

    /**
     * Creates new {@link Credentials} object.
     *
     * @return the new Credentials object
     */
    Credentials newCredentials();

    /**
     * Destroys {@link ICredentialsFactory}.
     */
    void destroy();
}
