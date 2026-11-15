package ar.edu.undec.biblioteca.controller;
import ar.edu.undec.biblioteca.dto.LibroMasPrestadoResponse;
import ar.edu.undec.biblioteca.dto.LibroRequest;
import ar.edu.undec.biblioteca.dto.LibroResponse;
import ar.edu.undec.biblioteca.service.LibroService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @PostMapping
    public ResponseEntity<LibroResponse> crear(@Valid @RequestBody LibroRequest request) {
        LibroResponse creado = libroService.crear(request);
        return ResponseEntity.created(URI.create("/api/libros/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<LibroResponse> listar(@RequestParam(required = false) Boolean disponible) {
        return libroService.listar(disponible);
    }

    @GetMapping("/mas-prestados")
    public List<LibroMasPrestadoResponse> masPrestados(@RequestParam(defaultValue = "5") int top) {
        return libroService.masPrestados(top);
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PutMapping("/{id}")
    public LibroResponse actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
        return libroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
