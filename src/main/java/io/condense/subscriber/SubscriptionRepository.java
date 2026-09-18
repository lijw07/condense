package io.condense.subscriber;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findAllBySubscriberId(UUID subscriberId);

    @Query("select distinct s.cik from Subscription s")
    List<String> findAllTrackedCiks();
}
