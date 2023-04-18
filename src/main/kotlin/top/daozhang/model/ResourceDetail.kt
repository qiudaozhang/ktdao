package top.daozhang.model

import top.daozhang.annotation.Col
import top.daozhang.annotation.Table
import java.io.Serial
import java.io.Serializable

@Table(name = "resource_detail")
class ResourceDetail:Serializable {

    @Col
    var id:Long?=null
    @Col(name="resource_id")
    var resourceId:Long?=null
    @Col
    var name:String?=null

    companion object {
        @Serial
        private const val serialVersionUID: Long = -2908861150946397139L
    }
}