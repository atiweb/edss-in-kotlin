package com.atiweb.edss

/**
 * EDSS (Expanded Disability Status Scale) Calculator.
 *
 * Calculates the EDSS score based on 7 Functional System (FS) scores and an Ambulation score,
 * following the scoring table by Ludwig Kappos, MD (University Hospital Basel) and the
 * Neurostatus-EDSS™ standard (Kurtzke, 1983).
 *
 * Functional Systems (in Neurostatus-EDSS™ standard order):
 *   1. Visual (Optic)         — raw 0-6, converted to 0-4 for EDSS calculation
 *   2. Brainstem              — 0-5
 *   3. Pyramidal              — 0-6
 *   4. Cerebellar             — 0-5
 *   5. Sensory                — 0-6
 *   6. Bowel & Bladder        — raw 0-6, converted to 0-5 for EDSS calculation
 *   7. Cerebral (Mental)      — 0-5
 *   8. Ambulation             — 0-16 (determines EDSS ≥ 5.0 directly)
 *
 * @see <a href="https://github.com/atiweb/edss">JavaScript reference implementation</a>
 * @see <a href="https://www.neurostatus.net/">Neurostatus-EDSS™</a>
 */
class EdssCalculator {

    companion object {
        /**
         * Default English field names for [calculateFromMap].
         *
         * These match the parameter names used in the JS reference implementation.
         */
        val FIELDS_DEFAULT: Map<String, String> = mapOf(
            "visual" to "visual_functions_score",
            "brainstem" to "brainstem_functions_score",
            "pyramidal" to "pyramidal_functions_score",
            "cerebellar" to "cerebellar_functions_score",
            "sensory" to "sensory_functions_score",
            "bowelBladder" to "bowel_and_bladder_functions_score",
            "cerebral" to "cerebral_functions_score",
            "ambulation" to "ambulation_score"
        )

        /**
         * REDCap/REDONE.br Portuguese field names (legacy).
         *
         * Maps the Portuguese field names used in REDCap projects to the
         * standard Functional System identifiers.
         */
        val FIELDS_REDCAP_PT: Map<String, String> = mapOf(
            "visual" to "edss_func_visuais",
            "brainstem" to "edss_cap_func_tronco_cereb",
            "pyramidal" to "edss_cap_func_pirad",
            "cerebellar" to "edss_cap_func_cereb",
            "sensory" to "edss_cap_func_sensitivas",
            "bowelBladder" to "edss_func_vesicais_e_instestinais",
            "cerebral" to "edss_func_cerebrais",
            "ambulation" to "edss_func_demabulacao_incapacidade"
        )

        /**
         * Convert the raw Visual (Optic) FS score to its adjusted value for EDSS.
         *
         * The Visual FS uses a 0-6 scale but is compressed for EDSS calculation:
         *   0 → 0, 1 → 1, 2-3 → 2, 4-5 → 3, 6 → 4
         */
        fun convertVisualScore(rawScore: Int): Int = when {
            rawScore == 6 -> 4
            rawScore >= 4 -> 3
            rawScore >= 2 -> 2
            else -> rawScore // 0 or 1
        }

        /**
         * Convert the raw Bowel & Bladder FS score to its adjusted value for EDSS.
         *
         * The Bowel & Bladder FS uses a 0-6 scale but is compressed for EDSS calculation:
         *   0 → 0, 1 → 1, 2 → 2, 3-4 → 3, 5 → 4, 6 → 5
         */
        fun convertBowelAndBladderScore(rawScore: Int): Int = when {
            rawScore == 6 -> 5
            rawScore == 5 -> 4
            rawScore >= 3 -> 3
            else -> rawScore // 0, 1, or 2
        }

        /**
         * Find the maximum value in a list and how many times it appears.
         *
         * @return Pair of (maxValue, count)
         */
        fun findMaxAndCount(scores: List<Int>): Pair<Int, Int> {
            val max = scores.max()
            val count = scores.count { it >= max }
            return Pair(max, count)
        }

        /**
         * Find the second-largest value in a list and how many times it appears.
         *
         * @param scores The list of FS scores
         * @param max The maximum value to exclude
         * @return Pair of (secondMaxValue, count)
         */
        fun findSecondMaxAndCount(scores: List<Int>, max: Int): Pair<Int, Int> {
            val filtered = scores.filter { it < max }
            if (filtered.isEmpty()) return Pair(0, 0)

            val secondMax = filtered.max()
            val count = filtered.count { it >= secondMax }
            return Pair(secondMax, count)
        }
    }

    /**
     * Calculate the EDSS score from individual Functional System scores.
     *
     * Parameter names match the JS reference: calculateEDSS(visualFunctionsScore,
     * brainstemFunctionsScore, pyramidalFunctionsScore, cerebellarFunctionsScore,
     * sensoryFunctionsScore, bowelAndBladderFunctionsScore, cerebralFunctionsScore,
     * ambulationScore)
     *
     * @param visualFunctionsScore              Raw Visual (Optic) FS score (0-6)
     * @param brainstemFunctionsScore            Brainstem FS score (0-5)
     * @param pyramidalFunctionsScore            Pyramidal FS score (0-6)
     * @param cerebellarFunctionsScore           Cerebellar FS score (0-5)
     * @param sensoryFunctionsScore              Sensory FS score (0-6)
     * @param bowelAndBladderFunctionsScore      Raw Bowel & Bladder FS score (0-6)
     * @param cerebralFunctionsScore             Cerebral (Mental) FS score (0-5)
     * @param ambulationScore                    Ambulation score (0-16)
     *
     * @return The calculated EDSS score (e.g., "0", "1.5", "4", "6.5", "10")
     */
    fun calculate(
        visualFunctionsScore: Int,
        brainstemFunctionsScore: Int,
        pyramidalFunctionsScore: Int,
        cerebellarFunctionsScore: Int,
        sensoryFunctionsScore: Int,
        bowelAndBladderFunctionsScore: Int,
        cerebralFunctionsScore: Int,
        ambulationScore: Int
    ): String {
        // ─── Phase 1: Ambulation-driven EDSS (≥ 5.0) ───
        val ambulationEdss = getAmbulationEdss(ambulationScore)
        if (ambulationEdss != null) return ambulationEdss

        // ─── Phase 2: FS-driven EDSS (0 – 5.0) ───
        val convertedVisual = convertVisualScore(visualFunctionsScore)
        val convertedBowelBladder = convertBowelAndBladderScore(bowelAndBladderFunctionsScore)

        val functionalSystems = listOf(
            convertedVisual,
            brainstemFunctionsScore,
            pyramidalFunctionsScore,
            cerebellarFunctionsScore,
            sensoryFunctionsScore,
            convertedBowelBladder,
            cerebralFunctionsScore
        )

        val (maxValue, maxCount) = findMaxAndCount(functionalSystems)

        return calculateFromFunctionalSystems(
            functionalSystems, maxValue, maxCount, ambulationScore
        )
    }

    /**
     * Calculate EDSS from an associative map using custom field mapping.
     *
     * By default uses English field names ([FIELDS_DEFAULT]). You can pass
     * [FIELDS_REDCAP_PT] for Portuguese REDCap field names, or your own mapping.
     *
     * @param data       Map with FS score values (values as strings or ints)
     * @param fieldMap   Field name mapping (default: [FIELDS_DEFAULT])
     * @param suffix     Optional suffix appended to field names (e.g., "_long")
     *
     * @return The calculated EDSS score, or null if data is incomplete
     */
    fun calculateFromMap(
        data: Map<String, Any?>,
        fieldMap: Map<String, String> = FIELDS_DEFAULT,
        suffix: String = ""
    ): String? {
        val fields = mapOf(
            "visual" to (fieldMap["visual"]!! + suffix),
            "brainstem" to (fieldMap["brainstem"]!! + suffix),
            "pyramidal" to (fieldMap["pyramidal"]!! + suffix),
            "cerebellar" to (fieldMap["cerebellar"]!! + suffix),
            "sensory" to (fieldMap["sensory"]!! + suffix),
            "bowelBladder" to (fieldMap["bowelBladder"]!! + suffix),
            "cerebral" to (fieldMap["cerebral"]!! + suffix),
            "ambulation" to (fieldMap["ambulation"]!! + suffix)
        )

        val values = mutableMapOf<String, Int>()
        for ((key, fieldName) in fields) {
            val rawValue = data[fieldName]?.toString() ?: ""
            if (rawValue.isEmpty()) return null // Incomplete data
            values[key] = rawValue.toInt()
        }

        return calculate(
            values["visual"]!!,
            values["brainstem"]!!,
            values["pyramidal"]!!,
            values["cerebellar"]!!,
            values["sensory"]!!,
            values["bowelBladder"]!!,
            values["cerebral"]!!,
            values["ambulation"]!!
        )
    }

    /**
     * Get the EDSS score determined directly by ambulation (for ambulationScore ≥ 3).
     *
     * @return The EDSS score, or null if ambulation doesn't directly determine it
     */
    private fun getAmbulationEdss(ambulationScore: Int): String? = when (ambulationScore) {
        16 -> "10"     // Death due to MS
        15 -> "9.5"    // Totally helpless bed patient
        14 -> "9"      // Helpless bed patient; can communicate and eat
        13 -> "8.5"    // Restricted to bed; some use of arm(s)
        12 -> "8"      // Restricted to bed/chair, out of bed most of day
        11 -> "7.5"    // Wheelchair with help
        10 -> "7"      // Wheelchair without help
        9, 8 -> "6.5"  // Bilateral assistance or limited walking
        7, 6, 5 -> "6" // Unilateral/bilateral assistance ≥120m
        4 -> "5.5"     // Walks 100-200m without help
        3 -> "5"       // Walks 200-300m without help
        else -> null    // FS-driven EDSS (ambulationScore 0-2)
    }

    /**
     * Calculate EDSS from FS scores when ambulation is 0-2 (Phase 2).
     */
    private fun calculateFromFunctionalSystems(
        functionalSystems: List<Int>,
        maxValue: Int,
        maxCount: Int,
        ambulationScore: Int
    ): String {
        // ── EDSS 5.0: FS-based ──
        if (maxValue >= 5) return "5"

        if (maxValue == 4 && maxCount >= 2) return "5"

        if (maxValue == 4 && maxCount == 1) {
            val (secondMax, secondCount) = findSecondMaxAndCount(functionalSystems, maxValue)

            if (secondMax == 3 && secondCount > 2) return "5"
            if (secondMax == 3 || secondMax == 2) return "4.5"
            if (ambulationScore < 2 && secondMax < 2) return "4"
        }

        // Check here because of ambulation score — the only case where it could go to 5
        if (maxValue == 3 && maxCount >= 6) return "5"

        // ── EDSS 4.5: Ambulation = 2 ──
        if (ambulationScore == 2) return "4.5"

        // ── EDSS 3.0 – 4.5: maxValue = 3 ──
        if (maxValue == 3) {
            if (maxCount == 5) return "4.5"

            if (maxCount >= 2) {
                if (maxCount == 2) {
                    val (secondMax, _) = findSecondMaxAndCount(functionalSystems, maxValue)
                    if (secondMax <= 1) return "3.5"
                }
                return "4"
            }

            // maxCount is 1
            val (secondMax, secondCount) = findSecondMaxAndCount(functionalSystems, maxValue)

            if (secondMax == 2) {
                if (secondCount >= 3) return "4"
                return "3.5"
            }

            // Second max is 0 or 1
            return "3"
        }

        // ── EDSS 2.0 – 4.0: maxValue = 2 ──
        if (maxValue == 2) {
            if (maxCount >= 6) return "4"
            if (maxCount == 5) return "3.5"
            if (maxCount == 3 || maxCount == 4) return "3"
            if (maxCount == 2) return "2.5"
            return "2"
        }

        // ── EDSS 2.0: Ambulation = 1 ──
        if (ambulationScore == 1) return "2"

        // ── EDSS 1.0 – 1.5: maxValue = 1 ──
        if (maxValue == 1) {
            if (maxCount >= 2) return "1.5"
            return "1"
        }

        // ── EDSS 0.0: All scores are 0 ──
        return "0"
    }
}
