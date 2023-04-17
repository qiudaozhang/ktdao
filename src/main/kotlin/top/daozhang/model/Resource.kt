package top.daozhang.model

import top.daozhang.annotation.Col
import top.daozhang.annotation.Id
import top.daozhang.annotation.Table
import java.time.LocalDateTime

@Table(name = "resource")
class Resource {


    @Id
    @Col(name = "id")
    var id:Long?=null
    @Col
    var icon:String?=null
    @Col
    var type:String?=null
    @Col
    var name:String?=null
    @Col(name="show_name")
    var showName:String?=null
    @Col
    var level:Int?=null
    @Col
    var pid:Long?=null
    @Col
    var created:LocalDateTime?=null
    @Col
    var updated:LocalDateTime?=null
    @Col
    var deleted:Int?=null
    override fun toString(): String {
        return "Resource(id=$id, icon=$icon, type=$type, name=$name, showName=$showName, level=$level, pid=$pid, created=$created, updated=$updated, deleted=$deleted)"
    }


}