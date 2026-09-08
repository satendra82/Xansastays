package com.project.xansastays.RoomMaster;

import com.project.xansastays.ImageMaster.ImageMaster;
import com.project.xansastays.ImageMaster.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ImageRepository imageRepository;


    public ResponseEntity<?> getAllRooms() {
        List<RoomMaster> rooms = roomRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (RoomMaster room : rooms) {
            Map<String, Object> map = roomToMap(room);
            result.add(map);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("rooms", result);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id:[0-9]+}")  // ✅ FIXED - sirf numbers match karega
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        Optional<RoomMaster> opt = roomRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Room nahi mili"));
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("room", roomToMap(opt.get()));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long total       = roomRepository.count();
        long available   = roomRepository.countByStatus(RoomMaster.Status.AVAILABLE);
        long booked      = roomRepository.countByStatus(RoomMaster.Status.BOOKED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total",     total);
        stats.put("available", available);
        stats.put("occupied",  booked);
        stats.put("maintenance", 0);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("stats", stats);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<?> addRoom(
            @RequestParam("roomNumber")  String roomNumber,
            @RequestParam("name")        String name,
            @RequestParam("pricePerDay") Double pricePerDay,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "maxGuests",   required = false) Integer maxGuests,
            @RequestParam(value = "areaSqm",     required = false) Double areaSqm,
            @RequestParam(value = "bedType",     required = false) String bedType,
            @RequestParam(value = "viewType",    required = false) String viewType,
            @RequestParam(value = "facilities",  required = false) String facilities,
            @RequestParam(value = "status",      required = false, defaultValue = "AVAILABLE") String status,
            @RequestParam(value = "roomType",    required = false, defaultValue = "SINGLE")    String roomType,
            @RequestParam(value = "images",      required = false) List<MultipartFile> images
    ) {
        try {
            RoomMaster room = new RoomMaster();
            room.setRoomNumber(roomNumber);
            room.setName(name);
            room.setPricePerDay(pricePerDay);
            room.setDescription(description);
            room.setMaxGuests(maxGuests);
            room.setAreaSqm(areaSqm);
            room.setBedType(bedType);
            room.setViewType(viewType);
            room.setFacilities(facilities);
            room.setStatus(RoomMaster.Status.valueOf(status.toUpperCase()));
            room.setRoomType(RoomMaster.RoomType.valueOf(roomType.toUpperCase()));

            RoomMaster savedRoom = roomRepository.save(room);

            // Images save karo
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        ImageMaster img = new ImageMaster();
                        img.setRoomImg(file.getBytes());
                        img.setContentType(file.getContentType());
                        img.setFileName(file.getOriginalFilename());
                        img.setRoom(savedRoom);
                        imageRepository.save(img);
                    }
                }
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Room successfully add ho gaya!");
            res.put("roomId", savedRoom.getRoomId());
            return ResponseEntity.ok(res);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Image save nahi hui: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id:[0-9]+}")  // ✅ FIXED - sirf numbers match karega
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id,
            @RequestParam("roomNumber")  String roomNumber,
            @RequestParam("name")        String name,
            @RequestParam("pricePerDay") Double pricePerDay,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "maxGuests",   required = false) Integer maxGuests,
            @RequestParam(value = "areaSqm",     required = false) Double areaSqm,
            @RequestParam(value = "bedType",     required = false) String bedType,
            @RequestParam(value = "viewType",    required = false) String viewType,
            @RequestParam(value = "facilities",  required = false) String facilities,
            @RequestParam(value = "status",      required = false) String status,
            @RequestParam(value = "roomType",    required = false) String roomType,
            @RequestParam(value = "images",      required = false) List<MultipartFile> images
    ) {
        try {
            Optional<RoomMaster> opt = roomRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Room nahi mili"));
            }

            RoomMaster room = opt.get();
            room.setRoomNumber(roomNumber);
            room.setName(name);
            room.setPricePerDay(pricePerDay);
            room.setDescription(description);
            room.setMaxGuests(maxGuests);
            room.setAreaSqm(areaSqm);
            room.setBedType(bedType);
            room.setViewType(viewType);
            room.setFacilities(facilities);
            if (status != null)   room.setStatus(RoomMaster.Status.valueOf(status.toUpperCase()));
            if (roomType != null) room.setRoomType(RoomMaster.RoomType.valueOf(roomType.toUpperCase()));

            roomRepository.save(room);

            // Nayi images add karo
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        ImageMaster img = new ImageMaster();
                        img.setRoomImg(file.getBytes());
                        img.setContentType(file.getContentType());
                        img.setFileName(file.getOriginalFilename());
                        img.setRoom(room);
                        imageRepository.save(img);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Room update ho gaya!"));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Image error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id:[0-9]+}")  // ✅ FIXED - sirf numbers match karega
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            if (!roomRepository.existsById(id)) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Room nahi mili"));
            }
            roomRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Room delete ho gaya!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/image/{imageId:[0-9]+}")  // ✅ FIXED - yahan bhi fix kiya
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        Optional<ImageMaster> opt = imageRepository.findById(imageId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ImageMaster img = opt.get();
        return ResponseEntity.ok()
                .header("Content-Type", img.getContentType() != null ? img.getContentType() : "image/jpeg")
                .body(img.getRoomImg());
    }

    private Map<String, Object> roomToMap(RoomMaster room) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId",      room.getRoomId());
        map.put("roomNumber",  room.getRoomNumber());
        map.put("name",        room.getName());
        map.put("pricePerDay", room.getPricePerDay());
        map.put("description", room.getDescription());
        map.put("maxGuests",   room.getMaxGuests());
        map.put("areaSqm",     room.getAreaSqm());
        map.put("bedType",     room.getBedType());
        map.put("viewType",    room.getViewType());
        map.put("facilities",  room.getFacilities());
        map.put("status",      room.getStatus() != null ? room.getStatus().name() : "AVAILABLE");
        map.put("roomType",    room.getRoomType() != null ? room.getRoomType().name() : "SINGLE");

        List<Map<String, Object>> imgList = new ArrayList<>();
        if (room.getImages() != null) {
            for (ImageMaster img : room.getImages()) {
                Map<String, Object> imgMap = new HashMap<>();
                imgMap.put("imageId",  img.getImageId());
                imgMap.put("fileName", img.getFileName());
                imgMap.put("url",      "/api/rooms/image/" + img.getImageId());
                imgList.add(imgMap);
            }
        }
        map.put("images",      imgList);
        map.put("imageCount",  imgList.size());
        map.put("primaryImage", imgList.isEmpty() ? null : "/api/rooms/image/" + imgList.getFirst().get("imageId"));

        return map;
    }
}