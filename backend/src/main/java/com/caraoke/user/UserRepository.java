package com.caraoke.user;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthUid(String authUid);
    Optional<User> findByHandle(String handle);
    boolean existsByHandle(String handle);

    /** People search: handle starts with, or name contains (both case-insensitive). Inputs arrive escaped. */
    @Query("""
            select u from User u
            where u.handle like concat(:handlePrefix, '%') escape '\\'
               or lower(u.displayName) like concat('%', :name, '%') escape '\\'
            order by case when u.handle like concat(:handlePrefix, '%') escape '\\' then 0 else 1 end, u.handle
            """)
    List<User> search(@Param("handlePrefix") String handlePrefix, @Param("name") String name, Limit limit);
}
