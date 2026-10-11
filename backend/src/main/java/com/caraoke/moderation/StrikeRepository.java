package com.caraoke.moderation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface StrikeRepository extends JpaRepository<Strike, Long> {

    long countByUserIdAndVoidedAtIsNull(long userId);

    List<Strike> findTop20ByUserIdOrderByIdDesc(long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Strike s set s.voidedAt = :at where s.userId = :userId and s.voidedAt is null")
    int voidAll(@Param("userId") long userId, @Param("at") Instant at);
}
