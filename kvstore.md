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
        weight: 20
---

This component allows you to access to the key/value store of Consul.

# How to use

```yaml
consul:
  clients:
    consulName:
        host: base.url.to.consul
        port: port.consul

MyApp:
  # ${...} Contains the access key to the key store
  # `<consulName>` Contains the name of the Consul client to contact
  # `<key>` Corresponds to the Consul store key, a key `key/subKey` is written as follows `key.subKey`
  someStorageKey: ${consul.clients.<consulName>.store.<key>}
```


```java
public class SomeClass {
  @Configuration("MyApp.someStorageKey")
  private String remoteStorageKey;     
}
```

# Example

Configuration to retrieve value of `web/foo` key from a consul:

```yaml
consul:
  clients:
    consul1:
        host: localhost
        port: 8500

MyApp:
    someStorageKey: ${consul.clients.consul1.store.web.foo}
```

```java
public class SomeClass {
  @Configuration("MyApp.someStorageKey")
  private String remoteStorageKey;     
}
```

# Bonus

You can access to the store anytime:

```java
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

//...

@Inject
@Named("consulName")
private Consul remoteConsul; 

public void SomeFunction {
    KeyValueClient kvClient = remoteConsul.keyValueClient();

    kvClient.putValue("foo", "bar");
    String value = kvClient.getValueAsString("foo").get(); // bar
}
```
