package com.urlsnap.url;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlRepository extends JpaRepository<Url, UUID> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByShortCodeAndIsActiveTrue(String shortCode);

    Page<Url> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    boolean existsByShortCode(String shortCode);
}
