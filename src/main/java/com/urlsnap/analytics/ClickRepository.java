package com.urlsnap.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ClickRepository extends JpaRepository<Click, UUID> {

    List<Click> findByUrlId(UUID urlId);

    long countByUrlId(UUID urlId);

    @Query("SELECT COUNT(c) FROM Click c WHERE c.url.id = :urlId AND c.clickedAt >= :since")
    long countByUrlIdAndClickedAtAfter(@Param("urlId") UUID urlId, @Param("since") LocalDateTime since);

    @Query("SELECT c.referer, COUNT(c) FROM Click c WHERE c.url.id = :urlId AND c.referer IS NOT NULL GROUP BY c.referer ORDER BY COUNT(c) DESC")
    List<Object[]> findTopReferersByUrlId(@Param("urlId") UUID urlId, Pageable pageable);

    @Query("SELECT CAST(c.clickedAt AS date), COUNT(c) FROM Click c WHERE c.url.id = :urlId AND c.clickedAt >= :since GROUP BY CAST(c.clickedAt AS date) ORDER BY CAST(c.clickedAt AS date) ASC")
    List<Object[]> findDailyClicksByUrlId(@Param("urlId") UUID urlId, @Param("since") LocalDateTime since);
}
