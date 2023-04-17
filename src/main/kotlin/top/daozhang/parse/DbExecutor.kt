package top.daozhang.parse

import cn.hutool.log.Log
import cn.hutool.log.dialect.console.ConsoleLog
import top.daozhang.meta.TableInfo
import top.daozhang.model.Resource
import java.sql.Connection
import java.sql.DriverManager


object DbExecutor {

    var connection: Connection? = null
    val logger = ConsoleLog(DbExecutor::class.java)
    var showSql = false

    init {
        val host = "localhost"
        val port = 3354
        val username = "root"
        val password = "root"
        val dbName = "cloud3"
        Class.forName("com.mysql.cj.jdbc.Driver")
        connection = DriverManager.getConnection("jdbc:mysql://${host}:${port}/${dbName}", username, password)
        println("初始化")
    }

    fun execute(sql: String) {
    }

    /**
     * 简单的转换具名参数的封装查询
     *
     * @param sql
     * @param params
     */
    fun executeQuery(sql: String, params: MutableMap<String, Any>) {
//        val pst = connection!!.prepareStatement(sql)
        /*
        假设有要给sql
        ① update user set name = ? where name = ? or username = ?
        ② update user set name = #{newName} where name = #{oldName} or username = #{oldName}
        这种情况下oldName可以复用，需要解决几个问题
        2 需要最终替换为1 ，且能够根据这个关系找到对应的数据
        所以可以构建一个三者关系
        位置=》具名参数的名称=》具体的值，并且还有
         */
        val words = sql.split(" ")
        val meanWords = words.filter { it != "" }
        val namedWords = meanWords.filter { it.startsWith("#") && it.endsWith("}") }
        val pos2param = mutableMapOf<Int, Any>()
        namedWords.forEachIndexed { index, s ->
            pos2param[index + 1] = s.drop(2).dropLast(1)
        }
        val placedSqlWords = meanWords.map { w ->
            run {
                if (w.startsWith("#") && w.endsWith("}")) {
                    "?"
                } else {
                    w
                }
            }
        }
        val placedSql = placedSqlWords.joinToString(" ")
        val ps = connection!!.prepareStatement(placedSql)


        if (!placedSql.startsWith("select", ignoreCase = true)) {
            throw RuntimeException("Illegal query SQL statement")
        }


        pos2param.forEach {
            val k = it.key
            val v = params[it.value]
            ps.setObject(k, v)
        }
        val rs = ps.executeQuery()
        val rsMeta = rs.metaData
        val selectColumnCount = rsMeta.columnCount
        while (rs.next()) {
            for (i in 1..selectColumnCount) {
                print("${rs.getObject(i)}\t")
            }
        }
    }


    /**
     * 传入table info 并且封装对应的数据返回
     * 对外暴露TableInfo用法应该限定在内部
     *
     * @param sql
     * @param params
     * @param tableInfo
     */
    fun executeQueryV2(sql: String, params: MutableMap<String, Any>, tableInfo: TableInfo) {
        val words = sql.split(" ")
        val meanWords = words.filter { it != "" }
        val namedWords = meanWords.filter { it.startsWith("#") && it.endsWith("}") }
        val pos2param = mutableMapOf<Int, Any>()
        namedWords.forEachIndexed { index, s ->
            pos2param[index + 1] = s.drop(2).dropLast(1)
        }
        val placedSqlWords = meanWords.map { w ->
            run {
                if (w.startsWith("#") && w.endsWith("}")) {
                    "?"
                } else {
                    w
                }
            }
        }
        val placedSql = placedSqlWords.joinToString(" ")
        val ps = connection!!.prepareStatement(placedSql)


        if (!placedSql.startsWith("select", ignoreCase = true)) {
            throw RuntimeException("Illegal query SQL statement")
        }

        pos2param.forEach {
            val k = it.key
            val v = params[it.value]
            ps.setObject(k, v)
        }
        val rs = ps.executeQuery()
        val rsMeta = rs.metaData
        val selectColumnCount = rsMeta.columnCount
        val clz = tableInfo.clz!!

        val result = mutableListOf<Any>()
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

        result.forEach { println(it) }
    }


    /**
     * 根据id查询一条数据
     *
     * @param T
     * @param id
     * @param clz
     * @return
     */
    fun <T> getId(id: Any, clz: Class<T>): T? {
        val sql = ParseTable.metaMap[clz.name]!!.selectByIdSql()
        val data = executeQueryV3(
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

    /**
     * 查询器
     *
     * @param T
     * @param sql
     * @param params
     * @param clz
     * @return
     */
    fun <T> executeQueryV3(sql: String, params: MutableMap<String, Any>, clz: Class<T>): MutableList<T> {
        val words = sql.split(" ")
        val meanWords = words.filter { it != "" }
        val namedWords = meanWords.filter { it.startsWith("#") && it.endsWith("}") }
        val pos2param = mutableMapOf<Int, Any>()
        namedWords.forEachIndexed { index, s ->
            pos2param[index + 1] = s.drop(2).dropLast(1)
        }
        val placedSqlWords = meanWords.map { w ->
            run {
                if (w.startsWith("#") && w.endsWith("}")) {
                    "?"
                } else {
                    w
                }
            }
        }
        val placedSql = placedSqlWords.joinToString(" ")
        val ps = connection!!.prepareStatement(placedSql)
        if (!placedSql.startsWith("select", ignoreCase = true)) {
            throw RuntimeException("Illegal query SQL statement")
        }
        if (showSql) {
            logger.info(placedSql)
        }
        pos2param.forEach {
            val k = it.key
            val v = params[it.value]
            ps.setObject(k, v)
            if (showSql) {
                logger.info(v.toString())
            }
        }
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

    @JvmStatic
    fun main(args: Array<String>) {
        ParseTable.parsePackage("top.daozhang.model")
//        ParseTable.parseClass(Resource::class.java)
//        executeQueryV3(
//            "select id,icon,`type`,name,show_name,created,updated,deleted from `resource` where icon = #{icon}",
//            mutableMapOf(
//                "icon" to "account2",
//            ),
//            Resource::class.java
//        )

//        executeQueryV3(
//            "select id,icon,`type`,name,show_name,created,updated,deleted from `resource` where icon = #{icon} or name = #{icon} ",
//            mutableMapOf(
//                "icon" to "account",
//            ),
//            Resource::class.java
//        )

        showSql = true
        val r = getId(391272090009669L, Resource::class.java)
        println(r)

//        executeQuery("update user set name = #{newName} where name = #{oldName} or username = #{oldName}" , mutableMapOf(
//                    "newName" to "有意思",
//                    "oldName" to "没意思"
//                )
//        )
    }
}