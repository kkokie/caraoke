package com.caraoke.page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PageRaisedSongRepository extends JpaRepository<PageRaisedSong, PageRaisedSong.Key> {

    List<PageRaisedSong> findByUserIdOrderByPositionAsc(long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PageRaisedSong p where p.userId = :userId")
    void deleteAllForUser(@Param("userId") long userId);
}
