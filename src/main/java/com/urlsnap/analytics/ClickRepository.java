package com.urlsnap.analytics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClickRepository extends JpaRepository<Click, UUID> {

    List<Click> findByUrlId(UUID urlId);

    long countByUrlId(UUID urlId);
}
