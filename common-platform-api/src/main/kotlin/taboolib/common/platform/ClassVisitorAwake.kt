package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.function.warning
import taboolib.common.util.t

class ClassVisitorAwake(private val lifeCycle: LifeCycle) : ClassVisitor(0) {

    override fun visit(method: ClassMethod, owner: ReflexClass) {
        val enumName = method.getAnnotationIfPresent(Awake::class.java)?.enumName("value", "") ?: return
        if (enumName == "") {
            warning(
                """
                    @Awake 注解缺少参数: ${owner.name}#${method.name}
                    Missing parameter in @Awake annotation: ${owner.name}#${method.name}
                """.t()
            )
            return
        }
        if (enumName == lifeCycle.name) {
            val instance = findInstance(owner)
            if (instance != null) {
                method.invoke(instance)
            } else {
                method.invokeStatic()
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return lifeCycle
    }
}