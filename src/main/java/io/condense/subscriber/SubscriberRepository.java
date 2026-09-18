package io.condense.subscriber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, UUID> {

    Optional<Subscriber> findByEmailIgnoreCase(String email);

    Optional<Subscriber> findByUnsubscribeToken(String unsubscribeToken);

    List<Subscriber> findAllByStatusAndCadence(SubscriberStatus status, Cadence cadence);
}
