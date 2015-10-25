# netlight-client

Built on top of [Netty Project](https://github.com/netty).

## Quickstart

Jumping ahead to show how the library is used:

```java
Connector connector = Connector.to(new InetSocketAddress("localhost", 18874))
        .autoReconnect(TimeProperty.seconds(5))
        .protocol(JsonEncodingProtocol.INSTANCE)
        .build();

connector.connect();
```
