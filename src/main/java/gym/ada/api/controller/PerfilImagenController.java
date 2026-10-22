
package gym.ada.api.controller;

import com.cloudinary.Cloudinary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
public class PerfilImagenController {

    private final Cloudinary cloudinary;

    public PerfilImagenController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping(
        value = "/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> subirAvatar(
        @RequestHeader(
            value = "Authorization",
            required = false
        ) String authorization,
        @RequestPart("file") MultipartFile archivo
    ) {
        try {
            // Identificar al usuario desde Firebase.
            if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "mensaje", "Falta el token Firebase"
                    ));
            }

            String idToken = authorization.substring(7).trim();

            FirebaseToken token = FirebaseAuth.getInstance()
                .verifyIdToken(idToken);

            String uid = token.getUid();

            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Selecciona una imagen")
                );
            }

            // Máximo 3 MB.
            if (archivo.getSize() > 3 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Máximo 3 MB")
                );
            }

            String tipo = archivo.getContentType();

            if (!"image/jpeg".equals(tipo) &&
                !"image/png".equals(tipo)) {
                return ResponseEntity.badRequest().body(
                    Map.of(
                        "mensaje", "Solo se admiten JPG y PNG"
                    )
                );
            }

            byte[] contenido = archivo.getBytes();

            // No confiar solamente en la extensión o MIME.
            if (ImageIO.read(
                new ByteArrayInputStream(contenido)
            ) == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Imagen no válida")
                );
            }

            Map<String, Object> opciones = new HashMap<>();
            opciones.put("folder", "gyselmood/avatars");
            opciones.put("public_id", uid);
            opciones.put("overwrite", true);
            opciones.put("unique_filename", false);
            opciones.put("invalidate", true);
            opciones.put("resource_type", "image");

            Map<?, ?> resultado = cloudinary.uploader()
                .upload(contenido, opciones);

            String url = resultado.get("secure_url").toString();

            return ResponseEntity.ok(
                Map.of(
                    "url", url,
                    "mensaje", "Imagen subida correctamente"
                )
            );

        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "mensaje", "Sesión Firebase no válida"
                ));
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).body(Map.of(
                "mensaje", "No se pudo subir la imagen"
            ));
        }
    }
}
