/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.util.bookend.ConsulBookend;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import javax.net.ssl.HostnameVerifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Config("consul")
public class ConsulConfig {
    private Map<String, ClientConfig> clients = new HashMap<>();

    public Map<String, ClientConfig> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    public void addClient(String name, ClientConfig config) {
        clients.put(name, config);
    }

    public static class ClientConfig {
        @SingleValue
        private String url;
        private HostAndPort host;
        private String aclToken;
        private boolean ping = true;
        private String user;
        private String password;
        private TimeoutConfig timeouts = new TimeoutConfig();
        private Class<? extends ConsulBookend> consulBookend;
        private Class<? extends ExecutorService> executorService;
        private Class<? extends HostnameVerifier> hostnameVerifier;
        private Map<String, String> headers = new HashMap<>();

        public String getUrl() {
            return url;
        }

        public ClientConfig setUrl(String url) {
            this.url = url;
            return this;
        }

        public HostAndPort getHost() {
            return host;
        }

        public ClientConfig setHost(HostAndPort host) {
            this.host = host;
            return this;
        }

        public String getAclToken() {
            return aclToken;
        }

        public ClientConfig setAclToken(String aclToken) {
            this.aclToken = aclToken;
            return this;
        }

        public boolean isPing() {
            return ping;
        }

        public ClientConfig setPing(boolean ping) {
            this.ping = ping;
            return this;
        }

        public String getUser() {
            return user;
        }

        public ClientConfig setUser(String user) {
            this.user = user;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public ClientConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        public TimeoutConfig getTimeouts() {
            return timeouts;
        }

        public ClientConfig setTimeouts(TimeoutConfig timeouts) {
            this.timeouts = timeouts;
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

        public Class<? extends HostnameVerifier> getHostnameVerifier() {
            return hostnameVerifier;
        }

        public ClientConfig setHostnameVerifier(Class<? extends HostnameVerifier> hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Map<String, String> getHeaders() {
            return Collections.unmodifiableMap(headers);
        }

        public ClientConfig addHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public static class TimeoutConfig {
            private Long connect;
            private Long read;
            private Long write;

            public Long getConnect() {
                return connect;
            }

            public TimeoutConfig setConnect(Long connect) {
                this.connect = connect;
                return this;
            }

            public Long getRead() {
                return read;
            }

            public TimeoutConfig setRead(Long read) {
                this.read = read;
                return this;
            }

            public Long getWrite() {
                return write;
            }

            public TimeoutConfig setWrite(Long write) {
                this.write = write;
                return this;
            }
        }
    }
}
