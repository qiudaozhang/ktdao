package top.daozhang.model

import top.daozhang.annotation.Col
import top.daozhang.annotation.Id
import top.daozhang.annotation.Table

@Table
class User {


    @Id
    @Col(name = "user_id")
    var id:Long?=null


    @Col
    var userName:String?=null


    @Col
    var age:Int?=null
}