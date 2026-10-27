package com.project.xansastays.ImageMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageMaster, Long> {

    // Lightweight projection: image ke bytes (room_img) load NAHI hote
    interface RoomImageInfo {
        Long getRoomId();
        Long getImageId();
        String getFileName();
    }

    @Query("select i.room.roomId as roomId, i.imageId as imageId, i.fileName as fileName " +
            "from ImageMaster i where i.room.roomId in :roomIds order by i.imageId")
    List<RoomImageInfo> findInfoByRoomIds(@Param("roomIds") List<Long> roomIds);

    @Modifying
    @Transactional
    @Query("delete from ImageMaster i where i.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}