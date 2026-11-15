package ar.edu.undec.biblioteca.dto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PrestamoRequest(
        @NotNull(message = "es obligatorio") Long libroId,
        @NotNull(message = "es obligatorio") Long socioId,
        @NotNull(message = "es obligatoria")
        @Future(message = "debe ser una fecha futura") LocalDate fechaDevolucion) {
}
