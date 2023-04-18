package top.daozhang.parse

import cn.hutool.core.util.ClassUtil
import cn.hutool.core.util.ReflectUtil
import cn.hutool.log.dialect.console.ConsoleLog
import top.daozhang.meta.ResultMap
import top.daozhang.tool.ClassTool
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement


object DbExecutor {

    var connection: Connection? = null
    val logger = ConsoleLog(DbExecutor::class.java)
    var showSql = false


    fun init(host: String, port: Int, username: String, password: String, dbName: String?, driverClass: String) {
        Class.forName(driverClass)
        connection = DriverManager.getConnection("jdbc:mysql://${host}:${port}/${dbName}", username, password)
    }


    /**
     * 根据id查询一条数据
     *
     * @param T
     * @param id
     * @param clz
     * @return
     */
    fun <T> getId(id: Any?, clz: Class<T>): T? {
        if (id == null) {
            return null
        }
        val sql = ParseTable.metaMap[clz.name]!!.selectByIdSql()
        val data = query(
            sql, mutableMapOf(
                "id" to id
            ), clz
        )
        return if (data.isEmpty()) {
            null
        } else {
            data[0]
        }
    }

    fun <T> delId(id: Any?, clz: Class<T>): Int {
        if (id == null) {
            return 0
        }
        val sql = ParseTable.metaMap[clz.name]!!.deleteByIdSql()
        val ps = buildPreparedStatement(
            sql,
            mutableMapOf(
                "id" to id
            )
        )
        return ps.executeUpdate()
    }

    fun <T> updId(v: T, clz: Class<T>): Int {
        if (v == null) {
            return 0
        }

        val sql = ParseTable.metaMap[clz.name]!!.updateIdSql()
        val map = ClassTool.toMap(v)
        val ps = buildPreparedStatement(sql, map.toMutableMap())
        return ps.executeUpdate()
    }

    fun sqlToToken(sql: String): String {

        val keyWords = listOf<String>(
            "select", "from", "where"
        )
        var s1 = sql.trim()
        val ws1 = s1.split(" ")
        val ws2 = ws1.filter { it != "" }
        // select id,name from user where xxx = xxx
        val words = mutableListOf<String>()
        var tokenEnd = false
        var preWord = ""
        ws2.forEach {
            if (tokenEnd) {
                preWord = ""
            }
            if (keyWords.contains(it) || keyWords.contains(it.uppercase())) {
                // 如果包含关键字 ，直接加入
                words += it
                tokenEnd = true
            } else {
                // 如果没有关键字，查看是否有#{}
                if (tokenEnd) {
                    if (it.startsWith("#{")) {
                        if (it.endsWith("}")) {
                            //  如果有,移除所有的空格
                            val v = it.replace(" ", "")
                            words += v
                            tokenEnd = true
                        } else {
                            tokenEnd = false
                            preWord = "${preWord}${it}"
                        }

                    } else {
                        tokenEnd = true
                        words += it
                    }
                } else {
                    preWord = "${preWord}${it}"
                    if (it.endsWith("}")) {
                        tokenEnd = true
                        words += preWord
                    }
                }

            }
        }
        return words.joinToString(" ")

    }


    /**
     * SQL语句执行器构建
     *
     * @param T
     * @param plainSql 原始的SQL语句
     * @param params
     * @param clz
     * @return
     */
    private fun buildPreparedStatement(
        plainSql: String,
        params: MutableMap<String, Any?>
    ): PreparedStatement {

        val sql = sqlToToken(plainSql)
        val words = sql.split(" ")
        val meanWords = words.filter { it != "" }
        val namedWords = meanWords.filter { it.startsWith("#") && it.endsWith("}") }
        val pos2param = mutableMapOf<Int, Any>()
        val finalParams = mutableListOf<Any?>()

        namedWords.forEachIndexed { index, s ->
            pos2param[index + 1] = s.drop(2).dropLast(1)
        }
        val placedSqlWords = meanWords.map { w ->
            run {
                if (w.startsWith("#") && w.endsWith("}")) {
                    if (w.contains("@loop")) {
                        // #{@loop(xxxx)}
                        val variableName = w.drop(8).dropLast(2)
                        val data = params[variableName]
                        if (data is List<*>) {
                            // 参数放进去
                            data.forEach {
                                finalParams += it
                            }
                            loopSql(data)
                        } else {
                            throw RuntimeException("param can't loop")
                        }

                    } else {
                        val variableName = w.drop(2).dropLast(1)
                        finalParams += params[variableName]
                        "?"
                    }

                } else {
                    w
                }
            }
        }


        val placedSql = placedSqlWords.joinToString(" ")
        val ps = connection!!.prepareStatement(placedSql)
        if (showSql) {
            logger.info(placedSql)
        }
        finalParams.forEachIndexed { index, v ->
            run {

            }
            ps.setObject(index + 1, v)
        }

        return ps
    }

    fun <T> exe(sql: String, params: MutableMap<String, Any>, clz: Class<T>): Long {
        return 0L
    }

    /**
     * 查询器
     *
     * @param T
     * @param sql
     * @param params
     * @param clz
     * @return
     */
    fun <T> query(sql: String, params: MutableMap<String, Any?>, clz: Class<T>): MutableList<T> {
        val ps = buildPreparedStatement(sql, params)
        val rs = ps.executeQuery()
        val rsMeta = rs.metaData
        val selectColumnCount = rsMeta.columnCount
        if (!ParseTable.metaMap.keys.contains(clz.name)) {
            ParseTable.parseClass(clz)
        }
        val tableInfo = ParseTable.metaMap[clz.name]!!
        val result = mutableListOf<T>()
        while (rs.next()) {
            val ins = clz.getDeclaredConstructor().newInstance()
            val fields = clz.declaredFields
            for (i in 1..selectColumnCount) {
                val columnName = rsMeta.getColumnLabel(i)
                var fieldOp = tableInfo.fc!!.find { it.columnName == columnName }
                if (fieldOp == null) {
                    fieldOp = tableInfo.fc!!.find { it.fieldName == columnName }
                }
                fieldOp?.let {
                    val fieldName = fieldOp.fieldName
                    val f = fields.find { it.name == fieldName }
                    f?.let {
                        f.trySetAccessible()
                        when (f.type) {
                            String::class.java -> {
                                f.set(ins, rs.getObject(columnName).toString())
                            }

                            else -> {
                                f.set(ins, rs.getObject(columnName))
                            }
                        }
                    }
                }
            }
            result += ins
        }
        return result
    }

    fun query(sql: String, params: MutableMap<String, Any?>, resultMap: ResultMap) {
        val ps = buildPreparedStatement(sql, params)
        val rs = ps.executeQuery()
        val rsMeta = rs.metaData
        val selectColumnCount = rsMeta.columnCount
        val clz = resultMap.type!!
        if (!ParseTable.metaMap.keys.contains(clz.name)) {
            ParseTable.parseClass(clz)
        }
        val result = mutableListOf<MutableMap<String, Any>>()
        while (rs.next()) {
            val row = mutableMapOf<String, Any>()
            for (i in 1..selectColumnCount) {
                val columnName = rsMeta.getColumnLabel(i)
                row[columnName] = rs.getObject(columnName)
            }
            result += row
        }
        val cols = resultMap.cols
        // 找出简单的列数据进行封装处理
        val simpleCols = cols!!.filter { it.simple!! }
        var insList: List<Any?> = mutableListOf()
        val fields = ReflectUtil.getFields(clz)
        val ids = mutableListOf<Any>()
        result.forEach { r ->
            run {
                val ins = clz.getDeclaredConstructor().newInstance()
                var next = true
                simpleCols.forEach { sc ->
                    run {
                        val columnValue = r[sc.column!!]
                        if (sc.id!!) {
                            if (ids.contains(columnValue)) {
                                next = false
                            } else {
                                if (columnValue != null) {
                                    ids += columnValue
                                }
                            }
                        }
                        if (next) {
                            val targetField = fields.find { it.name == sc.field }
                            targetField?.let {
                                targetField.trySetAccessible()
                                when (targetField.type) {
                                    String::class.java -> {
                                        targetField.set(ins, columnValue.toString())
                                    }

                                    else -> {
                                        targetField.set(ins, columnValue)
                                    }
                                }
                            }
                        }

                    }
                }
                if (next) {
                    insList += ins


                }
            }
        }


        val nonSimpleCols = cols.filter { !it.simple!! }

        if (nonSimpleCols.isNotEmpty()) {
            // 有非简单数据类型

            nonSimpleCols.forEach { sc ->
                run {

                    val innerCols = sc.fields
                    result.forEach { r ->
                        run {


                        }
                    }

                }
            }

        }



        println(insList)
    }


    fun loopSql(data: List<*>): String {
        // 第一个这段sql 需要重构
        val s1 = data.map { "?" }
        val s2 = s1.joinToString(",")
        return "(${s2})"
    }
}