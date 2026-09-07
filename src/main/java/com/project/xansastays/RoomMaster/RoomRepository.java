package com.project.xansastays.RoomMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomMaster, Long> {

    List<RoomMaster> findByStatus(RoomMaster.Status status);

    List<RoomMaster> findByRoomType(RoomMaster.RoomType roomType);

    long countByStatus(RoomMaster.Status status);
}