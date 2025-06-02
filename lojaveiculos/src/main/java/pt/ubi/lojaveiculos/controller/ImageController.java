package pt.ubi.lojaveiculos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exposes images stored in the “uploads/” directory at
 *   http://localhost:8080/images/{filename}
 *
 * Example: if imagePath = abc123.jpg, the tag <img src="/images/abc123.jpg">
 * will load that file.
 */
@Controller
public class ImageController {

    private final Path uploadDir;

    public ImageController(
            @Value("${car.upload-dir:uploads}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(
            @PathVariable String filename) throws IOException {

        Path file = uploadDir.resolve(filename).normalize();

        // security check: block “../../../etc/passwd”–style paths
        if (!file.startsWith(uploadDir) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
