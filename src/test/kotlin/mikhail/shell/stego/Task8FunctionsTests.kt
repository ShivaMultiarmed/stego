package mikhail.shell.stego

import mikhail.shell.stego.common.compose
import mikhail.shell.stego.common.decompose
import mikhail.shell.stego.task8.extractFromString
import mikhail.shell.stego.task8.generateString
import org.junit.jupiter.api.Test

class Task8FunctionsTests {
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
    @Test
    fun testGeneratingText() {
        val data = ""
            .encodeToByteArray()
            .toTypedArray()
            .decompose()
        val stringBuilder = StringBuilder()

        stringBuilder.append(
            generateString(
                template = template,
                bitSequence = data,
                maps = generationKey
            )
        )
        println(stringBuilder.toString())
    }

    @Test
    fun testExtractingData() {
//        val text = """
//            Work-life balance is vital. When you rest you open your mind
//            and may accidentally discover a different method that you never thought of.
//            Resting may seem simple, but you have to think about what recovers you better.
//            Search your feelings. Also consider a job you indeed wish for. You preserve your physical and mental
//            health if choose it properly.
//        """.trimIndent()
//        val data = extractFromString(
//            string = text,
//            maps = generationKey
//        )
//        val extractedText = data.compose().toByteArray().decodeToString()
//        println(extractedText)
    }
}