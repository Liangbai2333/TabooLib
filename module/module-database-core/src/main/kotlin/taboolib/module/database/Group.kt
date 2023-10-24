package taboolib.module.database

/**
 * 分组
 *
 * @author legoshi
 * @since 2023-10-24 15:33
 */
class Group(val values: ArrayList<Any>, var rollup: Boolean) : Attributes {

    fun isNotEmpty(): Boolean {
        return values.isNotEmpty()
    }

    fun withRollup() {
        rollup = true
    }

    /** 语句 */
    override val query: String
        get() = Statement("GROUP BY")
            .addSegment(values.joinToString(", ") { it.asFormattedColumnName() })
            .addSegmentIfTrue(rollup) {
                addSegment("WITH ROLLUP")
            }.build()
}