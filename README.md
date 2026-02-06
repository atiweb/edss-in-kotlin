# EDSS Calculator for Kotlin

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Kotlin implementation of the **Expanded Disability Status Scale (EDSS)** calculator, based on the scoring table by Ludwig Kappos, MD (University Hospital Basel) and the Neurostatus-EDSS™ standard (Kurtzke, 1983).

This library calculates the EDSS score from 7 Functional System (FS) scores and an Ambulation score, used in the clinical assessment of Multiple Sclerosis.

Based on the [JavaScript reference implementation](https://github.com/atiweb/edss) and the [PHP implementation](https://github.com/atiweb/edss-in-php).

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.atiweb:edss-in-kotlin:1.0.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.atiweb:edss-in-kotlin:1.0.0'
}
```

## Usage

### Direct calculation with individual scores

```kotlin
import com.atiweb.edss.EdssCalculator

val calculator = EdssCalculator()

val edss = calculator.calculate(
    visualFunctionsScore = 1,              // Visual (Optic) — raw 0-6
    brainstemFunctionsScore = 2,           // Brainstem — 0-5
    pyramidalFunctionsScore = 1,           // Pyramidal — 0-6
    cerebellarFunctionsScore = 3,          // Cerebellar — 0-5
    sensoryFunctionsScore = 1,             // Sensory — 0-6
    bowelAndBladderFunctionsScore = 4,     // Bowel & Bladder — raw 0-6
    cerebralFunctionsScore = 2,            // Cerebral (Mental) — 0-5
    ambulationScore = 1                     // Ambulation — 0-16
)

println(edss) // "4"
```

### From a Map

By default, `calculateFromMap()` expects English field names:

```kotlin
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

val edss = calculator.calculateFromMap(data)
println(edss) // "4"
```

### With REDCap Portuguese field names

```kotlin
val redcapData = mapOf(
    "edss_func_visuais" to "1",
    "edss_cap_func_tronco_cereb" to "2",
    "edss_cap_func_pirad" to "1",
    "edss_cap_func_cereb" to "3",
    "edss_cap_func_sensitivas" to "1",
    "edss_func_vesicais_e_instestinais" to "4",
    "edss_func_cerebrais" to "2",
    "edss_func_demabulacao_incapacidade" to "1"
)

val edss = calculator.calculateFromMap(redcapData, EdssCalculator.FIELDS_REDCAP_PT)
println(edss) // "4"

// Longitudinal data with suffix
val edssLong = calculator.calculateFromMap(longitudinalData, EdssCalculator.FIELDS_REDCAP_PT, "_long")
```

### Custom field mapping

```kotlin
val myFields = mapOf(
    "visual" to "my_visual_field",
    "brainstem" to "my_brainstem_field",
    "pyramidal" to "my_pyramidal_field",
    "cerebellar" to "my_cerebellar_field",
    "sensory" to "my_sensory_field",
    "bowelBladder" to "my_bowel_bladder_field",
    "cerebral" to "my_cerebral_field",
    "ambulation" to "my_ambulation_field"
)

val edss = calculator.calculateFromMap(myData, myFields)
```

### Score conversions

```kotlin
// Visual: raw 0-6 → converted 0-4
EdssCalculator.convertVisualScore(3)  // 2
EdssCalculator.convertVisualScore(5)  // 3

// Bowel & Bladder: raw 0-6 → converted 0-5
EdssCalculator.convertBowelAndBladderScore(4)  // 3
EdssCalculator.convertBowelAndBladderScore(6)  // 5
```

## Functional Systems

| # | Functional System   | Raw Scale | Converted Scale | Parameter Name |
|---|--------------------|-----------|-----------------|-----------------------|
| 1 | Visual (Optic)     | 0-6       | 0-4             | `visualFunctionsScore` |
| 2 | Brainstem          | 0-5       | —               | `brainstemFunctionsScore` |
| 3 | Pyramidal          | 0-6       | —               | `pyramidalFunctionsScore` |
| 4 | Cerebellar         | 0-5       | —               | `cerebellarFunctionsScore` |
| 5 | Sensory            | 0-6       | —               | `sensoryFunctionsScore` |
| 6 | Bowel & Bladder    | 0-6       | 0-5             | `bowelAndBladderFunctionsScore` |
| 7 | Cerebral (Mental)  | 0-5       | —               | `cerebralFunctionsScore` |
| 8 | Ambulation         | 0-16      | —               | `ambulationScore` |

## Algorithm

The EDSS calculation follows a two-phase approach:

### Phase 1: Ambulation-driven (EDSS ≥ 5.0)
When the Ambulation score is ≥ 3, it directly maps to an EDSS value (5.0 – 10.0).

### Phase 2: FS-driven (EDSS 0 – 5.0)
When Ambulation is 0-2, the EDSS is calculated from the combination of the 7 FS scores based on the Kappos scoring table.

## Other implementations

| Language | Repository |
|----------|------------|
| JavaScript | [atiweb/edss](https://github.com/atiweb/edss) |
| PHP | [atiweb/edss-in-php](https://github.com/atiweb/edss-in-php) |
| Flutter/Dart | [atiweb/edss-in-flutter](https://github.com/atiweb/edss-in-flutter) |

## Testing

```bash
./gradlew test
```

The test suite includes 70+ test cases covering all EDSS ranges, score conversions, edge cases, and field mapping support.

## References

- Kurtzke JF. Rating neurologic impairment in multiple sclerosis: an expanded disability status scale (EDSS). *Neurology*. 1983;33(11):1444-1452.
- [Neurostatus-EDSS™](https://www.neurostatus.net/)
- [JavaScript reference implementation](https://github.com/atiweb/edss)

## License

[MIT](LICENSE)
