package com.project.xansastays.ImageMaster;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/image")
public class ImageController {

    private final ImageRepository imageRepository;

    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    // ✅ Byte → byte[] fix kiya
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        return imageRepository.findById(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .body(img.getRoomImg()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload/room/{roomId}")
    public String uploadRoomImage(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            Model model
    ) throws IOException {

        if (file.isEmpty()) {
            model.addAttribute("error", "File empty hai, koi image select karo!");
            return "redirect:/Admin/rooms";
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            model.addAttribute("error", "Sirf image files allowed hain!");
            return "redirect:/Admin/rooms";
        }

        ImageMaster img = new ImageMaster();
        img.setRoomImg(file.getBytes());
        img.setContentType(contentType);
        img.setFileName(file.getOriginalFilename());

        com.project.xansastays.RoomMaster.RoomMaster room =
                new com.project.xansastays.RoomMaster.RoomMaster();
        room.setRoomId(roomId);
        img.setRoom(room);

        imageRepository.save(img);

        return "redirect:/Admin/rooms";
    }

    @PostMapping("/upload/room/{roomId}/multiple")
    public String uploadMultipleRoomImages(
            @PathVariable Long roomId,
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {

        com.project.xansastays.RoomMaster.RoomMaster room =
                new com.project.xansastays.RoomMaster.RoomMaster();
        room.setRoomId(roomId);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    ImageMaster img = new ImageMaster();
                    img.setRoomImg(file.getBytes());
                    img.setContentType(contentType);
                    img.setFileName(file.getOriginalFilename());
                    img.setRoom(room);
                    imageRepository.save(img);
                }
            }
        }

        return "redirect:/Admin/rooms";
    }

    @PostMapping("/delete/{id}")
    public String deleteImage(
            @PathVariable Long id,
            @RequestParam(value = "redirectUrl", defaultValue = "/Admin/rooms") String redirectUrl
    ) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/delete/room/{roomId}")
    public String deleteAllRoomImages(@PathVariable Long roomId) {
        List<ImageMaster> images = imageRepository.findByRoom_RoomId(roomId);
        imageRepository.deleteAll(images);
        return "redirect:/Admin/rooms";
    }
}