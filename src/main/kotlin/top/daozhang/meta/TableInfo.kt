package top.daozhang.meta

import java.io.Serial
import java.io.Serializable

class TableInfo : Serializable {

    var id: String? = null
    var tableName: String? = null
    var columns: List<String>? = null
    // 字段到column的名称的一个映射关系
//    var fc: MutableList<Pair<String, String>>? = null
    var fc: MutableList<ColumnInfo>? = null


    fun allColSql(): String {
        if (columns == null) {
            return " * "
        } else {
            return columns!!.reduce { a, b -> "${a},${b}" }
        }
    }

    fun selectAllSql(): String {
        return "select ${allColSql()} from $tableName"
    }

    fun delId(id: Any): String {
        return "delete from $tableName where ${this.id} = ${id}"
    }

    fun getId(id: Any): String {
        return "select ${allColSql()} from $tableName where ${this.id} = ${id}"
    }

    fun updateIdSql(v: Any): String {
        var updatePart = ""
        fc!!.forEach { f ->
            run {
                val field = f.fieldName
                val colName = f.columnName
                if (colName != this.id) { // 不更新主键
                    updatePart += "$colName = #{${field}},"
                }
            }
        }
        updatePart = updatePart.dropLast(1)
        val sql = "update $tableName set  $updatePart where ${this.id} = #{${this.id}}"
        return sql
    }


    companion object {
        @Serial
        private const val serialVersionUID: Long = -842345804659407844L

    }

}