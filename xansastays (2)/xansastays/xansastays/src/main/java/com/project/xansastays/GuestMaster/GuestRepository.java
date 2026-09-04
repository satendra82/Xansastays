package com.project.xansastays.GuestMaster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<GuestMaster,Long> {
    Optional<GuestMaster> findByEmail(String email);
}
