package com.project.xansastays.amenities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmenitiesRepository extends JpaRepository<AmenitiesMaster, Long> {
    List<AmenitiesMaster> findByRoom_RoomId(Long roomId);
}