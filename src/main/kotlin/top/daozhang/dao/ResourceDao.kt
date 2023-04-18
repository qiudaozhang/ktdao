package top.daozhang.dao

import top.daozhang.dto.ResourceWithDetailDto
import top.daozhang.meta.ColumnResult
import top.daozhang.meta.ResultMap
import top.daozhang.model.ResourceDetail
import top.daozhang.parse.DbExecutor

object ResourceDao {


    fun rsDetail(): ResultMap {

        val resultMap = ResultMap()

        val cols = mutableListOf<ColumnResult>()
        cols += ColumnResult("id", "id", Long::class.java, id = true)
        cols += ColumnResult("full_name", "fullName", String::class.java)
        cols += ColumnResult("level", "level", String::class.java)
        val detailCols = mutableListOf<ColumnResult>()
        detailCols += ColumnResult("resource_id", "id", Long::class.java)
        detailCols += ColumnResult("detail_name", "name", String::class.java)
        detailCols += ColumnResult("resource_id", "resourceId", Long::class.java)
        cols += ColumnResult("detailList", type = ResourceDetail::class.java, fields = detailCols, simple = false)
        // 一对多
        resultMap.cols = cols
        resultMap.id = "baseResourceDetail"
        resultMap.type = ResourceWithDetailDto::class.java
        return resultMap
//        return cols
    }


    fun getRsDetailById(id:Long){

//        DbExecutor.query()
        val sql = """
            select r.id,  concat(r.name,r.show_name) as full_name ,r.level, rd.id as detail_id,
             rd.resource_id, rd.name as detail_name  from resource r left join resource_detail rd  
             on r.id = rd.resource_id where r.id = #{id} 
        """.trimIndent()
       val data = DbExecutor.query(sql,
            mutableMapOf(
                "id" to id
            ),
            rsDetail()
        )

    }
}