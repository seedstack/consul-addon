---
title: "Consul"
repo: "https://github.com/seedstack/consul-addon"
author: Adrien LAUER
description: "Provides integration with HashiCorp Consul service discovery and distributed configuration."
tags:
    - micro-service
    - configuration
zones:
    - Addons
menu:
    AddonConsul:
        weight: 10
---

The Consul add-on allows you to configure, inject and use Consul clients.

{{< dependency g="org.seedstack.addons.consul" a="consul" >}}

{{% callout info %}}
For more information on Consul API: [https://www.consul.io/api/index.html](https://www.consul.io/api/index.html)
{{% /callout %}}

# Configuration

To access a Consul, you need to declare a client in configuration, and in its basic form is:

```yaml
consul:
  clients:
    consulName:
        host: base.url.to.consul
        port: port.consul
```

With all the options, the configuration file looks like this:

```yaml
consul:
  clients:
    consulName:
        host: base.url.to.consul
        port: port.consul
        url: http://url.to.consul:port
        aclToken: value
        ping: true
        timeoutMillis:
            connect:
            read:
            write:
        basicAuth:
            username: value
            password: value
        headers:
            propertyName1: value1
        proxy: org.mycompany.myapp.Proxy      
        hostnameVerifier: org.mycompany.myapp.HostnameVerifier
        consulBookend: org.mycompany.myapp.ConsulBookend
        executorService: org.mycompany.myapp.ExecutorService
        sslContext: org.mycompany.myapp.SslContext
```

`consulName` is the name you give to the remote consul.
`url` is optional and is only used if the `host` is not specified. 'aclToken' is used to control access to data and APIs. `ping` attempts a ping before before returning the Consul instance, the default value is true. `timeoutMillis` defines the timeout for `connect`, `write` and `read` on HTTP calls in milliseconds. `basicAuth` defines the credentials used for basic Authentication. `headers` is a list of http properties.
`proxy` takes a fullly qualified class name and sets a proxy for the client. The proxy must extends java.net.Proxy. `hostnameVerifier` takes a fullly qualified class name. The hostname must extends javax.net.ssl.HostnameVerifier. `consulBookend` takes a fully qualified class name. The consulBookend must extends com.orbitz.consul.util.bookend.ConsulBookend. `executorService` takes a fullly qualified class name. The executorService must extends java.util.concurrent.ExecutorService. `sslContext` takes a fullly qualified class name and sets the SSL contexts for HTTPS agents. The sslContext must extends javax.net.ssl.SSLContext.

# How to use

To use a configured Consul client, simply inject it where it needed:

  ```java
  public class SomeClass {
      @Inject
      @Named("consulName")
      private Consul remoteConsul;
  }
  ```

  {{% callout info %}}
  You can find more example about the Java API: [here](https://github.com/OrbitzWorldwide/consul-client)
  {{% /callout %}}

# Example

Configuration for a Consul server running on the same machine:

```yaml
consul:
  clients:
    consul1:
        host: localhost
        port: 8500
```

This client is used like this:
```java
public class SomeClass {
    @Inject
    @Named("consul1")
    private Consul remoteConsul;
}
```

And now, you can register a service:
```java
public void SomeFunction {
    AgentClient agentClient = remoteConsul.agentClient();
    String serviceName = "testService";
    String serviceId = "uniqueId";
    String servicePort = 8080;

    agentClient.register(servicePort, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
    agentClient.pass(serviceId);    // Check
}
```
