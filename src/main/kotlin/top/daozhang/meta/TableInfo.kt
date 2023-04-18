package top.daozhang.meta

import java.io.Serial
import java.io.Serializable

/**
 * 表信息
 * 2023-4-18
 */
class TableInfo : Serializable {

    /**主键名称*/
    var id: String? = null
    /**表名，如果没有标记@Table  视为一般性的结果集映射，非全映射表的类*/
    var tableName: String? = null
    /**简易类名*/
    var simpleClassName: String? = null
    /**全限定类名*/
    var fullClassName: String? = null
    /**所有的列名*/
    var columns: List<String>? = null
    /**filed to column 的一个集合*/
    var fc: MutableList<ColumnInfo>? = null
    /**对应的类*/
    var clz: Class<*>? = null

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



    fun selectByIdSql(): String {
        return "select ${allColSql()} from $tableName where ${this.id} = #{id}"
    }

    fun deleteByIdSql(): String {
        return "delete from $tableName where ${this.id} = #{id}"
    }





    fun updateIdSql(): String {
        var updatePart = ""
        fc!!.forEach { f ->
            run {
                val field = f.fieldName
                val colName = f.columnName
                if (colName != this.id) { // 不更新主键
                    updatePart += " $colName = #{${field}} , "
                }
            }
        }
        updatePart = updatePart.take(updatePart.lastIndexOf(",") - 1)
        return "update $tableName set  $updatePart where $id = #{$id}"
    }

    override fun toString(): String {
        return "TableInfo(id=$id, tableName=$tableName, columns=$columns, fc=$fc)"
    }


    companion object {
        @Serial
        private const val serialVersionUID: Long = -842345804659407844L

    }

}