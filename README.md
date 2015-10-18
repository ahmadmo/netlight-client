# netlight-client

Built on top of [Netty Project](https://github.com/netty).

## Quickstart

Jumping ahead to show how the library is used:

```
Connector connector = Connector.to(new InetSocketAddress("localhost", 18874))
        .autoReconnect(TimeProperty.seconds(5))
        .serializer(StandardSerializers.KRYO)
        .build();

connector.connect();
```
