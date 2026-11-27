# Informe individual — AD · UD01 · Modelo A

- **Nombre:** Rodrigo Álvarez Reza
- **Usuario GitHub:** `rodriiirezaaa`
- **Fecha del examen:** 2026-11-25
- **Fecha de corrección:** 2026-11-26
- **Módulo / unidad:** AD · UD01
- **Modelo:** A

He contrastado la hoja con la clave A del cuadernillo utilizado.


## Resultado

| Aciertos | Errores | En blanco | Penalización | Puntuación |
| -------: | ------: | --------: | -----------: | ---------: |
| 27 | 3 | 10 | 2,50 | **65/100** |

Cálculo: 2,5 × (27 − 3/3) = **65**  
Comprobación: 27 + 3 + 10 = 40.

## Preguntas que fallaste

### Pregunta 14
- **Enunciado:** Se lee el texto de un CSV y se escribe sin cambios en un fichero llamado `salida.json`. ¿Qué se ha hecho?
- **Respuesta marcada:** D. Convertir los campos del CSV en propiedades JSON.
- **Respuesta correcta:** C. Copiar el texto a otro fichero sin transformar su estructura CSV.
- **Explicación:** Cambiar la extensión no convierte el formato; sería necesario interpretar el CSV y generar JSON.

### Pregunta 16
- **Enunciado:** ¿Qué afirmación distingue correctamente un fichero de texto de uno binario?
- **Respuesta marcada:** D. Un fichero binario no puede contener datos organizados.
- **Respuesta correcta:** B. Texto y binario describen cómo interpretamos su contenido; la extensión no garantiza el formato real.
- **Explicación:** Los datos binarios también pueden tener una estructura definida. El formato depende del contenido y de cómo se interpreta.

### Pregunta 28
- **Enunciado:** Un fichero contiene `Ana`, `Brais` y `Luz`. Se invoca dos veces `readLine()` y se imprime el segundo resultado. ¿Qué aparece?
- **Respuesta marcada:** D. Las tres líneas completas.
- **Respuesta correcta:** A. `Brais`.
- **Explicación:** La primera llamada consume `Ana`; la segunda devuelve `Brais`.

## En blanco

### Pregunta 1
- **Enunciado:** El contrato exige comunicar un error de lectura y no confundirlo con un estado vacío válido. `leer()` puede lanzar `IOException`. ¿Qué problema introduce capturarla y devolver `Estado.vacio()`?
- **Respuesta correcta:** B. Oculta un fallo de lectura como si fuera una recuperación correcta sin datos.
- **Explicación:** El `catch` convierte un error en un resultado normal. El código llamador ya no puede distinguir entre un estado realmente vacío y una lectura que fracasó.

### Pregunta 9
- **Enunciado:** Jackson convierte el literal JSON `null` en una referencia nula y la validación del dominio la rechaza. ¿Qué conclusión es correcta?
- **Respuesta correcta:** D. Una lectura sintácticamente válida puede ser rechazada después por la validación.
- **Explicación:** La sintaxis JSON, la conversión a objetos y las reglas del dominio son comprobaciones diferentes.

### Pregunta 15
- **Enunciado:** Se ejecutan `raf.seek(4L * 8)` y después `raf.seek(1L * 8)`. ¿Dónde comienza la siguiente lectura?
- **Respuesta correcta:** B. En el byte 8, al comienzo del registro con índice 1.
- **Explicación:** Cada `seek` establece una posición absoluta. La segunda llamada sustituye la primera.

### Pregunta 17
- **Enunciado:** `procesar` puede lanzar una excepción antes de ejecutar `r.close()`. ¿Qué modificación garantiza que se intente cerrar el lector?
- **Respuesta correcta:** A. Gestionar el lector con *try-with-resources* alrededor de `procesar`.
- **Explicación:** *Try-with-resources* intenta cerrar el recurso tanto al terminar normalmente como al salir por una excepción.

### Pregunta 21
- **Enunciado:** Un fichero contiene JSON válido. Se abre con `newBufferedWriter` y la serialización falla antes de escribir datos. ¿Qué riesgo existe?
- **Respuesta correcta:** C. La apertura puede haber truncado el fichero anterior aunque la serialización falle.
- **Explicación:** El escritor puede vaciar el destino al abrirlo. Cerrar el recurso no restaura su contenido.

### Pregunta 24
- **Enunciado:** El programa notifica «estado guardado» antes de llamar a `guardar(estado)`. ¿Cómo debe corregirse el orden?
- **Respuesta correcta:** B. Ejecutar primero `guardar` y notificar únicamente cuando termine sin error.
- **Explicación:** El mensaje de éxito debe reflejar el resultado real de la operación.

### Pregunta 25
- **Enunciado:** Python interpreta correctamente `{"fin":"2026-09-01"}`, pero el comienzo del convenio es posterior. ¿Qué se puede concluir?
- **Respuesta correcta:** A. El JSON puede parsearse, pero la fecha debe validarse frente a las reglas del dominio.
- **Explicación:** El parser comprueba sintaxis, no coherencia. La aplicación debe verificar que el final no sea anterior al comienzo.

### Pregunta 30
- **Enunciado:** `validar(estado)` rechaza estados inválidos y `escribir(estado)` modifica el fichero. ¿Qué orden evita modificarlo si falla la validación?
- **Respuesta correcta:** A. `validar(estado); escribir(estado);`
- **Explicación:** La escritura solo debe ejecutarse si la validación termina correctamente.

### Pregunta 33
- **Enunciado:** Se captura una `IOException`, se registra y después se ejecuta `throw e`. ¿Qué efecto tiene?
- **Respuesta correcta:** D. Registra y propaga el fallo al llamador.
- **Explicación:** Registrar deja constancia; `throw e` permite que el nivel superior sepa que la lectura falló.

### Pregunta 40
- **Enunciado:** Un fichero UTF-8 guarda nombres de longitud variable. Se propone localizar el nombre `n` mediante `raf.seek(n * 10L)`. ¿Es correcto?
- **Respuesta correcta:** D. No permite localizarlo de forma general sin conocer sus posiciones reales en bytes.
- **Explicación:** Los registros no están rellenados hasta una longitud fija. La codificación, la longitud variable y los saltos de línea impiden calcular así la posición.

## Recomendaciones de repaso

- Diferencia entre sintaxis, formato y validación del dominio.
- Gestión de recursos y propagación de excepciones.
- Escritura segura: validar antes de modificar y no anunciar operaciones fallidas.
- Acceso secuencial y aleatorio a ficheros.
- Conversión real entre CSV y JSON.
