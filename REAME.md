spring flight (and some other examples) caches `Mono<RSocketRequester>` using `cache()` operator as follows

```
private final Mono<RSocketRequester> requesterMono;

public RadarService(RSocketRequester.Builder builder) {
	this.requesterMono = builder
            .connectTcp(...)
            .retry()
            .cache();
}
```

As far as I understand, without `cache()` it makes RSocket behave like HTTP since new connection will be established every time this mono is subscribed.
But this code wouldn't reconnect when the server (`rador-collector` in the spring-flight's case) is restarted and keep responding an error.

I was able to re-establish a connection only when the connection is closed. But it looks ugly and like a boilerplate.
https://gist.github.com/making/c066f594671930bd37d68b1f768e0928

Any idea to make this better or invalidate a cache elegantly (rather than ttl)?

I'm aware that providing ReconnectingRSocket is on going. I'm not sure this will resolve the case.