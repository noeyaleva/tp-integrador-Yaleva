package ar.edu.undec.biblioteca.repository;
import ar.edu.undec.biblioteca.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocioRepository extends JpaRepository<Socio, Long> {
}
