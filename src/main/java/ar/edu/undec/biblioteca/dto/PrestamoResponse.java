package ar.edu.undec.biblioteca.dto;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        Long libroId,
        Long socioId,
        LocalDate fechaDevolucion,
        EstadoPrestamo estado,
        boolean renovado,
        LocalDate fechaRealDevolucion,
        long multa,
        boolean multaPagada) {
}
