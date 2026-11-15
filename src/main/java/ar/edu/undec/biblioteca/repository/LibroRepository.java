package ar.edu.undec.biblioteca.repository;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import ar.edu.undec.biblioteca.entity.Libro;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);
    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Query("""
            select l from Libro l
            where not exists (
                select p.id from Prestamo p
                where p.libro = l and p.estado in :estados)
            """)
    List<Libro> findDisponibles(@Param("estados") Collection<EstadoPrestamo> estados);

    @Query("""
            select l from Libro l
            where exists (
                select p.id from Prestamo p
                where p.libro = l and p.estado in :estados)
            """)
    List<Libro> findNoDisponibles(@Param("estados") Collection<EstadoPrestamo> estados);
}
