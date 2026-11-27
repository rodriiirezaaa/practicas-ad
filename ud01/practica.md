# Práctica 1. Primera persistencia del caso FCT

## Ejecución

- Comandos utilizados y configuración necesaria:

  Se necesita JDK 21 y Maven (el `devcontainer` del repositorio ya los prepara).
  Desde la raíz del repositorio:

  ```bash
  mvn -f ud01/practica/pom.xml test
  mvn -f ud01/practica/pom.xml -Pcontrato clean verify
  mvn -f ud01/practica/pom.xml compile exec:java -Dexec.mainClass=fct.DemoFct
  ```

  En mi entorno no había JDK/Maven del sistema, así que usé Temurin 21 y Maven
  3.9.9 sin instalarlos en el sistema, exportando `JAVA_HOME` y añadiendo sus
  `bin` al `PATH`. No hay otra configuración especial: Jackson, Commons CSV y
  JUnit se resuelven desde el `pom.xml`.

- Resultado de la aceptación y de la demo:

  - `test`: `Tests run: 3, Failures: 0, Errors: 0` (pruebas base).
  - `-Pcontrato clean verify`: `Tests run: 23, Failures: 0, Errors: 0`
    (3 base + 20 de contrato/aceptación). `BUILD SUCCESS`.
  - Demo:

    ```text
    Relectura JSON correcta
    Empresa B modificada
    CSV generado
    Fallo simulado antes del reemplazo
    Fichero anterior conservado; sin notificación de éxito en el fallo
    ```

## Comprobaciones

| Caso | Resultado esperado | Resultado observado y prueba/archivo |
|---|---|---|
| Guardar y recuperar JSON | Una instancia nueva recupera los mismos valores y relaciones; el estado vacío también se guarda y relee; ausencia, cero bytes y documento incompatible se rechazan | Correcto. `ContratoFicherosTest.nuevaInstanciaRecuperaValoresYRelaciones`, `vacioValidoSeGuardaYRecupera`, `ficheroAusenteNoDevuelveUnEstadoVacio`, `contenidoInvalidoNoSePresentaComoVacio`, `esquemaIncompatibleSeRechaza`. La demo imprime `Relectura JSON correcta` |
| Exportar y releer CSV | Cabecera exacta `nombre;ciclo;empresa`, una fila por asignación en el orden de la lista, con nombre/ciclo de estudiante y nombre de empresa; comillas, `;` y saltos de línea conservados; sin asignaciones solo cabecera | Correcto. `ContratoFicherosTest.csvConservaDelimitadoresYComillasAlReleer` y `csvVacioConservaCabeceraYVariasAsignacionesConservanOrden`. La demo imprime `CSV generado` y escribe `datos-demo/asignaciones.csv` |
| Fallo controlado al guardar | El fallo ocurre tras escribir el temporal y antes de reemplazar: se conservan los bytes anteriores, se borra el temporal y no se emite `"estado guardado"` | Correcto. `ContratoFicherosTest.falloSimuladoConservaBytesYLimpiaTemporalSinNotificar` y `elAvisoSeEmiteCuandoElEstadoYaPuedeReleerse`. La demo confirma que el fichero anterior se conserva y no hay aviso de éxito |

- Copia, movimiento y borrado: lo demuestra
  `ContratoFicherosTest.unaCopiaMovidaSeRecuperaDesdeOtraRutaSinAlterarElOriginal`.
  Copia `estado.json` a `copia.json`, mueve la copia a `movida.json`, la recupera
  desde esa ruta, borra `movida.json` y vuelve a recuperar el original. Copiar y
  mover actúan sobre una copia (nuevo fichero); el original no se abre para
  escritura, por eso sus bytes y su contenido siguen intactos.

## Decisiones y límites

- Qué hace el servicio y qué hace el almacén: `ServicioFct` implementa las
  operaciones de dominio (altas, modificaciones, búsquedas, filtro por ciclo y
  asignaciones) y valida antes de delegar. No conoce el formato de persistencia:
  solo usa el puerto `AlmacenFct`. `RepositorioJson` es el adaptador que traduce
  `Estado` a/desde JSON, valida y escribe de forma segura. La ventaja es poder
  cambiar el almacenamiento (memoria, JSON, otro formato) sin tocar el servicio y
  poder probar el servicio contra un almacén en memoria.
- Una decisión de mi implementación y su motivo: valido el estado antes de
  serializar y escribir, para que un estado inválido no llegue nunca al disco;
  además notifico `"estado guardado"` solo después de que
  `EscrituraSegura.reemplazar` termina correctamente, de modo que un fallo no
  emite un falso éxito. En el CSV resuelvo los identificadores con mapas
  `id → objeto` para exportar nombre, ciclo y empresa en vez de sus ids.
- Qué garantiza el guardado y qué queda fuera: `EscrituraSegura` escribe un
  temporal en la misma carpeta que el destino y lo mueve con `ATOMIC_MOVE` +
  `REPLACE_EXISTING`, borrando el temporal en `finally`. Así, si falla antes del
  reemplazo se conserva el fichero anterior. Si el sistema no admite movimiento
  atómico, el error se propaga y no se degrada a escritura directa. Queda fuera:
  transacciones entre varios ficheros, control de concurrencia, sincronización
  con `fsync` y recuperación ante cualquier corte de alimentación.
- Para qué utilicé Java/Maven, Jackson, Commons CSV y JUnit: Java 21 y Maven
  compilan, empaquetan y ejecutan el proyecto y sus pruebas con las dependencias
  del `pom.xml`. Jackson convierte `Estado` a JSON y de JSON al modelo. Commons
  CSV escribe (y permite releer) el CSV con delimitador `;`, comillas y saltos de
  línea correctos. JUnit 5 ejecuta las pruebas base y las de aceptación, usando
  `@TempDir` para trabajar sobre ficheros temporales.
- Aspectos pendientes, si los hay: XML se trabajará en la última unidad. El CSV
  es solo una proyección de asignaciones: no hay importador CSV ni se recuperan
  tutores, fechas o el estado completo desde él. No se añade soporte de
  concurrencia ni de transacciones multifichero.

## Mi revisión

| Criterio de la rúbrica | Nivel estimado | Evidencia |
|---|---|---|
| Gestión de ficheros | Correcto | Rutas configurables (`Path` en constructores/parámetros) y prueba de copia/movimiento/borrado sin alterar el original |
| Recuperación JSON | Correcto | Relectura desde instancia nueva, estado vacío válido y rechazo de ausencia, contenido inválido y esquema incompatible |
| Almacenamiento e intercambio | Correcto | JSON completo con valores/relaciones, CSV `nombre;ciclo;empresa` y conservación del fichero anterior ante el fallo simulado |
| Comprobación y documentación | Correcto | Aceptación (23 pruebas) y demo ejecutadas; resultados normales y de error con comandos reproducibles en esta ficha |
| Diseño del componente | Correcto | Separación servicio/almacén mediante `AlmacenFct`, ventaja y límites de la persistencia en ficheros explicados |
| Herramientas | Correcto | Uso explicado de Java/Maven, Jackson, Commons CSV y JUnit; `target/` y `datos-demo/` son salidas generadas, no fuentes |
| Integración del componente | Correcto | `RepositorioJson` cumple `AlmacenFct`, ruta configurable y aviso posterior al reemplazo; `ServicioFct` funciona con él |

- Qué detecté y mejoré al revisar: comprobé que la validación ocurre antes de
  escribir (un estado inválido no toca el disco), que el aviso se emite después
  del reemplazo correcto (la demo y `elAvisoSeEmiteCuandoElEstadoYaPuedeReleerse`
  lo confirman) y que el CSV resuelve las relaciones a nombres reales,
  conservando caracteres especiales. También aseguré la creación de la carpeta
  de destino del CSV.
- Ayuda utilizada y comprobaciones realizadas: usé IA (opencode/DeepSeek) para
  implementar los tres métodos y redactar esta ficha. Verifiqué el resultado
  ejecutando los tres comandos de la sección de ejecución y revisando que las 23
  pruebas de aceptación y la demo terminan correctamente.
- Contraste con IA, si lo utilicé: la IA propuso validar antes de serializar,
  notificar solo tras el reemplazo y usar `ATOMIC_MOVE` con propagación del error;
  acepté esas observaciones porque coinciden con el enunciado y quedan
  respaldadas por las pruebas. Descarté añadir un parser/importador CSV propio,
  ya que el enunciado lo excluye.
