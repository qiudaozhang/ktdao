package top.daozhang.dto

import top.daozhang.model.ResourceDetail
import java.io.Serial
import java.io.Serializable


class ResourceWithDetailDto : ResourceDto(), Serializable {



    var detailList: List<ResourceDetail>? = null


    companion object {
        @Serial
        private const val serialVersionUID: Long = 129340992492288855L

    }

    override fun toString(): String {
        return "ResourceWithDetailDto(${super.toString()}, detailList=$detailList)"
    }

}