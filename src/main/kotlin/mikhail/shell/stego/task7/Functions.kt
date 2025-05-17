package mikhail.shell.stego.task7

inline operator fun <reified T : Number> Array<Array<T>>.times(other: Array<Array<T>>): Array<Array<T>> {
    require(this.isNotEmpty() && other.isNotEmpty()) { "Одна или обе матрицы пустые" }
    require(this[0].size == other.size) { "Количество строк в первой матрице не совпадает с количеством столбцов во второй." }
    val result = Array(this.size) { Array(other[0].size) { 0f } }
    for (i in this.indices) {
        for (j in other[0].indices) {
            for (k in this[0].indices) {
                result[i][j] += this[i][k].toFloat() * other[k][j].toFloat()
            }
        }
    }
    return result.map { row ->
        Array(row.size) { i ->
            row[i].let {
                when(T::class) {
                    Byte::class -> it.toInt().toByte()
                    Int::class -> it.toInt()
                    Long::class -> it.toLong()
                    Float::class -> it
                    Double::class -> it.toDouble()
                    else -> it
                }
            } as T
        }
    }.toTypedArray()
}

fun createHash(functionMatrix: Array<Array<Byte>>, bits: Array<Byte>): Array<Byte> {
    val bitsVector = bits.map { bit -> Array(1) { bit } }.toTypedArray()
    val resultVector = functionMatrix * bitsVector
    return resultVector.flatMap { it.asList() }.map { (it % 2).toByte() }.toTypedArray()
}

