package top.daozhang.dto

import top.daozhang.annotation.Col
import java.io.Serial
import java.io.Serializable


open class ResourceDto : Serializable {



    var id: Long? = null
    var level: String? = null
    var fullName: String? = null




    companion object {
        @Serial
        private const val serialVersionUID: Long = 853579234966115696L
    }

    override fun toString(): String {
        return "ResourceDto(id=$id, level=$level, fullName=$fullName)"
    }
}