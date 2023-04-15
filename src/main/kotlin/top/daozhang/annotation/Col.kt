package top.daozhang.annotation


@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Col(val name:String="")
