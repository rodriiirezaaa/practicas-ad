package fct;

import fct.Modelo.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class Intercambio {
    private Intercambio() {}
    public static void exportarAsignacionesCsv(Estado estado, Path ruta) throws IOException {
        Validacion.validar(estado);
        Map<String, Estudiante> estudiantes = estado.estudiantes().stream()
                .collect(Collectors.toMap(Estudiante::id, Function.identity()));
        Map<String, Empresa> empresas = estado.empresas().stream()
                .collect(Collectors.toMap(Empresa::id, Function.identity()));
        var formato = CSVFormat.DEFAULT.builder().setDelimiter(';')
                .setHeader("nombre", "ciclo", "empresa").get();
        Path padre = ruta.toAbsolutePath().getParent();
        if (padre != null) Files.createDirectories(padre);
        try (var salida = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8);
             var csv = new CSVPrinter(salida, formato)) {
            for (Asignacion asignacion : estado.asignaciones()) {
                Estudiante estudiante = estudiantes.get(asignacion.estudianteId());
                Empresa empresa = empresas.get(asignacion.empresaId());
                csv.printRecord(estudiante.nombre(), estudiante.ciclo(), empresa.nombre());
            }
        }
    }
}
