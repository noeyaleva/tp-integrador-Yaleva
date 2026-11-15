package ar.edu.undec.biblioteca.repository;
import ar.edu.undec.biblioteca.dto.LibroMasPrestadoResponse;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import ar.edu.undec.biblioteca.entity.Prestamo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    boolean existsByLibroIdAndEstadoIn(Long libroId, Collection<EstadoPrestamo> estados);
    long countBySocioIdAndEstadoIn(Long socioId, Collection<EstadoPrestamo> estados);
    boolean existsBySocioIdAndEstadoIn(Long socioId, Collection<EstadoPrestamo> estados);
    boolean existsBySocioIdAndMultaGreaterThanAndMultaPagadaFalse(Long socioId, long multa);
    List<Prestamo> findBySocioId(Long socioId);
    List<Prestamo> findByEstado(EstadoPrestamo estado);
    void deleteByLibroId(Long libroId);
    void deleteBySocioId(Long socioId);

    @Query("""
            select new ar.edu.undec.biblioteca.dto.LibroMasPrestadoResponse(
                l.id, l.titulo, l.autor, l.isbn, count(p))
            from Prestamo p join p.libro l
            group by l.id, l.titulo, l.autor, l.isbn
            order by count(p) desc, l.id asc
            """)
    List<LibroMasPrestadoResponse> masPrestados(Pageable pageable);
}
