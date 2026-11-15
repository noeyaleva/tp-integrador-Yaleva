package ar.edu.undec.biblioteca.dto;
import jakarta.validation.constraints.NotBlank;

public record LibroRequest(
        @NotBlank(message = "no debe estar vacío") String titulo,
        @NotBlank(message = "no debe estar vacío") String autor,
        @NotBlank(message = "no debe estar vacío") String isbn) {
}
