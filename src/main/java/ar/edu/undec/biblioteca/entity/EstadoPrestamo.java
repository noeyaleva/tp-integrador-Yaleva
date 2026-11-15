package ar.edu.undec.biblioteca.entity;
import java.util.EnumSet;
import java.util.Set;

public enum EstadoPrestamo {
    ACTIVO,
    DEVUELTO,
    VENCIDO;

    public static final Set<EstadoPrestamo> NO_DEVUELTOS = EnumSet.of(ACTIVO, VENCIDO);

    public boolean puedePasarA(EstadoPrestamo destino) {
        return switch (this) {
            case ACTIVO -> destino == DEVUELTO || destino == VENCIDO;
            case VENCIDO -> destino == DEVUELTO;
            case DEVUELTO -> false;
        };
    }
}
