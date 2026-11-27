package fct.bd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Completa esta infraestructura; no implementes el CRUD de UD3. */
public class EsquemaJdbc implements PreparacionRelacional {
    private static final List<String> TABLAS =
            List.of("empresa", "estudiante", "tutor", "asignacion");

    private final ConfiguracionBD config;
    public EsquemaJdbc(ConfiguracionBD config) { this.config = config; }

    @Override public void crear(Connection conexion) throws SQLException {
        if (!config.sqlite()) {
            String esquema = comillas(config.esquema());
            try (var sentencia = conexion.createStatement()) {
                sentencia.execute("CREATE SCHEMA IF NOT EXISTS " + esquema);
                sentencia.execute("SET search_path TO " + esquema);
            }
        }
        // El DDL usa IF NOT EXISTS: repetir la preparación conserva las filas.
        for (String instruccion : ddl().split(";")) {
            if (instruccion.isBlank()) continue;
            try (var sentencia = conexion.createStatement()) {
                sentencia.execute(instruccion);
            }
        }
    }

    @Override public void comprobar(Connection conexion) throws SQLException {
        DatabaseMetaData metadatos = conexion.getMetaData();
        if (config.sqlite()) comprobarClavesForaneasActivas(conexion);

        var columnas = new LinkedHashMap<String, Map<String, Columna>>();
        for (String tabla : TABLAS) {
            if (!existeTabla(metadatos, tabla))
                throw new SQLException("Falta la tabla " + tabla);
            columnas.put(tabla, columnasDe(metadatos, tabla));
        }
        comprobarColumnas(columnas);
        for (String tabla : TABLAS) comprobarClavePrimaria(metadatos, tabla);
        comprobarReferencias(metadatos);
    }

    @Override public void reiniciar(Connection conexion, boolean confirmado) throws SQLException {
        if (!config.pruebas() || !confirmado)
            throw new IllegalArgumentException(
                    "El reinicio exige entorno de pruebas y --confirmar-pruebas");
        crear(conexion);
        // Solo vacía las tablas del caso, en orden de dependencias y sin cascada.
        boolean autoCommit = conexion.getAutoCommit();
        try {
            conexion.setAutoCommit(false);
            try (var sentencia = conexion.createStatement()) {
                for (String tabla : List.of("asignacion", "tutor", "estudiante", "empresa"))
                    sentencia.executeUpdate("DELETE FROM " + tabla);
            }
            conexion.commit();
        } catch (SQLException error) {
            conexion.rollback();
            throw error;
        } finally {
            conexion.setAutoCommit(autoCommit);
        }
    }

    private String ddl() throws SQLException {
        String recurso = config.sqlite()
                ? "/sql/ud02/sqlite.sql" : "/sql/ud02/postgresql.sql";
        try (InputStream entrada = EsquemaJdbc.class.getResourceAsStream(recurso)) {
            if (entrada == null)
                throw new SQLException("No se encontró el DDL " + recurso);
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new SQLException("No se pudo leer el DDL " + recurso, error);
        }
    }

    private static String comillas(String identificador) {
        return "\"" + identificador + "\"";
    }

    private String esquema() {
        return config.sqlite() ? null : config.esquema();
    }

    private boolean existeTabla(DatabaseMetaData metadatos, String tabla) throws SQLException {
        try (ResultSet filas =
                     metadatos.getTables(null, esquema(), tabla, new String[]{"TABLE"})) {
            return filas.next();
        }
    }

    private record Columna(String nombre, int tipo, String tipoNombre, int nulabilidad) {}

    private Map<String, Columna> columnasDe(DatabaseMetaData metadatos, String tabla)
            throws SQLException {
        var resultado = new LinkedHashMap<String, Columna>();
        try (ResultSet filas = metadatos.getColumns(null, esquema(), tabla, "%")) {
            while (filas.next()) {
                String nombre = filas.getString("COLUMN_NAME");
                resultado.put(nombre, new Columna(nombre, filas.getInt("DATA_TYPE"),
                        filas.getString("TYPE_NAME"), filas.getInt("NULLABLE")));
            }
        }
        return resultado;
    }

    private void comprobarColumnas(Map<String, Map<String, Columna>> reales) throws SQLException {
        for (String tabla : TABLAS) {
            Map<String, Columna> encontradas = reales.get(tabla);
            Map<String, Boolean> esperadas = columnasEsperadas(tabla);
            if (!encontradas.keySet().equals(esperadas.keySet()))
                throw new SQLException("Columnas inesperadas en " + tabla + ": " + encontradas.keySet());
            for (var esperada : esperadas.entrySet()) {
                Columna columna = encontradas.get(esperada.getKey());
                if (!admiteTipo(columna, esperada.getValue()))
                    throw new SQLException("Tipo incorrecto en " + tabla + "."
                            + columna.nombre() + ": " + columna.tipoNombre());
                if (columna.nulabilidad() != DatabaseMetaData.columnNoNulls)
                    throw new SQLException("La columna " + tabla + "."
                            + columna.nombre() + " debe ser NOT NULL");
            }
        }
    }

    /** true indica una fecha ISO (TEXT en SQLite, DATE en PostgreSQL). */
    private static Map<String, Boolean> columnasEsperadas(String tabla) {
        var columnas = new LinkedHashMap<String, Boolean>();
        columnas.put("id", false);
        switch (tabla) {
            case "empresa" -> columnas.put("nombre", false);
            case "estudiante" -> {
                columnas.put("nombre", false);
                columnas.put("ciclo", false);
            }
            case "tutor" -> {
                columnas.put("nombre", false);
                columnas.put("empresa_id", false);
            }
            case "asignacion" -> {
                columnas.put("estudiante_id", false);
                columnas.put("empresa_id", false);
                columnas.put("tutor_id", false);
                columnas.put("inicio", true);
                columnas.put("fin", true);
            }
            default -> throw new IllegalStateException("Tabla desconocida: " + tabla);
        }
        return columnas;
    }

    private boolean admiteTipo(Columna columna, boolean fecha) {
        if (fecha && !config.sqlite()) return columna.tipo() == Types.DATE;
        return esTexto(columna.tipo());
    }

    private static boolean esTexto(int tipo) {
        return switch (tipo) {
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR,
                 Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR,
                 Types.CLOB, Types.NCLOB -> true;
            default -> false;
        };
    }

    private void comprobarClavePrimaria(DatabaseMetaData metadatos, String tabla)
            throws SQLException {
        var columnas = new LinkedHashSet<String>();
        try (ResultSet filas = metadatos.getPrimaryKeys(null, esquema(), tabla)) {
            while (filas.next()) columnas.add(filas.getString("COLUMN_NAME"));
        }
        if (!columnas.equals(java.util.Set.of("id")))
            throw new SQLException("Clave primaria incorrecta en " + tabla + ": " + columnas);
    }

    private void comprobarReferencias(DatabaseMetaData metadatos) throws SQLException {
        var tutor = referencias(metadatos, "tutor");
        var tutorEsperadas = Map.of("empresa", Map.of("empresa_id", "id"));
        if (!tutor.equals(tutorEsperadas))
            throw new SQLException("Referencias incorrectas en tutor: " + tutor);

        var asignacion = referencias(metadatos, "asignacion");
        var asignacionEsperadas = Map.of(
                "estudiante", Map.of("estudiante_id", "id"),
                "empresa", Map.of("empresa_id", "id"),
                "tutor", Map.of("tutor_id", "id", "empresa_id", "empresa_id"));
        if (!asignacion.equals(asignacionEsperadas))
            throw new SQLException("Referencias incorrectas en asignacion: " + asignacion);
    }

    private Map<String, Map<String, String>> referencias(DatabaseMetaData metadatos, String tabla)
            throws SQLException {
        var porTabla = new LinkedHashMap<String, Map<String, String>>();
        try (ResultSet filas = metadatos.getImportedKeys(null, esquema(), tabla)) {
            while (filas.next()) {
                porTabla.computeIfAbsent(filas.getString("PKTABLE_NAME"),
                                clave -> new LinkedHashMap<>())
                        .put(filas.getString("FKCOLUMN_NAME"), filas.getString("PKCOLUMN_NAME"));
            }
        }
        return porTabla;
    }

    private void comprobarClavesForaneasActivas(Connection conexion) throws SQLException {
        try (var sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery("PRAGMA foreign_keys")) {
            if (!filas.next() || filas.getInt(1) != 1)
                throw new SQLException("Las claves foráneas de SQLite no están activas");
        }
    }
}
