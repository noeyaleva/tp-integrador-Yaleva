package ar.edu.undec.biblioteca.dto;

public record LibroMasPrestadoResponse(
        Long id,
        String titulo,
        String autor,
        String isbn,
        Long cantidad) {
}
