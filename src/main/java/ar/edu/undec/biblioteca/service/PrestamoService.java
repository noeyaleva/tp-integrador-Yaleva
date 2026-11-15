package ar.edu.undec.biblioteca.service;
import ar.edu.undec.biblioteca.dto.PrestamoRequest;
import ar.edu.undec.biblioteca.dto.PrestamoResponse;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import ar.edu.undec.biblioteca.entity.Libro;
import ar.edu.undec.biblioteca.entity.Prestamo;
import ar.edu.undec.biblioteca.entity.Socio;
import ar.edu.undec.biblioteca.exception.RecursoNoEncontradoException;
import ar.edu.undec.biblioteca.exception.ReglaNegocioException;
import ar.edu.undec.biblioteca.repository.PrestamoRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestamoService {

    private static final int MAXIMO_PRESTAMOS_POR_SOCIO = 3;
    private static final int DIAS_RENOVACION = 7;
    private static final long MULTA_POR_DIA = 500L;
    private final PrestamoRepository prestamoRepository;
    private final LibroService libroService;
    private final SocioService socioService;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           LibroService libroService,
                           SocioService socioService) {
        this.prestamoRepository = prestamoRepository;
        this.libroService = libroService;
        this.socioService = socioService;
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar(EstadoPrestamo estado) {
        List<Prestamo> prestamos = (estado == null)
                ? prestamoRepository.findAll()
                : prestamoRepository.findByEstado(estado);
        return prestamos.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarPorSocio(Long socioId) {
        socioService.buscarEntidad(socioId);
        return prestamoRepository.findBySocioId(socioId).stream().map(this::aResponse).toList();
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request) {
        Libro libro = libroService.buscarEntidad(request.libroId());
        Socio socio = socioService.buscarEntidad(request.socioId());

        controlarSocioNoSuspendido(socio.getId());
        controlarLibroDisponible(libro.getId());
        controlarCupoDelSocio(socio.getId());

        Prestamo prestamo = new Prestamo(libro, socio, request.fechaDevolucion());
        return aResponse(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse actualizar(Long id, PrestamoRequest request) {
        Prestamo prestamo = buscarEntidad(id);
        Libro libro = libroService.buscarEntidad(request.libroId());
        Socio socio = socioService.buscarEntidad(request.socioId());

        boolean sigueAfuera = EstadoPrestamo.NO_DEVUELTOS.contains(prestamo.getEstado());
        if (sigueAfuera && !libro.getId().equals(prestamo.getLibro().getId())) {
            controlarLibroDisponible(libro.getId());
        }
        if (sigueAfuera && !socio.getId().equals(prestamo.getSocio().getId())) {
            controlarSocioNoSuspendido(socio.getId());
            controlarCupoDelSocio(socio.getId());
        }

        prestamo.setLibro(libro);
        prestamo.setSocio(socio);
        prestamo.setFechaDevolucion(request.fechaDevolucion());
        return aResponse(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse cambiarEstado(Long id, EstadoPrestamo nuevoEstado, LocalDate fechaReal) {
        Prestamo prestamo = buscarEntidad(id);
        EstadoPrestamo actual = prestamo.getEstado();
        if (!actual.puedePasarA(nuevoEstado)) {
            throw new ReglaNegocioException(
                    "No se puede pasar un préstamo de " + actual + " a " + nuevoEstado);
        }
        prestamo.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoPrestamo.DEVUELTO) {
            registrarDevolucion(prestamo, fechaReal);
        }
        return aResponse(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse renovar(Long id) {
        Prestamo prestamo = buscarEntidad(id);
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new ReglaNegocioException(
                    "Solo se puede renovar un préstamo ACTIVO (estado actual: " + prestamo.getEstado() + ")");
        }
        if (prestamo.isRenovado()) {
            throw new ReglaNegocioException("El préstamo ya fue renovado una vez");
        }
        prestamo.setFechaDevolucion(prestamo.getFechaDevolucion().plusDays(DIAS_RENOVACION));
        prestamo.setRenovado(true);
        return aResponse(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse pagarMulta(Long id) {
        Prestamo prestamo = buscarEntidad(id);
        if (prestamo.getMulta() <= 0) {
            throw new ReglaNegocioException("El préstamo no tiene multa para pagar");
        }
        if (prestamo.isMultaPagada()) {
            throw new ReglaNegocioException("La multa de este préstamo ya fue pagada");
        }
        prestamo.setMultaPagada(true);
        return aResponse(prestamoRepository.save(prestamo));
    }

    @Transactional
    public void eliminar(Long id) {
        Prestamo prestamo = buscarEntidad(id);
        prestamoRepository.delete(prestamo);
    }

    @Transactional(readOnly = true)
    public Prestamo buscarEntidad(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo", id));
    }

    private void registrarDevolucion(Prestamo prestamo, LocalDate fechaReal) {
        LocalDate real = (fechaReal != null) ? fechaReal : LocalDate.now();
        prestamo.setFechaRealDevolucion(real);
        long diasAtraso = ChronoUnit.DAYS.between(prestamo.getFechaDevolucion(), real);
        prestamo.setMulta(diasAtraso > 0 ? diasAtraso * MULTA_POR_DIA : 0);
    }

    private void controlarSocioNoSuspendido(Long socioId) {
        if (prestamoRepository.existsBySocioIdAndMultaGreaterThanAndMultaPagadaFalse(socioId, 0L)) {
            throw new ReglaNegocioException(
                    "El socio " + socioId + " tiene una multa sin pagar y no puede pedir préstamos");
        }
    }

    private void controlarLibroDisponible(Long libroId) {
        if (prestamoRepository.existsByLibroIdAndEstadoIn(libroId, EstadoPrestamo.NO_DEVUELTOS)) {
            throw new ReglaNegocioException("El libro " + libroId + " ya tiene un préstamo sin devolver");
        }
    }

    private void controlarCupoDelSocio(Long socioId) {
        long sinDevolver = prestamoRepository.countBySocioIdAndEstadoIn(socioId, EstadoPrestamo.NO_DEVUELTOS);
        if (sinDevolver >= MAXIMO_PRESTAMOS_POR_SOCIO) {
            throw new ReglaNegocioException(
                    "El socio " + socioId + " ya tiene " + MAXIMO_PRESTAMOS_POR_SOCIO + " préstamos sin devolver");
        }
    }

    private PrestamoResponse aResponse(Prestamo p) {
        return new PrestamoResponse(
                p.getId(), p.getLibro().getId(), p.getSocio().getId(),
                p.getFechaDevolucion(), p.getEstado(), p.isRenovado(),
                p.getFechaRealDevolucion(), p.getMulta(), p.isMultaPagada());
    }
}
