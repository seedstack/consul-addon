---
title: "Consul"
addon: "Consul"
repo: "https://github.com/seedstack/consul-addon"
author: Adrien LAUER
description: "Provides integration with HashiCorp Consul service discovery and distributed configuration."
tags:
    - micro-service
    - configuration
zones:
    - Addons
noMenu: true    
---

The Consul add-on allows you to configure, inject and use Java Consul clients.<!--more-->

{{< dependency g="org.seedstack.addons.consul" a="consul" >}}

{{% callout info %}}
For more information on Consul HTTP API, see [https://www.consul.io/api/index.html](https://www.consul.io/api/index.html).
This add-on uses the [official Java client](https://github.com/OrbitzWorldwide/consul-client) which is a thin wrapper around the HTTP API.
{{% /callout %}}

## Configuration

To access a Consul server, you need to declare a client in configuration:

```yaml
consul:
  clients:
    consulName:
      # URL of the Consul server 
      url: (URL)
      
      # Instead of the URL, you can specify the host and port (host:port) of the Consul server
      host: (HostAndPort)
      
      # The token used for access control
      aclToken: (String)
      
      # If true (default value), a ping will be attempted on startup
      ping: (boolean)
      
      # The user name for basic authentication
      username: value
      
      # The password for basic authentication
      password: value
      
      # Timeout values in milliseconds for HTTP requests
      timeoutMillis:
        # Connection timeout
        connect: (int)
        
        # Read timeout
        read: (int)
        
        # Write timeout
        write: (int)
        
      # The class used to verify the hostname of the Consul server
      hostnameVerifier: (Class<? extends javax.net.ssl.HostnameVerifier>)
      
      # The consul bookend to be used
      consulBookend: (Class<? extends com.orbitz.consul.util.bookend.ConsulBookend>)
      
      # The executor service to be used
      executorService: (Class<? extends java.util.concurrent.ExecutorService>)
      
      # HTTP headers added to outgoing requests with the name of the header as key  
      headers:
        headerName: (String)
```

{{% callout info %}}
* SeedStack proxy configuration will automatically be used if any.
* [SeedStack SSL configuration]({{<ref "docs/core/crypto.md#ssl" >}}) will be automatically used if any. 
{{% /callout %}}

## Usage

### Consul API

To use a configured Consul client, simply inject it with its configured name:

```java
import javax.inject.Inject;
import javax.inject.name.Named;
import com.orbitz.consul.Consul;

public class SomeClass {
  @Inject
  @Named("someConsul")
  private Consul remoteConsul;
}
```

{{% callout info %}}
You can find more example about the Java API [here](https://github.com/OrbitzWorldwide/consul-client).
{{% /callout %}}

### Key/value store through configuration

The key/value store of each Consul client is accessible programmatically using the Consul API, but you can also access
its values at the `consul.clients.<clientName>.store` tree node: 

```yaml
consul:
  clients:
    consul1:
        host: localhost
        port: 8500
        # The key/value store is accessible through this node
        store: ...
```

Therefore, you can access any consul key/store value, by using [configuration macros]({{< ref "docs/core/configuration.md#Macros" >}}):

```yaml
consul:
  clients:
    consul1:
        host: localhost
        port: 8500

myAppConfig:
    someKey: ${consul.clients.consul1.store.some.key}
```

You can then inject the configuration value into your code like this:

```java
public class SomeClass {
  @Configuration("myAppConfig.someKey")
  private String someKey;     
}
```

{{% callout tips %}}
The consul storage key can also be referenced directly but using a macro allows to isolate your code from the origin of the
key.
{{% /callout %}}

## Example

Configuration for a Consul server running on the same machine:

```yaml
consul:
  clients:
    someConsul:
        host: localhost
        port: 8500
```

This client is used like this:

```java
import javax.inject.Inject;
import javax.inject.name.Named;
import com.orbitz.consul.Consul;

public class SomeClass {
    @Inject
    @Named("someConsul")
    private Consul someConsul;
}
```

As any other operation, registering a service is done through the native Consul API:

```java
public class SomeClass {
    @Inject
    @Named("someConsul")
    private Consul someConsul;

    public void someMethod() {
        AgentClient agentClient = someConsul.agentClient();
        // registers with a TTL of 3 seconds
        agentClient.register(8080, 3L, "testService", "uniqueId");
        // Check
        agentClient.pass(serviceId);    
    }
}
```
