Kite is a Spring-based library of components implementing various patterns for managing app and service availability,
performance and capacity. Based in part upon patterns in the Release It! book by Michael Nygard.

Harden your app in two easy steps
=================================

Let's say you want to protect an integration point with a concurrency throttle, rate limiter and circuit breaker. While
While you could certainly code that logic by hand, with Kite you don't need to do that. Instead all it takes is two
steps. First, you'll need to create the circuit breaker and throttle:

```xml
<beans:beans xmlns="http://zkybase.org/schema/kite"
    xmlns:beans="http://www.springframework.org/schema/beans"
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-3.1.xsd
        http://zkybase.org/schema/kite
        http://zkybase.org/schema/kite/kite-1.0-a3.xsd">

    <!-- Activate Kite annotations -->
    <annotation-config />
    
    <!-- Guards -->
    <circuit-breaker id="messageServiceBreaker" exceptionThreshold="3" timeout="30000" />
    <concurrency-throttle id="messageServiceThrottle" limit="50" />
    <rate-limiting-throttle id="messageServiceRateLimiter" limit="5000" />

    <!-- Export the guards as MBeans -->
    <context:mbean-export />

</beans:beans>
```

[**NOTE:** The rate-limiter doesn't actually perform any rate-limiting yet. It just does a pass-through. I'll fix this
when I get the chance.]

Second, you'll need to annotate the service methods. I'm assuming a transactional service here, though that's not
required:

```java
@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @GuardedBy({ "messageServiceThrottle", "messageServiceRateLimiter", "messageServiceBreaker" })
    public Message getMotd() { ... }

    @GuardedBy({ "messageServiceThrottle", "messageServiceRateLimiter", "messageServiceBreaker" })
    public List<Message> getMessages() { ... }
}
```

Voila: all calls to the service methods are now guarded by

* a concurrency throttle that rejects requests once there are 50 concurrent requests in the guard
* a rate-limiter that rejects anything beyond the first 5,000 requests in a given hour
* a circuit breakers that trips after three consecutive exceptions, and retries after 30 seconds

Kite applies the guards in the specified order. As an added bonus, the guards are both exposed as MBeans for manual
tripping, resetting, etc. by your NOC should the need arise.

Besides the annotation-based approach illustrated above, the standard template- and AOP-based approaches are also
available.

Overview of components
======================

This is a brand-new project, so there's not much yet, but here's what exists now:

**Circuit breaker:** Trips after a configurable number of consecutive exceptions, and retries after a configurable
timeout. Eventually it will be possible to trip based of failure rates, and it will be possible to select specific
exception types.

**Concurrency throttle:** A fail-fast concurrency throttle that rejects requests once a configurable concurrency limit
is reached. Eventually throttles will be able to reject requests based on failure to meet SLAs.

**Rate-limiting throttle:** A throttle that rejects requests after the client reaches a configurable limit on the
number of requests in some time period.
