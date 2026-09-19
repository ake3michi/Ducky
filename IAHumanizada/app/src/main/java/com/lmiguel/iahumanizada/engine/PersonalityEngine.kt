package com.lmiguel.iahumanizada.engine

import com.lmiguel.iahumanizada.data.model.Afinacion
import com.lmiguel.iahumanizada.data.model.Alma
import com.lmiguel.iahumanizada.data.model.Estado
import com.lmiguel.iahumanizada.data.model.EstadoTipo
import com.lmiguel.iahumanizada.data.model.LearningCategory
import com.lmiguel.iahumanizada.data.repository.AlmaRepository

class PersonalityEngine(
    private val almaRepository: AlmaRepository,
    private val memoryManager: MemoryManager
) {

    private var almaActual: Alma? = null
    private var afinacionActual: Afinacion = Afinacion()
    private val detector = StateDetector()
    private var ultimoTimestamp = System.currentTimeMillis()

    // Seleccionar alma activa
    suspend fun seleccionarAlma(id: String): Alma? {
        val alma = almaRepository.obtenerAlmaPorId(id)
        almaActual = alma
        return alma
    }

    // Obtener alma actual
    fun obtenerAlmaActual(): Alma? = almaActual

    // === Gestión de almas (crear/editar/borrar) ===
    // Antes las almas solo vivían en res/raw/*.json, así que cambiarlas
    // exigía tocar código y recompilar. Ahora viven en Room y se pueden
    // editar/crear desde la propia app.

    suspend fun listarAlmas(): List<Alma> = almaRepository.cargarTodas()

    suspend fun guardarAlma(alma: Alma) {
        almaRepository.guardarAlma(alma)
        // Si estábamos editando el alma activa, refrescamos la referencia en memoria
        if (almaActual?.id == alma.id) almaActual = alma
    }

    suspend fun duplicarAlma(original: Alma, nuevoId: String, nuevoNombre: String): Alma =
        almaRepository.duplicarAlma(original, nuevoId, nuevoNombre)

    suspend fun eliminarAlma(alma: Alma): Boolean = almaRepository.eliminarAlma(alma)

    // Actualizar afinación desde sliders
    fun actualizarAfinacion(afinacion: Afinacion) {
        afinacionActual = afinacion
    }

    // Procesar mensaje y generar prompt
    suspend fun procesarMensaje(mensajeUsuario: String, contexto: String = ""): ResultadoMotor {
        val alma = almaActual ?: almaRepository.obtenerAlmaPorId("neutra")!!

        // Medir pausa
        val ahora = System.currentTimeMillis()
        val pausaSegundos = (ahora - ultimoTimestamp) / 1000
        ultimoTimestamp = ahora

        // Detectar estado
        val estado = detector.detectar(mensajeUsuario, pausaSegundos)

        // Key compartida con el feedback explícito del usuario (ver
        // registrarFeedback): así un 👍/👎 en el chat refuerza o debilita
        // exactamente el mismo patrón que el aprendizaje automático detecta.
        val keyPatronEstado = "estado_frecuente_${alma.id}_${estado.tipo}"

        // Conectar el aprendizaje reforzado: si el usuario repite el mismo
        // estado emocional con esta alma, se va reforzando y, tras 5 veces,
        // MemoryManager lo promueve al Soul (antes esto nunca se llamaba).
        // NEUTRO no aporta señal, así que no lo reforzamos.
        if (estado.tipo != EstadoTipo.NEUTRO) {
            memoryManager.learn(
                key = keyPatronEstado,
                content = "Contigo, el usuario suele estar en un estado ${estado.tipo} durante la charla.",
                category = LearningCategory.PREFERENCE,
                archetypeId = alma.id
            )
        }

        // Calcular parámetros finales
        val parametrosFinales = calcularParametros(alma, afinacionActual, estado)

        // Obtener contexto de memoria
        val contextoMemoria = memoryManager.buildContext(alma.id)
        val contextoCompleto = if (contextoMemoria.isNotBlank())
            "$contextoMemoria\n\n$contexto" else contexto

        // Generar prompt maestro
        val prompt = generarPrompt(alma, parametrosFinales, estado, contextoCompleto, mensajeUsuario)

        return ResultadoMotor(
            prompt = prompt,
            respuestaSilenciosa = null, // El modelo (Ollama) responde siempre con su personalidad
            estado = estado,
            parametros = parametrosFinales,
            almaId = alma.id,
            feedbackKey = keyPatronEstado
        )
    }

    // Guarda cada intercambio en la memoria declarativa para que el contexto
    // reciente ("[CONTEXTO RECIENTE]") realmente se alimente con el tiempo.
    suspend fun registrarIntercambio(mensajeUsuario: String, respuestaIA: String) {
        val key = "mem_${System.currentTimeMillis()}"
        val contenido = "Usuario dijo: \"$mensajeUsuario\" → Respondiste: \"$respuestaIA\""
        memoryManager.store(key, contenido)
    }

    // Feedback explícito del usuario (👍/👎 en el chat) sobre el patrón de
    // estado detectado en ese momento. Antes MemoryManager.reinforce() nunca
    // se llamaba desde ningún lado; ahora este es su único punto de entrada.
    suspend fun registrarFeedback(archetypeId: String, key: String, positivo: Boolean) {
        if (positivo) {
            memoryManager.reinforce(key, archetypeId)
        } else {
            memoryManager.debilitar(key)
        }
    }

    // Poda memorias y aprendizajes débiles (poco reforzados y viejos).
    // Antes existía en MemoryManager pero nadie la llamaba nunca, así que la
    // memoria solo crecía sin límite.
    suspend fun limpiarMemoriaDebil() {
        memoryManager.olvidarDebiles()
    }

    private fun calcularParametros(
        alma: Alma,
        afinacion: Afinacion,
        estado: Estado
    ): Map<String, Float> {
        val base = alma.parametros.toMutableMap()

        // Mapeo flexible para soportar nombres como "tono_equilibrado" o "tono"
        fun getParam(prefijo: String): Float {
            return base.entries.find { it.key.startsWith(prefijo) }?.value ?: 0.5f
        }

        val finalParams = mutableMapOf<String, Float>()

        // Aplicar afinación sobre la base del alma
        finalParams["tono"] = (getParam("tono") * 0.7f + afinacion.tono * 0.3f).coerceIn(0f, 1f)
        finalParams["ritmo"] = (getParam("ritmo") * 0.6f + afinacion.ritmo * 0.4f).coerceIn(0f, 1f)
        finalParams["humor"] = (getParam("humor") + afinacion.humor * 0.5f).coerceIn(0f, 1f)
        finalParams["microgestos"] = (getParam("microgestos") * afinacion.microgestos).coerceIn(0f, 1f)
        finalParams["profundidad"] = (getParam("profundidad") * 0.5f + afinacion.profundidad * 0.5f).coerceIn(0f, 1f)
        finalParams["presencia"] = (getParam("presencia") * 0.5f + afinacion.presencia * 0.5f).coerceIn(0f, 1f)

        // Aplicar modificadores de estado
        when (estado.tipo) {
            EstadoTipo.CALIDO -> {
                finalParams["tono"] = (finalParams["tono"]!! + 0.3f).coerceIn(0f, 1f)
                finalParams["ritmo"] = (finalParams["ritmo"]!! - 0.2f).coerceIn(0f, 1f)
                finalParams["microgestos"] = (finalParams["microgestos"]!! + 0.4f).coerceIn(0f, 1f)
            }
            EstadoTipo.TECNICO -> {
                finalParams["tono"] = (finalParams["tono"]!! + 0.4f).coerceIn(0f, 1f)
                finalParams["ritmo"] = (finalParams["ritmo"]!! + 0.3f).coerceIn(0f, 1f)
                finalParams["humor"] = (finalParams["humor"]!! - 0.3f).coerceIn(0f, 1f)
            }
            EstadoTipo.JUGUETON -> {
                finalParams["humor"] = (finalParams["humor"]!! + 0.6f).coerceIn(0f, 1f)
                finalParams["ritmo"] = (finalParams["ritmo"]!! + 0.4f).coerceIn(0f, 1f)
                finalParams["microgestos"] = (finalParams["microgestos"]!! + 0.3f).coerceIn(0f, 1f)
            }
            EstadoTipo.SILENCIOSO -> {
                finalParams["ritmo"] = (finalParams["ritmo"]!! - 0.4f).coerceIn(0f, 1f)
                finalParams["humor"] = (finalParams["humor"]!! - 0.5f).coerceIn(0f, 1f)
            }
            EstadoTipo.REFLEXIVO -> {
                finalParams["profundidad"] = (finalParams["profundidad"]!! + 0.4f).coerceIn(0f, 1f)
                finalParams["ritmo"] = (finalParams["ritmo"]!! - 0.2f).coerceIn(0f, 1f)
            }
            EstadoTipo.NEUTRO -> { /* sin modificadores */ }
        }

        return finalParams
    }

    private fun generarPrompt(
        alma: Alma,
        parametros: Map<String, Float>,
        estado: Estado,
        contexto: String,
        mensajeUsuario: String
    ): String {
        val microgesto = if ((parametros["microgestos"] ?: 0f) > 0.5f)
            "${alma.microgestos.random()} " else ""

        return """
[IDENTIDAD]
Eres una IA con alma "${alma.nombre}". ${alma.descripcion}
Vocabulario característico: ${alma.vocabulario.joinToString(", ")}

[PARÁMETROS ACTIVOS]
- Tono: ${parametros["tono"]}
- Ritmo: ${parametros["ritmo"]}  
- Humor: ${parametros["humor"]}
- Profundidad: ${parametros["profundidad"]}
- Presencia: ${parametros["presencia"]}

[ESTADO ACTUAL]
El usuario está en estado: ${estado.tipo}
Adapta tu energía, ritmo y tono a este estado.

[ESTILO]
- Mantén coherencia con tu alma base.
- Microgestos disponibles: ${alma.microgestos.joinToString(", ")}
- Si el estado es SILENCIOSO → frases mínimas.
- Evita repeticiones. Mantén fluidez.

[CONTEXTO CONVERSACIONAL]
$contexto

[MENSAJE DEL USUARIO]
$mensajeUsuario

[RESPUESTA]
$microgesto
        """.trimIndent()
    }
}

data class ResultadoMotor(
    val prompt: String,
    val respuestaSilenciosa: String?,
    val estado: Estado,
    val parametros: Map<String, Float>,
    val almaId: String,
    val feedbackKey: String
)
