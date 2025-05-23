package mikhail.shell.stego.task8

import mikhail.shell.stego.common.compose
import mikhail.shell.stego.common.decompose
import mikhail.shell.stego.common.explode
import mikhail.shell.stego.common.implode
import kotlin.math.log

val zeroSymbol = "\uFE0E"[0]
val oneSymbol = "\uFE0F"[0]

fun generateString(
    template: String,
    bitSequence: Array<Byte>,
    maps: List<Map<Int, String>>
): String {

    var resultBuilder = StringBuilder(template)
    var bitIndex = 0
    var charIndex = 0
    var substitutionIndex = 0
    while (charIndex < resultBuilder.toString().length) {
        if (resultBuilder[charIndex] != '~') {
            val bitSymbol =
                if (bitIndex < bitSequence.size && bitSequence[bitIndex] == 1.toByte()) oneSymbol else zeroSymbol
            resultBuilder.insert(charIndex, bitSymbol)
            charIndex += 2
            bitIndex++
        } else {
            val substitutionMap = maps[substitutionIndex]
            val bitGroupCount = log(substitutionMap.keys.max().toDouble() + 1, 2.0).toInt()
            val substitutionOptionIndex = if (bitIndex < bitSequence.size) {
                val bitGroup = bitSequence.sliceArray(bitIndex until bitIndex + bitGroupCount)
                bitGroup.implode().toInt()
            } else 0
            resultBuilder = StringBuilder(
                resultBuilder.replaceFirst(
                    "~".toRegex(),
                    substitutionMap[substitutionOptionIndex]!!
                )
            )
            bitIndex += bitGroupCount
            substitutionIndex++
        }
    }
    return resultBuilder.toString()
}

fun extractFromString(
    template: String,
    string: String,
    maps: List<Map<Int, String>>
): Array<Byte> { // Возвращает биты
    val result = mutableListOf<Byte>()
    var bitIndex = 0
    var charIndex = 0
    var substitutionIndex = 0
    while (charIndex < template.length) {
        if (template[charIndex] != '~') {
            if (string[charIndex * 2] == zeroSymbol) result.add(0) else result.add(1)
            charIndex++
        } else {
            val restString = string
                .substring(charIndex * 2)
            val modifiedRestString = restString
                .replace(zeroSymbol.toString(), "")
                .replace(oneSymbol.toString(), "")
            for (entry in maps[substitutionIndex]) {
                if (modifiedRestString.indexOf(entry.value) == 0) {
                    val bitGroup = entry.key.toByte().explode()
                    result.addAll(bitGroup)
                    for (symbol in restString.substring(0, entry.value.length * 2)) {
                        if (symbol == zeroSymbol) result.add(0)
                        else if (symbol == oneSymbol) result.add(1)
                    }
                    break
                }
            }

            substitutionIndex++
        }
    }
    return result.toTypedArray()
}

val template = """
                    Work-life balance is ~. When you ~ you ~ your mind
                    and ~ ~ ~ a ~ ~ that you never thought ~.
                    ~ ~ seem ~, but you ~ to ~ what ~ you better.
                    ~. Also ~ a ~ you ~ ~. You ~ your physical and mental
                    ~ if ~ it ~.
                    """.trimIndent()
val generationKey = listOf(
    mapOf(
        0 to "critical",
        1 to "vital",
        2 to "important",
        3 to "necessary"
    ),
    mapOf(
        0 to "rest",
        1 to "relax"
    ),
    mapOf(
        0 to "free",
        1 to "open",
        2 to "release",
        3 to "clear"
    ),
    mapOf(
        0 to "may",
        1 to "can"
    ),
    mapOf(
        0 to "accidentally",
        1 to "eventually"
    ),
    mapOf(
        0 to "discover",
        1 to "come up with",
        2 to "find",
        3 to "find out"
    ),
    mapOf(
        0 to "new",
        1 to "different"
    ),
    mapOf(
        0 to "approach",
        1 to "way",
        2 to "method",
        3 to "technique"
    ),
    mapOf(
        0 to "of",
        1 to "about"
    ),
    mapOf(
        0 to "Relaxing",
        1 to "Having a rest",
        2 to "Resting",
        3 to "Rest"
    ),
    mapOf(
        0 to "can",
        1 to "may"
    ),
    mapOf(
        0 to "simple",
        1 to "easy"
    ),
    mapOf(
        0 to "need",
        1 to "have"
    ),
    mapOf(
        0 to "consider",
        1 to "think about"
    ),
    mapOf(
        0 to "recovers",
        1 to "recreates"
    ),
    mapOf(
        0 to "Listen to yourself",
        1 to "Listen to your inner voice",
        2 to "Listen to your thoughts",
        3 to "Search your feelings"
    ),
    mapOf(
        0 to "consider",
        1 to "choose"
    ),
    mapOf(
        0 to "job",
        1 to "profession"
    ),
    mapOf(
        0 to "really",
        1 to "indeed",
        2 to "actually",
        3 to "certainly"
    ),
    mapOf(
        0 to "like",
        1 to "wish",
        2 to "desire",
        3 to "want",
        4 to "need",
        5 to "wish for",
        6 to "love",
        7 to "lust"
    ),
    mapOf(
        0 to "save",
        1 to "preserve"
    ),
    mapOf(
        0 to "health",
        1 to "resources"
    ),
    mapOf(
        0 to "choose",
        1 to "consider"
    ),
    mapOf(
        0 to "carefully",
        1 to "properly",
        2 to "wisely",
        3 to "with caution"
    )
)

fun String.insert(dataBytes: Array<Byte>): String { // Принимает байты
    val dataBits = dataBytes.decompose()

    val stringBuilder = StringBuilder()

    try {
        for (i in dataBits.indices) {
            val bitSymbol = if (dataBits[i] == 0.toByte()) zeroSymbol else oneSymbol
            stringBuilder.append(bitSymbol)
            stringBuilder.append(this[i])
        }
        stringBuilder.append(this.substring(dataBits.size))
    } catch (_: StringIndexOutOfBoundsException) { }

    return stringBuilder.toString()
}

fun String.extract(): Array<Byte> { // Возвращает байты
    val dataBits = mutableListOf<Byte>() // биты

    for (char in this) {
        if (char == zeroSymbol) {
            dataBits.add(0)
        } else if (char == oneSymbol) {
            dataBits.add(1)
        }
    }

    return dataBits.toTypedArray().compose()
}