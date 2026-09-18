package io.condense.digest;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, UUID> {

    boolean existsBySubscriberIdAndDigestDate(UUID subscriberId, LocalDate digestDate);
}
