/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul;

import org.seedstack.coffig.Config;

import com.orbitz.consul.util.bookend.ConsulBookend;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

@Config("consul")
public class ConsulConfig {
	private static final int DEFAULT_CONSUL_PORT = 8500;
	private Map<String, ClientConfig> clients = new HashMap<>();

    public Map<String, ClientConfig> getConsulClients() {
        return Collections.unmodifiableMap(clients);
    }

    public void addConsulClient(String name, ClientConfig config) {
        clients.put(name, config);
    }

    public static class ClientConfig {
        private String host = new String();
        private int port = DEFAULT_CONSUL_PORT;        
        private String url;
        private String aclToken;
        private Map<String, String> timeoutMillis = new HashMap<String, String>();
        private Map<String, String> basicAuth = new HashMap<String, String>();
        private Boolean ping;
        private Class<? extends ConsulBookend> consulBookend;
        private Class<? extends ExecutorService> executorService;
        private Class<? extends HostnameVerifier> hostnameVerifier;
        private Class<? extends Proxy> proxy;
        private Class<? extends SSLContext> sslContext;
        private Map<String, String> headers = new HashMap<String, String>();
        
        public String getHost() {
            return host;
        }

		public String getUrl() {
			return url;
		}

		public ClientConfig setUrl(String url) {
			this.url = url;
			return this;
		}

		public Class<? extends HostnameVerifier> getHostnameVerifier() {
			return hostnameVerifier;
		}

		public ClientConfig setHostnameVerifier(Class<? extends HostnameVerifier> hostnameVerifier) {
			this.hostnameVerifier = hostnameVerifier;
			return this;
		}

		public Class<? extends SSLContext> getSslContext() {
			return sslContext;
		}

		public ClientConfig setSslContext(Class<? extends SSLContext> sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		public String getAclToken() {
			return aclToken;
		}

		public ClientConfig setAclToken(String aclToken) {
			this.aclToken = aclToken;
			return this;
		}

		public Map<String, String> getTimeoutMillis() {
			return timeoutMillis;
		}

		public ClientConfig setTimeoutMillis(Map<String, String> timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
			return this;
		}

		public Map<String, String> getBasicAuth() {
			return basicAuth;
		}

		public ClientConfig setBasicAuth(Map<String, String> basicAuth) {
			this.basicAuth = basicAuth;
			return this;
		}

		public Boolean getPing() {
			return ping;
		}

		public ClientConfig setPing(Boolean ping) {
			this.ping = ping;
			return this;
		}

		public Class<? extends ConsulBookend> getConsulBookend() {
			return consulBookend;
		}

		public ClientConfig setConsulBookend(Class<? extends ConsulBookend> consulBookend) {
			this.consulBookend = consulBookend;
			return this;
		}

		public Class<? extends ExecutorService> getExecutorService() {
			return executorService;
		}

		public ClientConfig setExecutorService(Class<? extends ExecutorService> executorService) {
			this.executorService = executorService;
			return this;
		}

		public Class<? extends Proxy> getProxy() {
			return proxy;
		}

		public ClientConfig setProxy(Class<? extends Proxy> proxy) {
			this.proxy = proxy;
			return this;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public ClientConfig setHeaders(Map<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public ClientConfig addHost(String host) {
            this.host = host;
            return this;
        }		

        public int getPort() {
			return port;
		}

		public ClientConfig setPort(int port) {
			this.port = port;
			return this;
		}
    }
}
