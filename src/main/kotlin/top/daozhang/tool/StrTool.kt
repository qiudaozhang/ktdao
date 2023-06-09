package top.daozhang.tool

object StrTool {

    /**
     * 获取大写字母的索引位置列表
     *
     * @param s
     * @return
     */

    fun bigIndex(s: String): List<Int> {
        val indexes = s.mapIndexed { index, e ->
            if (e.isUpperCase()) {
                index
            } else {
                -1
            }
        }
        return indexes.filter { it != -1 }
    }


    /**
     * 按照索引位置拆分为一组string
     *
     * @param poses
     * @return
     */
    fun splitWithIndex(s: String, poses: List<Int>): List<String> {
        var start = 0
        val words = mutableListOf<String>()
        poses.forEach {
            if (it > 0) {
                val word = s.drop(start).take(it - start)
                words += word
            }
            start = it
        }
        if (poses.takeLast(1)[0] != (s.length - 1)) {
            words += s.drop(poses.takeLast(1)[0])
        }
        return words
    }


    fun underScoreIt(s: String): String {
        val indexes = bigIndex(s)
        if (indexes.isEmpty()) {
            return s
        } else {
            val words = splitWithIndex(s, indexes)
            return words.map { it.lowercase() }.reduce { a, b -> "${a}_${b}" }
        }
    }


    fun camelIt(s: String): String {
        val words = s.split("_")
        val w2 = words.map { w ->
            run {
                w.replaceFirstChar { a -> a.uppercase() }
            }
        }
        return w2.joinToString("").replaceFirstChar { a -> a.lowercase() }
    }


    @JvmStatic
    fun main(args: Array<String>) {
//        val r = underScoreIt("GoodStudy2BigTable")
//        println(r)
        println(camelIt("love_is_good"))
//        println(bigIndex("GoodStudy"))
//        println(splitWithIndex("GoodStudy"))
    }
}