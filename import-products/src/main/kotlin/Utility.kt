import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun getLogger(forClass: KClass<*>): Logger = LoggerFactory.getLogger(forClass.java)