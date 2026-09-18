package io.condense.filing;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilingRepository extends JpaRepository<Filing, UUID> {

    boolean existsByAccessionNumber(String accessionNumber);

    Optional<Filing> findByAccessionNumber(String accessionNumber);

    List<Filing> findAllByCikInAndFiledOnGreaterThanEqualOrderByFiledOnDesc(Collection<String> ciks, LocalDate since);

    List<Filing> findAllByCikOrderByFiledOnDescIdDesc(String cik, Pageable pageable);
}
