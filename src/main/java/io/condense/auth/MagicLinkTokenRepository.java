package io.condense.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, UUID> {

    Optional<MagicLinkToken> findByTokenHash(String tokenHash);
}
