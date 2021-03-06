/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul.internal;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.Consul.Builder;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.seedstack.consul.ConsulConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.crypto.CryptoPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This plugin manages clients used to access Consul instances.
 */
public class ConsulPlugin extends AbstractSeedPlugin {
    private static final int DEFAULT_CONSUL_PORT = 8500;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulPlugin.class);
    private Map<String, Consul> consulClients = new HashMap<>();
    private SeedRuntime seedRuntime;

    @Override
    public String name() {
        return "consul";
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        this.seedRuntime = seedRuntime;
    }

    @Override
    protected Collection<Class<?>> dependencies() {
        return Lists.newArrayList(CryptoPlugin.class);
    }

    @Override
    public InitState initialize(InitContext initContext) {
        ConsulConfig consulConfig = getConfiguration(ConsulConfig.class);

        Map<String, ConsulConfig.ClientConfig> clients = consulConfig.getClients();
        if (!clients.isEmpty()) {
            clients.forEach((consulClientName, consulClientConfig) -> {
                LOGGER.info("Creating Consul client {} for remote instance at {}", consulClientName, Optional.ofNullable(consulClientConfig.getUrl()).orElse(consulClientConfig.getHost().toString()));
                consulClients.put(consulClientName, buildRemoteConsul(consulClientName, consulClientConfig, initContext.dependency(CryptoPlugin.class)));
            });
        } else {
            LOGGER.info("No Consul configured, Consul support disabled");
        }

        configureConsulProvider();

        return InitState.INITIALIZED;
    }

    private void configureConsulProvider() {
        ConsulProvider consulProvider = new ConsulProvider();
        Optional.ofNullable(consulClients).ifPresent(consulProvider::addAllConsul);

        seedRuntime.registerConfigurationProvider(
                consulProvider,
                Integer.MAX_VALUE
        );
    }

    @Override
    public Object nativeUnitModule() {
        return new ConsulModule(consulClients);
    }

    @Override
    public void stop() {
        consulClients.forEach((name, client) -> {
            LOGGER.info("Closing Consul client {}", name);
            try {
                client.destroy();
            } catch (Exception e) {
                LOGGER.error("Unable to properly close Consul client {}", name, e);
            }
        });
    }

    private Consul buildRemoteConsul(String consulClientName, ConsulConfig.ClientConfig consulClientConfig, CryptoPlugin cryptoPlugin) {
        Builder consulBuilder = Consul.builder();

        // Base options
        String url = consulClientConfig.getUrl();
        HostAndPort host = consulClientConfig.getHost();
        if (!Strings.isNullOrEmpty(url)) {
            consulBuilder.withUrl(consulClientConfig.getUrl());
        } else if (host != null) {
            consulBuilder.withHostAndPort(host.withDefaultPort(DEFAULT_CONSUL_PORT));
        } else {
            throw SeedException.createNew(ConsulErrorCode.NO_URL_OR_HOST_SPECIFIED)
                    .put("consulClientName", consulClientName);
        }
        Optional.ofNullable(consulClientConfig.getAclToken()).filter(s -> !s.isEmpty()).ifPresent(consulBuilder::withAclToken);
        consulBuilder.withPing(consulClientConfig.isPing());

        // Credentials
        String username = consulClientConfig.getUser();
        String password = consulClientConfig.getPassword();
        if (!Strings.isNullOrEmpty(username) && password != null) {
            consulBuilder.withBasicAuth(username, password);
        }

        // Timeouts
        ConsulConfig.ClientConfig.TimeoutConfig timeouts = consulClientConfig.getTimeouts();
        Optional.ofNullable(timeouts.getConnect()).ifPresent(consulBuilder::withConnectTimeoutMillis);
        Optional.ofNullable(timeouts.getRead()).ifPresent(consulBuilder::withReadTimeoutMillis);
        Optional.ofNullable(timeouts.getWrite()).ifPresent(consulBuilder::withWriteTimeoutMillis);

        // Custom classes
        Optional.ofNullable(consulClientConfig.getHostnameVerifier()).map(this::instantiateClass).ifPresent(consulBuilder::withHostnameVerifier);
        Optional.ofNullable(consulClientConfig.getConsulBookend()).map(this::instantiateClass).ifPresent(consulBuilder::withConsulBookend);
        Optional.ofNullable(consulClientConfig.getExecutorService()).map(this::instantiateClass).ifPresent(consulBuilder::withExecutorService);

        // Proxy (consulBuilder::withProxy) is already handled by JVM-wide proxy configured in seed-core

        // SSL
        cryptoPlugin.sslContext().ifPresent(consulBuilder::withSslContext);

        // HTTP headers
        consulBuilder.withHeaders(consulClientConfig.getHeaders());

        Consul consul;
        try {
            consul = consulBuilder.build();
            LOGGER.info("Building Consul client {} for remote instance at {}", consulClientName, !Strings.isNullOrEmpty(url) ? url : host);
        } catch (Exception e) {
            throw SeedException.createNew(ConsulErrorCode.CANNOT_CREATE_CLIENT).put("consulClientName", consulClientName);
        }

        return consul;
    }

    private <T> T instantiateClass(Class<T> someClass) {
        try {
            return someClass.newInstance();
        } catch (Exception e) {
            throw SeedException.createNew(ConsulErrorCode.CANNOT_INSTANTIATE_CLASS).put("class", someClass);
        }
    }
}
