package ar.edu.undec.biblioteca.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SocioRequest(
        @NotBlank(message = "no debe estar vacío") String nombre,
        @Email(message = "debe ser un email válido") String email) {
}
