Kite is a Spring-based library of components implementing various
patterns for managing app and service availability, performance
and capacity. Based in part upon patterns in the Release It! book
by Michael Nygard.

HARDEN YOUR APP IN TWO EASY STEPS
=======================

Let's say you want to protect an integration point with a
so-called circuit breaker on the client side and a throttle on
the service side. While you could certainly code that logic by
hand, with Kite you don't need to do that. Instead all it takes
is two steps. First, you'll need to create the circuit breaker
and throttle:

```xml
<beans:beans xmlns="http://zkybase.org/schema/kite"
    xmlns:beans="http://www.springframework.org/schema/beans"
    xmlns:context="http://www.springframework.org/schema/context" ...>

    <!-- Activate Kite annotations -->
    <annotation-config />

    <!-- Create the circuit breaker and the throttle -->
    <circuit-breaker id="messageServiceBreaker"
        exceptionThreshold="3" timeout="30000" />
    <throttle id="messageServiceThrottle" limit="50" />

    <!-- Export the breaker and throttle as MBeans -->
    <context:mbean-export />

</beans:beans>
```

Second, you'll need to annotate the service methods. I'm assuming
a transactional service here, though that's not required:

```java
@Service
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.DEFAULT,
    readOnly = true)
public class MessageServiceImpl implements MessageService {

    @GuardedByCircuitBreaker("messageServiceBreaker")
    @GuardedByThrottle("messageServiceThrottle")
    public Message getMotd() { ... }

    @GuardedByCircuitBreaker("messageServiceBreaker")
    @GuardedByThrottle("messageServiceThrottle")
    public List<Message> getMessages() { ... }
}
```

Voila: all calls to the service methods are now guarded by (1) a
breaker that trips after three consecutive exceptions, and
retries after 30 seconds, and (2) a fail-fast concurrency
throttle that rejects requests once the limit of 50 concurrent
requests is exceeded. Kite knows that the circuit breaker goes in
front of the throttle, so you don't need to worry about that. As
an added bonus, the breaker and throttle are both exposed as
MBeans for manual tripping, resetting, etc. by your NOC should
the need arise.

Besides the annotation-based approach illustrated above, the
standard template- and AOP-based approaches are also available.

OVERVIEW OF COMPONENTS
=======================

This is a brand-new project, so there's not much yet, but here's
what exists now:

**CIRCUIT BREAKER:** Trips after a configurable number of consecutive
exceptions, and retries after a configurable timeout. Eventually
it will be possible to trip based of failure rates, and it will
be possible to select specific exception types.

**THROTTLE:** A fail-fast concurrency throttle that rejects requests
once a configurable concurrency limit is reached. Eventually
throttles will be able to reject requests based on failure to
meet SLAs.
