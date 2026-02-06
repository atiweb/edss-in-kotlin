package com.atiweb.edss

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class EdssCalculatorTest {

    private lateinit var calculator: EdssCalculator

    @BeforeEach
    fun setUp() {
        calculator = EdssCalculator()
    }

    // ─── Visual Score Conversion ─────────────────────────────────

    @Test
    fun `Visual score conversion 0-6 to 0-4`() {
        // 0 → 0, 1 → 1, 2-3 → 2, 4-5 → 3, 6 → 4
        assertEquals(0, EdssCalculator.convertVisualScore(0))
        assertEquals(1, EdssCalculator.convertVisualScore(1))
        assertEquals(2, EdssCalculator.convertVisualScore(2))
        assertEquals(2, EdssCalculator.convertVisualScore(3))
        assertEquals(3, EdssCalculator.convertVisualScore(4))
        assertEquals(3, EdssCalculator.convertVisualScore(5))
        assertEquals(4, EdssCalculator.convertVisualScore(6))
    }

    // ─── Bowel & Bladder Score Conversion ────────────────────────

    @Test
    fun `Bowel and Bladder score conversion 0-6 to 0-5`() {
        // 0 → 0, 1 → 1, 2 → 2, 3-4 → 3, 5 → 4, 6 → 5
        assertEquals(0, EdssCalculator.convertBowelAndBladderScore(0))
        assertEquals(1, EdssCalculator.convertBowelAndBladderScore(1))
        assertEquals(2, EdssCalculator.convertBowelAndBladderScore(2))
        assertEquals(3, EdssCalculator.convertBowelAndBladderScore(3))
        assertEquals(3, EdssCalculator.convertBowelAndBladderScore(4))
        assertEquals(4, EdssCalculator.convertBowelAndBladderScore(5))
        assertEquals(5, EdssCalculator.convertBowelAndBladderScore(6))
    }

    // ─── Helper Functions ────────────────────────────────────────

    @Test
    fun `findMaxAndCount returns correct max and count`() {
        assertEquals(Pair(3, 2), EdssCalculator.findMaxAndCount(listOf(1, 3, 2, 3, 0)))
        assertEquals(Pair(5, 1), EdssCalculator.findMaxAndCount(listOf(1, 5, 2, 3, 0)))
        assertEquals(Pair(0, 7), EdssCalculator.findMaxAndCount(listOf(0, 0, 0, 0, 0, 0, 0)))
    }

    @Test
    fun `findSecondMaxAndCount returns correct second max and count`() {
        assertEquals(Pair(2, 1), EdssCalculator.findSecondMaxAndCount(listOf(1, 3, 2, 3, 0), 3))
        assertEquals(Pair(3, 2), EdssCalculator.findSecondMaxAndCount(listOf(1, 5, 3, 3, 0), 5))
        assertEquals(Pair(0, 0), EdssCalculator.findSecondMaxAndCount(listOf(3, 3, 3), 3))
    }

    // ─── calculateFromMap ────────────────────────────────────────

    @Test
    fun `calculateFromMap returns null on empty data`() {
        assertNull(calculator.calculateFromMap(emptyMap()))
    }

    @Test
    fun `calculateFromMap returns null on incomplete data`() {
        assertNull(calculator.calculateFromMap(mapOf(
            "visual_functions_score" to "0",
            "brainstem_functions_score" to "0"
        )))
    }

    @Test
    fun `calculateFromMap with default English fields`() {
        val data = mapOf(
            "visual_functions_score" to "1",
            "brainstem_functions_score" to "2",
            "pyramidal_functions_score" to "1",
            "cerebellar_functions_score" to "3",
            "sensory_functions_score" to "1",
            "bowel_and_bladder_functions_score" to "4",
            "cerebral_functions_score" to "2",
            "ambulation_score" to "1"
        )
        assertEquals("4", calculator.calculateFromMap(data))
    }

    @Test
    fun `calculateFromMap with REDCap Portuguese fields`() {
        val data = mapOf(
            "edss_func_visuais" to "1",
            "edss_cap_func_tronco_cereb" to "2",
            "edss_cap_func_pirad" to "1",
            "edss_cap_func_cereb" to "3",
            "edss_cap_func_sensitivas" to "1",
            "edss_func_vesicais_e_instestinais" to "4",
            "edss_func_cerebrais" to "2",
            "edss_func_demabulacao_incapacidade" to "1"
        )
        assertEquals("4", calculator.calculateFromMap(data, EdssCalculator.FIELDS_REDCAP_PT))
    }

    @Test
    fun `calculateFromMap with suffix`() {
        val data = mapOf(
            "visual_functions_score_long" to "0",
            "brainstem_functions_score_long" to "0",
            "pyramidal_functions_score_long" to "0",
            "cerebellar_functions_score_long" to "0",
            "sensory_functions_score_long" to "0",
            "bowel_and_bladder_functions_score_long" to "0",
            "cerebral_functions_score_long" to "0",
            "ambulation_score_long" to "0"
        )
        assertEquals("0", calculator.calculateFromMap(data, suffix = "_long"))
    }

    @Test
    fun `calculateFromMap with REDCap suffix`() {
        val data = mapOf(
            "edss_func_visuais_long" to "0",
            "edss_cap_func_tronco_cereb_long" to "0",
            "edss_cap_func_pirad_long" to "0",
            "edss_cap_func_cereb_long" to "0",
            "edss_cap_func_sensitivas_long" to "0",
            "edss_func_vesicais_e_instestinais_long" to "0",
            "edss_func_cerebrais_long" to "0",
            "edss_func_demabulacao_incapacidade_long" to "0"
        )
        assertEquals("0", calculator.calculateFromMap(data, EdssCalculator.FIELDS_REDCAP_PT, "_long"))
    }

    @Test
    fun `calculateFromMap with custom field mapping`() {
        val customFields = mapOf(
            "visual" to "fs_visual",
            "brainstem" to "fs_brainstem",
            "pyramidal" to "fs_pyramidal",
            "cerebellar" to "fs_cerebellar",
            "sensory" to "fs_sensory",
            "bowelBladder" to "fs_bowel_bladder",
            "cerebral" to "fs_cerebral",
            "ambulation" to "fs_ambulation"
        )
        val data = mapOf(
            "fs_visual" to "0",
            "fs_brainstem" to "0",
            "fs_pyramidal" to "2",
            "fs_cerebellar" to "0",
            "fs_sensory" to "0",
            "fs_bowel_bladder" to "0",
            "fs_cerebral" to "2",
            "fs_ambulation" to "0"
        )
        assertEquals("2.5", calculator.calculateFromMap(data, customFields))
    }

    // ─── EDSS Calculation (parameterized) ────────────────────────

    companion object {
        @JvmStatic
        fun edssDataProvider(): List<Arguments> = listOf(
            // ── EDSS 0 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  0, "0"),

            // ── EDSS 1.0 ──
            Arguments.of(0, 0, 1, 0, 0, 0, 0,  0, "1"),
            Arguments.of(0, 0, 0, 0, 0, 1, 0,  0, "1"),
            Arguments.of(0, 0, 0, 1, 0, 0, 0,  0, "1"),
            Arguments.of(1, 0, 0, 0, 0, 0, 0,  0, "1"),

            // ── EDSS 1.5 ──
            Arguments.of(0, 1, 1, 0, 0, 0, 0,  0, "1.5"),
            Arguments.of(1, 1, 1, 1, 1, 1, 1,  0, "1.5"),

            // ── EDSS 2.0 ──
            Arguments.of(0, 0, 2, 0, 0, 0, 0,  0, "2"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  1, "2"),
            Arguments.of(1, 1, 1, 1, 1, 1, 1,  1, "2"),
            Arguments.of(2, 0, 0, 0, 0, 0, 0,  0, "2"),
            Arguments.of(3, 0, 0, 0, 0, 0, 0,  0, "2"),
            Arguments.of(0, 0, 0, 0, 0, 2, 0,  0, "2"),

            // ── EDSS 2.5 ──
            Arguments.of(0, 0, 2, 2, 0, 0, 0,  0, "2.5"),
            Arguments.of(2, 2, 0, 0, 0, 0, 0,  0, "2.5"),

            // ── EDSS 3.0 ──
            Arguments.of(0, 0, 2, 2, 2, 0, 0,  0, "3"),
            Arguments.of(0, 0, 2, 2, 2, 2, 0,  0, "3"),
            Arguments.of(0, 3, 0, 0, 0, 0, 0,  0, "3"),
            Arguments.of(1, 3, 1, 1, 1, 1, 1,  0, "3"),
            Arguments.of(0, 0, 0, 0, 0, 3, 0,  0, "3"),
            Arguments.of(0, 0, 0, 0, 0, 4, 0,  0, "3"),
            Arguments.of(4, 0, 0, 0, 0, 0, 0,  0, "3"),
            Arguments.of(5, 0, 0, 0, 0, 0, 0,  0, "3"),

            // ── EDSS 3.5 ──
            Arguments.of(2, 2, 2, 2, 2, 0, 0,  0, "3.5"),
            Arguments.of(0, 0, 3, 2, 0, 0, 0,  0, "3.5"),
            Arguments.of(2, 0, 0, 0, 0, 0, 3,  0, "3.5"),
            Arguments.of(0, 0, 0, 3, 0, 0, 3,  0, "3.5"),
            Arguments.of(0, 0, 0, 3, 1, 0, 3,  0, "3.5"),

            // ── EDSS 4.0 ──
            Arguments.of(2, 2, 2, 2, 2, 2, 0,  0, "4"),
            Arguments.of(2, 2, 2, 2, 2, 2, 2,  0, "4"),
            Arguments.of(0, 0, 4, 0, 0, 0, 0,  0, "4"),
            Arguments.of(1, 1, 1, 1, 1, 1, 4,  0, "4"),
            Arguments.of(0, 0, 0, 3, 2, 0, 3,  0, "4"),
            Arguments.of(2, 3, 3, 3, 2, 2, 3,  0, "4"),
            Arguments.of(2, 2, 2, 0, 0, 0, 3,  0, "4"),
            Arguments.of(6, 0, 0, 0, 0, 0, 0,  0, "4"),
            Arguments.of(0, 0, 0, 0, 0, 5, 0,  0, "4"),

            // ── EDSS 4.5 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  2, "4.5"),
            Arguments.of(0, 0, 4, 3, 0, 0, 0,  0, "4.5"),
            Arguments.of(0, 2, 0, 0, 0, 0, 4,  0, "4.5"),
            Arguments.of(0, 4, 3, 3, 0, 0, 0,  0, "4.5"),
            Arguments.of(1, 3, 3, 3, 3, 1, 3,  0, "4.5"),
            Arguments.of(5, 1, 1, 1, 1, 1, 1,  2, "4.5"),
            Arguments.of(0, 0, 0, 0, 0, 0, 4,  2, "4.5"),

            // ── EDSS 5.0 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  3, "5"),
            Arguments.of(0, 0, 4, 4, 0, 0, 0,  0, "5"),
            Arguments.of(0, 5, 0, 0, 0, 0, 0,  0, "5"),
            Arguments.of(0, 0, 5, 0, 0, 0, 0,  0, "5"),
            Arguments.of(0, 0, 0, 0, 5, 0, 0,  0, "5"),
            Arguments.of(0, 0, 0, 0, 0, 6, 0,  0, "5"),
            Arguments.of(4, 3, 3, 3, 3, 3, 3,  0, "5"),
            Arguments.of(1, 4, 3, 3, 3, 1, 1,  0, "5"),

            // ── EDSS 5.5 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  4, "5.5"),

            // ── EDSS 6.0 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  5, "6"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  6, "6"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  7, "6"),

            // ── EDSS 6.5 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  8, "6.5"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0,  9, "6.5"),

            // ── EDSS 7.0 – 10.0 ──
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 10, "7"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 11, "7.5"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 12, "8"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 13, "8.5"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 14, "9"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 15, "9.5"),
            Arguments.of(0, 0, 0, 0, 0, 0, 0, 16, "10"),

            // ── Reference example from JS repo ──
            Arguments.of(1, 2, 1, 3, 1, 4, 2,  1, "4")
        )
    }

    @ParameterizedTest(name = "EDSS({0},{1},{2},{3},{4},{5},{6},{7}) = {8}")
    @MethodSource("edssDataProvider")
    fun `EDSS calculation`(
        visual: Int, brainstem: Int, pyramidal: Int, cerebellar: Int,
        sensory: Int, bowelBladder: Int, cerebral: Int, ambulation: Int,
        expected: String
    ) {
        val result = calculator.calculate(
            visual, brainstem, pyramidal, cerebellar,
            sensory, bowelBladder, cerebral, ambulation
        )
        assertEquals(
            expected, result,
            "EDSS mismatch for Visual=$visual Brainstem=$brainstem Pyramidal=$pyramidal " +
            "Cerebellar=$cerebellar Sensory=$sensory BowelBladder=$bowelBladder " +
            "Cerebral=$cerebral Ambulation=$ambulation: expected $expected, got $result"
        )
    }
}
