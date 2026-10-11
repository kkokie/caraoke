package com.caraoke.song;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface SongRepository extends JpaRepository<Song, Long> {
    Optional<Song> findByAppleId(String appleId);

    /** Which of these catalog ids someone has already opened on caraoke (a light popularity signal). */
    @Query("select s.appleId from Song s where s.appleId in :appleIds")
    Set<String> findAppleIdsIn(@Param("appleIds") Collection<String> appleIds);
}
