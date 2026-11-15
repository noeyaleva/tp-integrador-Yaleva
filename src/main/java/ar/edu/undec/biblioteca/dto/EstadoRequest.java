package ar.edu.undec.biblioteca.dto;
import ar.edu.undec.biblioteca.entity.EstadoPrestamo;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EstadoRequest(
        @NotNull(message = "es obligatorio") EstadoPrestamo estado,
        LocalDate fechaRealDevolucion) {
}
