package top.daozhang.meta

import java.io.Serial
import java.io.Serializable

data class ColumnResult(
    val column:String?="",
    val field:String?="",
    val type:Class<*>?=null,
    var simple:Boolean?=true,
    var id:Boolean?=false,// 是否为一个记录区分的唯一标识
    val fields:List<ColumnResult>?=null
):Serializable {

//    var column:String?=null
//    var field:String?=null
//    var type:Class<*>?=null

    companion object {
        @Serial
        private const val serialVersionUID: Long = -8076800120569264817L

    }

}