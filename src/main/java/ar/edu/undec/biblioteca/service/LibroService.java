package ar.edu.undec.biblioteca.service;
import ar.edu.undec.biblioteca.dto.LibroMasPrestadoResponse;
import ar.edu.undec.biblioteca.dto.LibroRequest;
import ar.edu.undec.biblioteca.dto.LibroResponse;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import ar.edu.undec.biblioteca.entity.Libro;
import ar.edu.undec.biblioteca.exception.RecursoNoEncontradoException;
import ar.edu.undec.biblioteca.exception.ReglaNegocioException;
import ar.edu.undec.biblioteca.repository.LibroRepository;
import ar.edu.undec.biblioteca.repository.PrestamoRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public LibroService(LibroRepository libroRepository, PrestamoRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional(readOnly = true)
    public List<LibroResponse> listar(Boolean disponible) {
        List<Libro> libros;
        if (disponible == null) {
            libros = libroRepository.findAll();
        } else if (disponible) {
            libros = libroRepository.findDisponibles(EstadoPrestamo.NO_DEVUELTOS);
        } else {
            libros = libroRepository.findNoDisponibles(EstadoPrestamo.NO_DEVUELTOS);
        }
        return libros.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LibroMasPrestadoResponse> masPrestados(int top) {
        return prestamoRepository.masPrestados(PageRequest.of(0, Math.max(1, top)));
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional
    public LibroResponse crear(LibroRequest request) {
        String isbn = request.isbn().trim();
        if (libroRepository.existsByIsbn(isbn)) {
            throw new ReglaNegocioException("Ya existe un libro con el ISBN " + isbn);
        }
        Libro libro = new Libro(request.titulo().trim(), request.autor().trim(), isbn);
        return aResponse(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest request) {
        Libro libro = buscarEntidad(id);
        String isbn = request.isbn().trim();
        if (libroRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ReglaNegocioException("Ya existe un libro con el ISBN " + isbn);
        }
        libro.setTitulo(request.titulo().trim());
        libro.setAutor(request.autor().trim());
        libro.setIsbn(isbn);
        return aResponse(libroRepository.save(libro));
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro = buscarEntidad(id); 
        if (prestamoRepository.existsByLibroIdAndEstadoIn(id, EstadoPrestamo.NO_DEVUELTOS)) {
            throw new ReglaNegocioException("El libro tiene un préstamo sin devolver y no se puede borrar");
        }

        prestamoRepository.deleteByLibroId(id);
        libroRepository.delete(libro);
    }

    @Transactional(readOnly = true)
    public Libro buscarEntidad(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro", id));
    }

    private LibroResponse aResponse(Libro libro) {
        return new LibroResponse(libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getIsbn());
    }
}
