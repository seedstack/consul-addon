/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul.internal;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.orbitz.consul.Consul;

import java.util.Map;

class ConsulModule extends AbstractModule {
    private final Map<String, Consul> consulWrappers;

    ConsulModule(Map<String, Consul> consulWrappers) {
        this.consulWrappers = consulWrappers;
    }

    @Override
    protected void configure() {
        consulWrappers.forEach((consulClientName, consulClient) -> bind(Consul.class)
                .annotatedWith(Names.named(consulClientName))
                .toInstance(consulClient)
        );
    }
}
