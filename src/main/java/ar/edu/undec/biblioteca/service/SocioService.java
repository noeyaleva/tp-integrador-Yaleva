package ar.edu.undec.biblioteca.service;
import ar.edu.undec.biblioteca.dto.SocioRequest;
import ar.edu.undec.biblioteca.dto.SocioResponse;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import ar.edu.undec.biblioteca.entity.Socio;
import ar.edu.undec.biblioteca.exception.RecursoNoEncontradoException;
import ar.edu.undec.biblioteca.exception.ReglaNegocioException;
import ar.edu.undec.biblioteca.repository.PrestamoRepository;
import ar.edu.undec.biblioteca.repository.SocioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocioService {

    private final SocioRepository socioRepository;
    private final PrestamoRepository prestamoRepository;

    public SocioService(SocioRepository socioRepository, PrestamoRepository prestamoRepository) {
        this.socioRepository = socioRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional(readOnly = true)
    public List<SocioResponse> listar() {
        return socioRepository.findAll().stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public SocioResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional
    public SocioResponse crear(SocioRequest request) {
        Socio socio = new Socio(request.nombre().trim(), request.email());
        return aResponse(socioRepository.save(socio));
    }

    @Transactional
    public SocioResponse actualizar(Long id, SocioRequest request) {
        Socio socio = buscarEntidad(id);
        socio.setNombre(request.nombre().trim());
        socio.setEmail(request.email());
        return aResponse(socioRepository.save(socio));
    }

    @Transactional
    public void eliminar(Long id) {
        Socio socio = buscarEntidad(id);
        if (prestamoRepository.existsBySocioIdAndEstadoIn(id, EstadoPrestamo.NO_DEVUELTOS)) {
            throw new ReglaNegocioException("El socio tiene préstamos sin devolver y no se puede borrar");
        }
        prestamoRepository.deleteBySocioId(id);
        socioRepository.delete(socio);
    }

    @Transactional(readOnly = true)
    public Socio buscarEntidad(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Socio", id));
    }

    private SocioResponse aResponse(Socio socio) {
        return new SocioResponse(socio.getId(), socio.getNombre(), socio.getEmail());
    }
}
