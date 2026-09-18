package io.condense.filing;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilingSummaryRepository extends JpaRepository<FilingSummary, UUID> {

    Optional<FilingSummary> findByFilingId(UUID filingId);

    List<FilingSummary> findAllByFilingIdIn(Collection<UUID> filingIds);
}
