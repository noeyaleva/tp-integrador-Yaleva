package ar.edu.undec.biblioteca.controller;
import ar.edu.undec.biblioteca.dto.EstadoRequest;
import ar.edu.undec.biblioteca.dto.PrestamoRequest;
import ar.edu.undec.biblioteca.dto.PrestamoResponse;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import ar.edu.undec.biblioteca.service.PrestamoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> crear(@Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse creado = prestamoService.crear(request);
        return ResponseEntity.created(URI.create("/api/prestamos/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<PrestamoResponse> listar(@RequestParam(required = false) EstadoPrestamo estado) {
        return prestamoService.listar(estado);
    }

    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable Long id) {
        return prestamoService.obtener(id);
    }

    @PutMapping("/{id}")
    public PrestamoResponse actualizar(@PathVariable Long id, @Valid @RequestBody PrestamoRequest request) {
        return prestamoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public PrestamoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return prestamoService.cambiarEstado(id, request.estado(), request.fechaRealDevolucion());
    }

    @PatchMapping("/{id}/renovar")
    public PrestamoResponse renovar(@PathVariable Long id) {
        return prestamoService.renovar(id);
    }

    @PatchMapping("/{id}/pagar-multa")
    public PrestamoResponse pagarMulta(@PathVariable Long id) {
        return prestamoService.pagarMulta(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        prestamoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}