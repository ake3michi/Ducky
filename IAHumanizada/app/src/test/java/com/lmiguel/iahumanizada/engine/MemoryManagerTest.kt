package com.lmiguel.iahumanizada.engine

import androidx.test.core.app.ApplicationProvider
import com.lmiguel.iahumanizada.data.model.LearningCategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MemoryManagerTest {

    private lateinit var dao: FakeMemoryDao
    private lateinit var manager: MemoryManager

    @Before
    fun setUp() {
        dao = FakeMemoryDao()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        manager = MemoryManager(dao, context)
    }

    @Test
    fun `store guarda una memoria nueva`() = runTest {
        manager.store("mem_1", "el usuario dijo hola")
        assertEquals(1, dao.memories.size)
        assertEquals("el usuario dijo hola", dao.memories["mem_1"]?.content)
    }

    @Test
    fun `store sobre una key existente incrementa hits en vez de duplicar`() = runTest {
        manager.store("mem_1", "primer contenido")
        manager.store("mem_1", "contenido actualizado")
        assertEquals(1, dao.memories.size)
        assertEquals(2, dao.memories["mem_1"]?.hits)
        assertEquals("contenido actualizado", dao.memories["mem_1"]?.content)
    }

    @Test
    fun `learn crea un aprendizaje nuevo con refuerzo 1`() = runTest {
        manager.learn("patron_1", "le gusta el humor", LearningCategory.PREFERENCE, "sensual")
        assertEquals(1, dao.learnings["patron_1"]?.reinforcementCount)
    }

    @Test
    fun `learn repetido va reforzando el mismo aprendizaje`() = runTest {
        repeat(3) {
            manager.learn("patron_1", "le gusta el humor", LearningCategory.PREFERENCE, "sensual")
        }
        // La primera llamada inserta (count=1); las siguientes 2 refuerzan (+1 c/u) => 3
        assertEquals(3, dao.learnings["patron_1"]?.reinforcementCount)
    }

    @Test
    fun `tras 5 refuerzos el aprendizaje se promueve al Soul`() = runTest {
        repeat(5) {
            manager.learn("patron_1", "suele estar CALIDO contigo", LearningCategory.PREFERENCE, "sensual")
        }
        val soul = manager.getSoul("sensual")
        assertTrue(soul.contains("suele estar CALIDO contigo"))
    }

    @Test
    fun `getTopReinforced no mezcla aprendizajes de otra alma`() = runTest {
        manager.learn("patron_a", "es del alma sensual", LearningCategory.PREFERENCE, "sensual")
        manager.learn("patron_b", "es del alma tecnica", LearningCategory.PREFERENCE, "tecnica")

        val contextoSensual = manager.buildContext("sensual")
        assertTrue(contextoSensual.contains("es del alma sensual"))
        assertTrue(!contextoSensual.contains("es del alma tecnica"))
    }

    @Test
    fun `olvidarDebiles borra memorias viejas y poco reforzadas`() = runTest {
        val hace30Dias = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        dao.memories["vieja"] = com.lmiguel.iahumanizada.data.model.Memory(
            key = "vieja", content = "algo poco relevante", timestamp = hace30Dias, hits = 1
        )
        dao.memories["reciente"] = com.lmiguel.iahumanizada.data.model.Memory(
            key = "reciente", content = "algo de ahora", timestamp = System.currentTimeMillis(), hits = 1
        )

        manager.olvidarDebiles()

        assertEquals(null, dao.memories["vieja"])
        assertTrue(dao.memories.containsKey("reciente"))
    }
}
