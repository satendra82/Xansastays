package com.project.xansastays.ImageMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageMaster, Long> {

    List<ImageMaster> findByRoom_RoomId(Long roomId);

    void deleteByRoom_RoomId(Long roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ImageMaster i WHERE i.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}