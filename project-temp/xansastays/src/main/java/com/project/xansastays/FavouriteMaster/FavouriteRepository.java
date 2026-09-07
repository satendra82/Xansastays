package com.project.xansastays.FavouriteMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.RoomMaster.RoomMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<FavouriteMaster, Long> {

    Optional<FavouriteMaster> findByGuestAndRoom(GuestMaster guest, RoomMaster room);

    boolean existsByGuestAndRoom(GuestMaster guest, RoomMaster room);

    // No timestamp field on FavouriteMaster, so newest-first is approximated by favouriteId desc
    List<FavouriteMaster> findByGuestOrderByFavouriteIdDesc(GuestMaster guest);

    void deleteByGuestAndRoom(GuestMaster guest, RoomMaster room);

    @Modifying
    @Transactional
    @Query("DELETE FROM FavouriteMaster f WHERE f.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}