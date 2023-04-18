import org.junit.Before
import org.junit.Test
import top.daozhang.dao.ResourceDao
import top.daozhang.dto.ResourceDto
import top.daozhang.model.Resource
import top.daozhang.parse.DbExecutor
import top.daozhang.parse.ParseTable

class JoinTest {

    @Before
    fun init() {
        ParseTable.parsePackage("top.daozhang.model")
        DbExecutor.showSql = true
        DbExecutor.init("localhost", 3354, "root", "root", "cloud3", "com.mysql.cj.jdbc.Driver")
    }

    @Test
    fun getId() {
//        val sql = """
//            select r.id,  concat(r.name,r.show_name) as full_name ,r.level, rd.id as detail_id, rd.resource_id, rd.name as detail_name  from resource r left join resource_detail rd  on r.id = rd.resource_id where r.id = #{id}
//        """.trimIndent()
//        println(sql)

        ResourceDao.getRsDetailById(391179018518598L)
    }


    @Test
    fun groupByTest() {

       val map = mutableListOf<MutableMap<String,Any>>(

            mutableMapOf(
                "id" to 1 ,
                "name" to "good"
            ),
            mutableMapOf(
                "id" to 1 ,
                "name" to "study"
            ),
            mutableMapOf(
                "id" to 2 ,
                "name" to "day day up"
            )
        )

        val data = map.groupBy { it["id"] }
        println(data)
    }


}