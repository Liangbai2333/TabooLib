package taboolib.common.platform.event

import org.tabooproject.reflex.ClassAnnotation
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.event.InternalEvent
import taboolib.common.event.InternalEventBus
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.Ghost
import taboolib.common.platform.Platform
import taboolib.common.platform.function.*
import taboolib.common.util.optional
import taboolib.common.util.t

@Awake
@Inject
class EventBus : ClassVisitor(-1) {

    @Suppress("UNCHECKED_CAST")
    override fun visit(method: ClassMethod, owner: ReflexClass) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameter.size == 1) {
            val anno = method.getAnnotation(SubscribeEvent::class.java)
            val bind = anno.property("bind", "")
            val optionalEvent = if (bind.isNotEmpty()) {
                try {
                    Class.forName(bind)
                } catch (ex: ClassNotFoundException) {
                    null
                }
            } else {
                null
            }
            if (method.parameter.size != 1) {
                error("${owner.name}#${method.name} must have 1 parameter and must be an event type")
            }
            val listenType = try {
                method.parameter[0].instance
            } catch (_: ClassNotFoundException) {
                null
            }
            // 未找到事件类
            if (listenType == null) {
                // 忽略警告
                if (!method.isAnnotationPresent(Ghost::class.java)) {
                    warning(
                        """
                            事件 ${method.parameter[0].name} 未能找到，可使用 @Ghost 关闭此警告。
                            ${method.parameter[0].name} not found, use @Ghost to turn off this warning.
                        """.t()
                    )
                }
                return
            }
            optional(anno) {
                val obj = findInstance(owner)
                // 内部事件处理
                if (InternalEvent::class.java.isAssignableFrom(listenType)) {
                    val priority = anno.enum("priority", EventPriority.NORMAL)
                    val ignoreCancelled = anno.property("ignoreCancelled", false)
                    InternalEventBus.listen(listenType as Class<InternalEvent>, priority.level, ignoreCancelled) { invoke(obj, method, it) }
                    return
                }
                // 判定运行平台
                when (runningPlatform) {
                    Platform.BUKKIT -> registerBukkit(method, optionalEvent, anno, obj)
                    Platform.BUNGEE -> registerBungee(method, optionalEvent, anno, obj)
                    Platform.VELOCITY -> registerVelocity(method, optionalEvent, anno, obj)
                    Platform.AFYBROKER -> registerAfyBroker(method, optionalEvent, anno, obj)
                    else -> {}
                }
            }
        }
    }

    private fun registerBukkit(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val priority = event.enum("priority", EventPriority.NORMAL)
        val ignoreCancelled = event.property("ignoreCancelled", false)
        val listenType = method.parameterTypes[0]
        if (listenType == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerBukkitListener(optionalBind, priority, ignoreCancelled) { invoke(obj, method, it, true) }
            }
        } else {
            registerBukkitListener(listenType, priority, ignoreCancelled) { invoke(obj, method, it) }
        }
    }

    private fun registerBungee(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val annoLevel = event.property("level", -1)
        val level = if (annoLevel != 0) annoLevel else event.enum("priority", EventPriority.NORMAL).level
        val ignoreCancelled = event.property("ignoreCancelled", false)
        val listenType = method.parameterTypes[0]
        if (listenType == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerBungeeListener(optionalBind, level, ignoreCancelled) { invoke(obj, method, it, true) }
            }
        } else {
            registerBungeeListener(listenType, level, ignoreCancelled) { invoke(obj, method, it) }
        }
    }

    private fun registerAfyBroker(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val annoLevel = event.property("level", -1)
        val level = if (annoLevel != 0) annoLevel else event.enum("priority", EventPriority.NORMAL).level
        val ignoreCancelled = event.property("ignoreCancelled", false)
        val listenType = method.parameterTypes[0]
        if (listenType == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerAfyBrokerListener(optionalBind, level, ignoreCancelled) { invoke(obj, method, it, true) }
            }
        } else {
            registerAfyBrokerListener(listenType, level, ignoreCancelled) { invoke(obj, method, it) }
        }
    }

    private fun registerVelocity(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val postOrder = event.enum("postOrder", PostOrder.NORMAL)
        val listenType = method.parameterTypes[0]
        if (listenType == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerVelocityListener(optionalBind, postOrder) { invoke(obj, method, it, true) }
            }
        } else {
            registerVelocityListener(listenType, postOrder) { invoke(obj, method, it) }
        }
    }

    private fun invoke(obj: Any?, method: ClassMethod, it: Any, optional: Boolean = false) {
        if (obj != null) {
            method.invoke(obj, if (optional) OptionalEvent(it) else it)
        } else {
            method.invokeStatic(if (optional) OptionalEvent(it) else it)
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ENABLE
    }
}