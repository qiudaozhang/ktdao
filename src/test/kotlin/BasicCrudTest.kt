import org.junit.Before
import org.junit.Test
import top.daozhang.dto.ResourceDto
import top.daozhang.model.Resource
import top.daozhang.parse.DbExecutor
import top.daozhang.parse.ParseTable

class BasicCrudTest {

    @Before
    fun init() {
        ParseTable.parsePackage("top.daozhang.model")
        DbExecutor.showSql = true
        DbExecutor.init("localhost", 3354, "root", "root", "cloud3", "com.mysql.cj.jdbc.Driver")
    }

    @Test
    fun getId() {
        val r = DbExecutor.getId(391179018518598L, Resource::class.java)
        println(r)
    }

    @Test
    fun queryIn() {
        val data = DbExecutor.query(
            "select id ,name,icon,show_name,created,updated,deleted from resource where id in #{  @loop(idList)   } or show_name = #{   showName  }",
            mutableMapOf(
                "idList" to listOf(391179018518598L, 391272015003717L),
                "showName" to "大威天龙"
            ),
            Resource::class.java
        )
        data.forEach { println(it) }
    }


    @Test
    fun updateTest() {
        val r = DbExecutor.getId(391179018518598L, Resource::class.java)

        r?.let {
            r.name = "月光星子"
            val matched = DbExecutor.updId(r, Resource::class.java)
        }
    }


    @Test
    fun sqlToToken(){

        println(DbExecutor.sqlToToken(
            "select id,    name,icon from user     where id in #{  @loop  (idList)   } or show_name = #{ showName }"
        ))
    }


    @Test
    fun customResultTest() {
        val data = DbExecutor.query(
            "select id, concat(name,show_name) as full_name , level  from resource where id = #{id }",
            mutableMapOf(
                "id" to 391179018518598
            ),
            ResourceDto::class.java
        )
        data.forEach { println(it) }
    }
    @Test
    fun customResultTest2() {
        val start = System.currentTimeMillis()
        for ( i in 1 .. 1000){
            val data = DbExecutor.query(
                "select id, concat(name,show_name) as full_name , level  from resource where id = #{id }",
                mutableMapOf(
                    "id" to 391179018518598
                ),
                ResourceDto::class.java
            )
        }

        val end = System.currentTimeMillis()
        println((end-start))
    }
}