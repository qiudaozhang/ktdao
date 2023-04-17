package top.daozhang.parse

import cn.hutool.core.util.ClassUtil
import top.daozhang.annotation.Col
import top.daozhang.annotation.Id
import top.daozhang.annotation.Table
import top.daozhang.meta.ColumnInfo
import top.daozhang.meta.TableInfo
import top.daozhang.tool.StrTool
import java.util.concurrent.ConcurrentHashMap


object ParseTable {

    val metaMap = ConcurrentHashMap<String, TableInfo>()

    fun parseClass(clz: Class<*>) {
        val annos = clz.declaredAnnotations
        val tableInfo = TableInfo()
        tableInfo.clz = clz
        tableInfo.simpleClassName = clz.simpleName
        tableInfo.fullClassName = clz.name
        annos.forEach {
            when (it) {
                is Table -> {
                    val tableName = if (it.name == "") {
                        // 获取类的简易名称
                        StrTool.camelIt(clz.simpleName)
                    } else {
                        it.name
                    }
                    tableInfo.tableName = tableName
                }

                else -> {
                }
            }
        }
        val fields = clz.declaredFields
        val columns = mutableListOf<String>()
        val fc = mutableListOf<ColumnInfo>()
        var idField = ""
        fields.forEach { field ->
            run {
                val fas = field.declaredAnnotations
                fas.forEach { anno ->
                    run {
                        when (anno) {
                            is Col -> {
                                val colName = if (anno.name == "") {
                                    StrTool.camelIt(field.name)
                                } else {
                                    anno.name
                                }
                                columns += colName
                                val columnInfo = ColumnInfo()
                                columnInfo.fieldName = field.name
                                columnInfo.columnName = colName
                                fc += columnInfo
                            }

                            is Id -> {
                                idField = field.name
                            }
                        }
                    }

                }
            }
        }
        tableInfo.columns = columns
        tableInfo.fc = fc
        tableInfo.id = fc.find { it.fieldName == idField }!!.columnName
//        metaMap[tableInfo.tableName!!] = tableInfo
        // 使用
//        metaMap[tableInfo.simpleClassName!!] = tableInfo
        metaMap[tableInfo.fullClassName!!] = tableInfo

    }


    fun parsePackage(packageName: String) {
        val sp = ClassUtil.scanPackage(packageName)
        sp.forEach {
            parseClass(it)
        }
    }


}