package fct;

import com.fasterxml.jackson.databind.ObjectMapper;
import fct.Modelo.Estado;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/** Completa esta implementación. Las operaciones pendientes no simulan éxito. */
public class RepositorioJson implements AlmacenFct {
    private final Path ruta;
    private final Consumer<String> notificar;
    private final boolean simularFallo;
    public RepositorioJson(Path ruta) { this(ruta, mensaje -> {}, false); }
    public RepositorioJson(Path ruta, Consumer<String> notificar, boolean simularFallo) {
        this.ruta = ruta;
        this.notificar = notificar;
        this.simularFallo = simularFallo;
    }
    public Estado recuperar() throws IOException {
        String texto = Files.readString(ruta, StandardCharsets.UTF_8);
        Estado estado = new ObjectMapper().readValue(texto, Estado.class);
        Validacion.validar(estado);
        return estado;
    }
    public void guardar(Estado estado) throws IOException {
        Validacion.validar(estado);
        String texto = new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(estado);
        EscrituraSegura.reemplazar(ruta, texto, simularFallo);
        notificar.accept("estado guardado");
    }
}
