package top.daozhang.dto

import top.daozhang.model.ResourceDetail
import java.io.Serializable


class ResourceWithDetailDto : ResourceDto(), Serializable {

    var detailList: List<ResourceDetail>? = null

}