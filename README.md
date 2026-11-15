# TP Integrador · DAW1 — Biblioteca
 
**Alumno/a:** Apellido, Nombre · **Legajo:** 00000
**Enunciado elegido:** biblioteca
 
## Descripción
 
API REST para una biblioteca barrial que quiere dejar el cuaderno de papel. Permite registrar libros y socios, y llevar el control de los préstamos: qué libro está prestado, a quién, hasta cuándo, con límite de préstamos por socio, estados de devolución y multas por atraso. Está pensada para el personal de la biblioteca.
 
## Cómo ejecutarlo
 
Requisitos: Java 21 (Maven viene incluido con `mvnw`).
 
```
./mvnw spring-boot:run
```
 
La API queda en `http://localhost:8080/api`. La base H2 es **en memoria**: se crea al arrancar y se borra al cerrar la aplicación, por lo que no genera archivos ni hay nada que excluir del repositorio. Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:biblioteca`, usuario `sa`, sin contraseña).
 
## Modelo
 
| Entidad | Atributos principales | Relaciones |
|---|---|---|
| Libro | id, titulo, autor, isbn (único) | 1 a N con Prestamo |
| Socio | id, nombre, email | 1 a N con Prestamo |
| Prestamo | id, libroId, socioId, fechaDevolucion, estado, renovado, fechaRealDevolucion, multa, multaPagada | N a 1 con Libro y con Socio |
 
Estados de Prestamo: `ACTIVO` → `DEVUELTO` | `VENCIDO`, `VENCIDO` → `DEVUELTO`. `DEVUELTO` es final. Cualquier otra transición devuelve 409. El estado inicial (`ACTIVO`) lo fija el sistema y no se envía al crear.
 
## Endpoints
 
Cualquier ruta con `{id}` devuelve 400 si el id no es numérico.
 
### Libros
 
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/libros` | Lista libros. Con `?disponible=true` solo los que no tienen préstamo activo o vencido (`false` devuelve los contrarios) | 200, 400 |
| GET | `/api/libros/mas-prestados?top=5` | Ranking de libros con más préstamos y la cantidad de cada uno | 200, 400 |
| GET | `/api/libros/{id}` | Busca por id | 200, 400, 404 |
| POST | `/api/libros` | Crea | 201, 400, 409 |
| PUT | `/api/libros/{id}` | Modifica | 200, 400, 404, 409 |
| DELETE | `/api/libros/{id}` | Borra | 204, 404, 409 |
 
### Socios
 
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/socios` | Lista socios | 200 |
| GET | `/api/socios/{id}` | Busca por id | 200, 400, 404 |
| GET | `/api/socios/{id}/prestamos` | Préstamos del socio | 200, 400, 404 |
| POST | `/api/socios` | Crea | 201, 400 |
| PUT | `/api/socios/{id}` | Modifica | 200, 400, 404 |
| DELETE | `/api/socios/{id}` | Borra | 204, 404, 409 |
 
### Préstamos
 
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/prestamos` | Lista préstamos. Con `?estado=ACTIVO` filtra por estado | 200, 400 |
| GET | `/api/prestamos/{id}` | Busca por id | 200, 400, 404 |
| POST | `/api/prestamos` | Crea (queda `ACTIVO`) | 201, 400, 404, 409 |
| PUT | `/api/prestamos/{id}` | Modifica | 200, 400, 404, 409 |
| DELETE | `/api/prestamos/{id}` | Borra | 204, 404 |
| PATCH | `/api/prestamos/{id}/estado` | Cambia el estado. Cuerpo: `{"estado":"DEVUELTO"}`; `fechaRealDevolucion` es opcional (por defecto, hoy) | 200, 400, 404, 409 |
| PATCH | `/api/prestamos/{id}/renovar` | Renueva el préstamo | 200, 404, 409 |
| PATCH | `/api/prestamos/{id}/pagar-multa` | Marca la multa como pagada | 200, 404, 409 |
 
Los errores se devuelven en formato `ProblemDetail`. En los 400 de validación se agrega el mapa `errores` (campo → mensaje). Un JSON mal formado devuelve 400, nunca 500.
 
## Reglas de negocio
 
1. Un libro con un préstamo no devuelto (`ACTIVO` o `VENCIDO`) no se puede volver a prestar → **409**.
2. Un socio puede tener como máximo 3 préstamos no devueltos → **409**.
3. No puede haber dos libros con el mismo ISBN → **409**.
4. Las transiciones de estado no permitidas (por ejemplo desde `DEVUELTO`) → **409**.
5. No se puede borrar un libro ni un socio con un préstamo sin devolver → **409**. Si solo tiene préstamos ya devueltos, estos se borran junto con él.
6. Si el libro o el socio de un préstamo no existen → **404**.
7. La fecha de devolución debe ser futura → **400**.
### Extras implementados
 
- **Multa por atraso:** al pasar a `DEVUELTO` después de `fechaDevolucion`, el préstamo guarda `fechaRealDevolucion` y `multa` = días de atraso × $500.
- **Socio suspendido:** un socio con una multa sin pagar no puede pedir préstamos → **409**. `PATCH /api/prestamos/{id}/pagar-multa` la marca como pagada (409 si el préstamo no tiene multa o ya está pagada).
- **Disponibilidad:** `GET /api/libros?disponible=true`.
- **Ranking:** `GET /api/libros/mas-prestados?top=5`.
- **Renovación:** `PATCH /api/prestamos/{id}/renovar` suma 7 días a `fechaDevolucion`, una sola vez y solo si está `ACTIVO` (si no, **409**).
## Pruebas
 
En `pruebas.http` están todas las peticiones de ejemplo (se ejecutan desde IntelliJ).
