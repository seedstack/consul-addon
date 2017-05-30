/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.it.AbstractSeedIT;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

import javax.inject.Inject;
import javax.inject.Named;

public class ConsulIT extends AbstractSeedIT {
    @Inject
    @Named("consul1")
    private Consul remoteConsul;

    @Configuration("MyApp.someStorageKey")
    private String remoteStorageKey;

    @Test
    public void consulIsInjectable() {
        Assertions.assertThat(remoteConsul).isNotNull();
    }
    
    @Test
    public void remote_agent_client() {
    	AgentClient agentClient = remoteConsul.agentClient();
    	Assertions.assertThat(agentClient).isNotNull();
    }
    
    @Test
    public void getConsulStorageConfig() {
    	Assertions.assertThat(remoteStorageKey).isEqualTo("foo");
    }
}
