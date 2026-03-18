package com.urlsnap.url;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByShortCodeAndIsActiveTrue(String shortCode);

    List<Url> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByShortCode(String shortCode);
}
