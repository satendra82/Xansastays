package com.project.xansastays.FacilitiesMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FacilitiesRepository extends JpaRepository<FacilitiesMaster,Long> {

    @Query("SELECT f FROM FacilitiesMaster f WHERE f.room.roomId = :roomId")
    Optional<FacilitiesMaster> findByRoomId(@Param("roomId") Long roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FacilitiesMaster f WHERE f.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}