package com.caraoke.user;

import org.springframework.data.jpa.repository.JpaRepository;

interface BannedAuthUidRepository extends JpaRepository<BannedAuthUid, String> { }
