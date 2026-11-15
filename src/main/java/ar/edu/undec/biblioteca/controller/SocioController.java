package ar.edu.undec.biblioteca.controller;
import ar.edu.undec.biblioteca.dto.PrestamoResponse;
import ar.edu.undec.biblioteca.dto.SocioRequest;
import ar.edu.undec.biblioteca.dto.SocioResponse;
import ar.edu.undec.biblioteca.service.PrestamoService;
import ar.edu.undec.biblioteca.service.SocioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioService socioService;
    private final PrestamoService prestamoService;

    public SocioController(SocioService socioService, PrestamoService prestamoService) {
        this.socioService = socioService;
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<SocioResponse> crear(@Valid @RequestBody SocioRequest request) {
        SocioResponse creado = socioService.crear(request);
        return ResponseEntity.created(URI.create("/api/socios/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<SocioResponse> listar() {
        return socioService.listar();
    }

    @GetMapping("/{id}")
    public SocioResponse obtener(@PathVariable Long id) {
        return socioService.obtener(id);
    }

    @GetMapping("/{id}/prestamos")
    public List<PrestamoResponse> listarPrestamos(@PathVariable Long id) {
        return prestamoService.listarPorSocio(id);
    }

    @PutMapping("/{id}")
    public SocioResponse actualizar(@PathVariable Long id, @Valid @RequestBody SocioRequest request) {
        return socioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        socioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
