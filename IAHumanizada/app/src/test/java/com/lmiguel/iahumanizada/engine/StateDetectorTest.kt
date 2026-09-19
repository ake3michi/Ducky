package com.lmiguel.iahumanizada.engine

import com.lmiguel.iahumanizada.data.model.EstadoTipo
import org.junit.Assert.assertEquals
import org.junit.Test

class StateDetectorTest {

    private val detector = StateDetector()

    @Test
    fun `mensaje neutro sin senales da NEUTRO`() {
        val estado = detector.detectar("pásame el archivo")
        assertEquals(EstadoTipo.NEUTRO, estado.tipo)
    }

    @Test
    fun `saludo simple detecta CALIDO`() {
        val estado = detector.detectar("hola, buenos días, gracias por todo")
        assertEquals(EstadoTipo.CALIDO, estado.tipo)
    }

    @Test
    fun `palabra tecnica dentro de otra palabra no debe disparar falso positivo`() {
        // Antes "api" se buscaba como substring, y "capital" contiene "api" (c-api-tal).
        // Con límites de palabra reales esto ya no debe contar como TECNICO.
        val estado = detector.detectar("¿cuál es la capital de Francia?")
        assertEquals(EstadoTipo.NEUTRO, estado.tipo)
    }

    @Test
    fun `mencion real de una palabra tecnica si detecta TECNICO`() {
        val estado = detector.detectar("tengo un error raro en esta funcion de kotlin")
        assertEquals(EstadoTipo.TECNICO, estado.tipo)
    }

    @Test
    fun `palabra con tilde coincide con su version sin tilde en el codigo`() {
        // "reflexion" en el código no tiene tilde; "reflexión" del usuario sí.
        // Antes esto nunca coincidía por no normalizar acentos.
        val estado = detector.detectar("esto me lleva a una reflexión profunda sobre la vida")
        assertEquals(EstadoTipo.REFLEXIVO, estado.tipo)
    }

    @Test
    fun `risas detectan JUGUETON`() {
        val estado = detector.detectar("jajaja q bueno eso, no lo esperaba")
        assertEquals(EstadoTipo.JUGUETON, estado.tipo)
    }

    @Test
    fun `frase explicita de silencio devuelve SILENCIOSO sin importar el resto`() {
        val estado = detector.detectar("no quiero hablar ahora, jajaja")
        assertEquals(EstadoTipo.SILENCIOSO, estado.tipo)
    }

    @Test
    fun `mensaje corto tras pausa muy larga es SILENCIOSO`() {
        val estado = detector.detectar("ok", pausaSegundos = 400)
        assertEquals(EstadoTipo.SILENCIOSO, estado.tipo)
    }

    @Test
    fun `mensaje muy largo tiende a REFLEXIVO`() {
        val textoLargo = (1..25).joinToString(" ") { "palabra$it" }
        val estado = detector.detectar(textoLargo)
        assertEquals(EstadoTipo.REFLEXIVO, estado.tipo)
    }
}
