package fct.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** UD2: usa la URL y Properties validadas; el cliente cierra la conexión. */
public class FabricaConexiones {
    private final ConfiguracionBD config;
    public FabricaConexiones(ConfiguracionBD config) { this.config = config; }

    public Connection abrir() throws SQLException {
        Connection conexion = DriverManager.getConnection(config.url(), config.propiedades());
        boolean lista = false;
        try {
            if (config.sqlite()) activarClavesForaneas(conexion);
            lista = true;
            return conexion;
        } finally {
            if (!lista) conexion.close();
        }
    }

    private static void activarClavesForaneas(Connection conexion) throws SQLException {
        // SQLite las desactiva por conexión: hay que activarlas en cada apertura.
        try (var sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        }
    }
}
