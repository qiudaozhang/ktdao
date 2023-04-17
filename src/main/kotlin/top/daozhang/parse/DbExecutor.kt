package top.daozhang.parse

import cn.hutool.log.dialect.console.ConsoleLog
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


    /**
     * SQL语句执行器构建
     *
     * @param T
     * @param sql
     * @param params
     * @param clz
     * @return
     */
    private fun buildPreparedStatement(
        sql: String,
        params: MutableMap<String, Any?>
    ): PreparedStatement {
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

//        val placedSqlWords = meanWords.map { w ->
//            run {
//                if (w.startsWith("#") && w.endsWith("}")) {
//                    if(w.contains("@loop")){
//                        // #{@loop(xxxx)}
//                        val variableName = w.drop(6).dropLast(2)
//                        val data = params[variableName]
//                        if(data is List<*>){
//                            // 参数放进去
//                            data.forEach {
//                                finalParams +=  it
//                            }
//                            loopSql(data)
//                        } else {
//                            throw RuntimeException("param can't loop")
//                        }
//
//                    } else {
//                        finalParams +=
//                        "?"
//                    }
//
//                } else {
//                    w
//                }
//            }
//        }
        val placedSql = placedSqlWords.joinToString(" ")
        val ps = connection!!.prepareStatement(placedSql)
        if (showSql) {
            logger.info(placedSql)
        }
//        finalParams.forEach {
//            ps.setObject()
//        }
        finalParams.forEachIndexed { index, v ->
            run {

            }
            ps.setObject(index + 1, v)
        }
//        pos2param.forEach {
//            val k = it.key
//            val v = params[it.value]
//            ps.setObject(k, v)
//            if (showSql) {
//                logger.info(v.toString())
//            }
//        }
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
            throw RuntimeException("not found class table info")
        }
        val tableInfo = ParseTable.metaMap[clz.name]!!
        val result = mutableListOf<T>()
        while (rs.next()) {
            val ins = clz.getDeclaredConstructor().newInstance()
            val fields = clz.declaredFields
            for (i in 1..selectColumnCount) {
                val columnName = rsMeta.getColumnLabel(i)
                val fieldOp = tableInfo.fc!!.find { it.columnName == columnName }
                fieldOp?.let {
                    val fieldName = fieldOp.fieldName
                    val f = fields.find { it.name == fieldName }
                    f?.let {
                        f.trySetAccessible()
                        f.set(ins, rs.getObject(columnName))
                    }
                }
            }
            result += ins
        }
        return result
    }


    fun loopSql(data: List<*>): String {

        // 第一个这段sql 需要重构
        val s1 = data.map { "?" }
        val s2 = s1.joinToString(",")
        val s3 = "(${s2})"
        return s3

    }
}