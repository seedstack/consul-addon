/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul.internal;

import com.google.common.base.Optional;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.seedstack.coffig.spi.ConfigurationProvider;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

public class ConsulProvider implements ConfigurationProvider {		
	private final Map<String, Consul> sources = new HashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    
    @Override
    public MapNode provide() {
        MapNode mapNode = new MapNode();
        sources.forEach((consulName, consul) -> {
            mapNode.set("consul.clients", buildConsulFromSource(consulName, consul));
        });

        dirty.set(false);
        return mapNode;
    }
    
    private MapNode buildConsulFromSource(String consulName, Consul consul) {
        MapNode mapNode = new MapNode();
        KeyValueClient keyValueClient = consul.keyValueClient();
        // Get all Consul keys
        List<String> consulKeys = keyValueClient.getKeys("");

        consulKeys.forEach(entry -> {
            String consulStorage = consulName + ".store";
            mapNode.set(consulStorage, buildKeyFromSource(entry, consul, keyValueClient));
        });
    	return mapNode;
    }

    private MapNode buildKeyFromSource(String keyName, Consul consul, KeyValueClient keyValueClient) {
        MapNode mapNode = new MapNode();
        String nodeKey = StringUtils.replace(keyName, "/", ".");
        Optional<String> valueKey = keyValueClient.getValueAsString(keyName);
        ValueNode valueNode = (valueKey.isPresent()) ? new ValueNode(valueKey.get()) : new ValueNode("");
        mapNode.set(nodeKey, valueNode);
        return mapNode;
    }
    
    @Override
    public ConsulProvider fork() {
    	ConsulProvider fork = new ConsulProvider();
        fork.sources.putAll(sources);
        return fork;
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    public ConsulProvider addAllConsul(Map<String, Consul> keys) {
        if (keys == null) {
            throw new NullPointerException("key cannot be null");
        }

        this.sources.putAll(keys);
        dirty.set(true);
        return this;
    }

}
