/**
 * Copyright (c) 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;

import org.seedstack.consul.ConsulConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.Consul.Builder;
import com.orbitz.consul.util.bookend.ConsulBookend;

/**
 * This plugin manages clients used to access Consul instances.
 */
public class ConsulPlugin extends AbstractSeedPlugin {    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulPlugin.class);
    private final Map<String, Consul> consulClients = new HashMap<>();

    @Override
    public String name() {
        return "consul";
    }

    @Override
    public Object nativeUnitModule() {
        return new ConsulModule(consulClients);
    }
    
    @Override
    public InitState initialize(InitContext initContext) {
    	ConsulConfig consulConfig = getConfiguration(ConsulConfig.class);

        if (!consulConfig.getConsulClients().isEmpty()) {
            consulConfig.getConsulClients().forEach((consulClientName, consulClient) -> {
                LOGGER.info("Creating Consul client {} for remote instance at {}", consulClientName, consulClient.getHost());
                consulClients.put(consulClientName, buildRemoteConsul(consulClientName, consulClient));
            });
        } else {
            LOGGER.info("No Consul configured, Consul support disabled");
        }
        
        return InitState.INITIALIZED;
    }
    
    private Consul buildRemoteConsul(String consulClientName, ConsulConfig.ClientConfig consulClientConfig) {
    	Builder consulBuilder = Consul.builder();
    	
    	String host = consulClientConfig.getHost();    	
    	if (host != null && !host.isEmpty()) {
    		HostAndPort hostAndPort = HostAndPort.fromParts(host, consulClientConfig.getPort()); 
    		consulBuilder.withHostAndPort(hostAndPort);
    	} else {
    		consulBuilder.withUrl(consulClientConfig.getUrl());
    	}
    	
    	if (consulClientConfig.getAclToken() != null && !consulClientConfig.getAclToken().isEmpty()) {
    		consulBuilder.withAclToken(consulClientConfig.getAclToken());
    	}
    	
    	if (consulClientConfig.getPing() != null) {
    		consulBuilder.withPing(consulClientConfig.getPing());
    	}
    	
    	String username = consulClientConfig.getBasicAuth().get("username");
    	String password = consulClientConfig.getBasicAuth().get("password");
    	if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
    		consulBuilder.withBasicAuth(username, password);
    	}    	
    	
    	if (!consulClientConfig.getTimeoutMillis().isEmpty()) {
    		if (consulClientConfig.getTimeoutMillis().get("connect") != null 
    			&& !consulClientConfig.getTimeoutMillis().get("connect").isEmpty()) {
    			try {
    				Long timeout = Long.parseLong(consulClientConfig.getTimeoutMillis().get("connect"));
    				consulBuilder.withConnectTimeoutMillis(timeout);
    			} catch(NumberFormatException nfe) {
    				throw SeedException.createNew(ConsulErrorCode.NUMBER_FORMAT_EXCEPTION).put("number", consulClientConfig.getTimeoutMillis().get("connect"));
    			}
    		}
    		
    		if (consulClientConfig.getTimeoutMillis().get("read") != null 
        		&& !consulClientConfig.getTimeoutMillis().get("read").isEmpty()) {
        		try {
    				Long timeout = Long.parseLong(consulClientConfig.getTimeoutMillis().get("read"));
    				consulBuilder.withReadTimeoutMillis(timeout);
    			} catch(NumberFormatException nfe) {
    				throw SeedException.createNew(ConsulErrorCode.NUMBER_FORMAT_EXCEPTION).put("number", consulClientConfig.getTimeoutMillis().get("read"));
    			}
        	}
    		
    		if (consulClientConfig.getTimeoutMillis().get("write") != null 
        		&& !consulClientConfig.getTimeoutMillis().get("write").isEmpty()) {
    			try {
    				Long timeout = Long.parseLong(consulClientConfig.getTimeoutMillis().get("write"));
    				consulBuilder.withWriteTimeoutMillis(timeout);
    			} catch(NumberFormatException nfe) {
    				throw SeedException.createNew(ConsulErrorCode.NUMBER_FORMAT_EXCEPTION).put("number", consulClientConfig.getTimeoutMillis().get("write"));
    			}
        	}
    	}
    	
    	if (!consulClientConfig.getHeaders().isEmpty()) {
    		consulBuilder.withHeaders(consulClientConfig.getHeaders());
    	}
    	
    	if (consulClientConfig.getHostnameVerifier() != null) { 
    		HostnameVerifier hostnameVerifier = null;
			try {
				hostnameVerifier = consulClientConfig.getHostnameVerifier().newInstance();
			} catch (InstantiationException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_INSTANTIATE).put("class", consulClientConfig.getHostnameVerifier());
			} catch (IllegalAccessException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_ACCESSIBLE).put("class", consulClientConfig.getHostnameVerifier());
			}

    		consulBuilder.withHostnameVerifier(hostnameVerifier);
    	}
    	
    	if (consulClientConfig.getConsulBookend() != null) { 
    		ConsulBookend consulBookend = null;
			try {
				consulBookend = consulClientConfig.getConsulBookend().newInstance();
			} catch (InstantiationException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_INSTANTIATE).put("class", consulClientConfig.getConsulBookend());
			} catch (IllegalAccessException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_ACCESSIBLE).put("class", consulClientConfig.getConsulBookend());
			}

    		consulBuilder.withConsulBookend(consulBookend);
    	}
    	
    	if (consulClientConfig.getProxy() != null) { 
    		Proxy proxy = null;
			try {
				proxy = consulClientConfig.getProxy().newInstance();
			} catch (InstantiationException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_INSTANTIATE).put("class", consulClientConfig.getProxy());
			} catch (IllegalAccessException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_ACCESSIBLE).put("class", consulClientConfig.getProxy());
			}

    		consulBuilder.withProxy(proxy);
    	}    	
    	
    	if (consulClientConfig.getExecutorService() != null) { 
    		ExecutorService executorService = null;
			try {
				executorService = consulClientConfig.getExecutorService().newInstance();
			} catch (InstantiationException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_INSTANTIATE).put("class", consulClientConfig.getExecutorService());
			} catch (IllegalAccessException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_ACCESSIBLE).put("class", consulClientConfig.getExecutorService());
			}

    		consulBuilder.withExecutorService(executorService);
    	}
    	
    	if (consulClientConfig.getSslContext() != null) { 
    		SSLContext sslContext = null;
			try {
				sslContext = consulClientConfig.getSslContext().newInstance();
			} catch (InstantiationException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_INSTANTIATE).put("class", consulClientConfig.getSslContext());
			} catch (IllegalAccessException e) {
				throw SeedException.createNew(ConsulErrorCode.CLASS_NOT_ACCESSIBLE).put("class", consulClientConfig.getSslContext());
			}

    		consulBuilder.withSslContext(sslContext);
    	}
	   	
		Consul consul;		
    	try {
    	    consul = consulBuilder.build();
    	    LOGGER.info("Build Consul client " + consulClientName);
    	} catch (Exception e) {
    	    throw SeedException.createNew(ConsulErrorCode.CANNOT_CREATE_CLIENT).put("consulClientName", consulClientName);
    	}

    	return consul;    	
    }
    
    @Override
    public void stop() {
    	consulClients.forEach((consulClientName, value) -> {
            LOGGER.info("Closing Consul client {}", consulClientName);
            try {
                value.destroy();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close Consul client %s", consulClientName), e);
            }
        });
    }    
}
