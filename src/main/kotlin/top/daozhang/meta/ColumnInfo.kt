package top.daozhang.meta

import java.io.Serial
import java.io.Serializable

class ColumnInfo :Serializable{

    // 字段名称
    var fieldName:String?=null
    // 列名称
    var columnName:String?=null





    companion object {
        @Serial
        private const val serialVersionUID: Long = 5860251200509005209L
    }

    override fun toString(): String {
        return "ColumnInfo(fieldName=$fieldName, columnName=$columnName)"
    }
}