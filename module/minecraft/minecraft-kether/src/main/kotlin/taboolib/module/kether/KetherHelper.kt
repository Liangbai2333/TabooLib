package taboolib.module.kether

import com.mojang.datafixers.kinds.App
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.warning
import taboolib.common.util.t
import taboolib.library.kether.*
import taboolib.library.kether.Parser.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

typealias Script = Quest

typealias ScriptFrame = QuestContext.Frame

/**
 * 运行 Kether 语句并打印错误
 */
fun <T> runKether(el: T? = null, detailError: Boolean = false, function: () -> T): T? {
    try {
        return function()
    } catch (ex: Exception) {
        ex.printKetherErrorMessage(detailError)
    }
    return el
}

/**
 * 创建 ScriptParser 对象
 */
fun <T> scriptParser(resolve: (QuestReader) -> QuestAction<T>): ScriptActionParser<T> {
    return ScriptActionParser(resolve)
}

fun <T> combinationParser(builder: ParserHolder.(Instance) -> App<Mu, Action<T>>): ScriptActionParser<T> {
    val parser = build(builder(ParserHolder, instance()))
    return ScriptActionParser { parser.resolve<T>(this) }
}

/**
 * 从字符串创建 Script 对象
 */
fun String.parseKetherScript(namespace: List<String> = emptyList()): Script {
    return KetherScriptLoader().load(ScriptService, "temp_${UUID.randomUUID()}", toByteArray(StandardCharsets.UTF_8), namespace)
}

/**
 * 从字符串列表创建 Script 对象
 */
fun List<String>.parseKetherScript(namespace: List<String> = emptyList()): Script {
    return joinToString("\n").parseKetherScript(namespace)
}

/**
 * 在 Frame 中运行 ParsedAction
 */
fun ScriptFrame.run(action: ParsedAction<*>): CompletableFuture<Any?> {
    return newFrame(action).run()
}

/**
 * 获取玩家
 */
fun ScriptFrame.player(): ProxyPlayer {
    return script().sender as? ProxyPlayer ?: error("No player selected.")
}

/**
 * 获取脚本上下文
 */
fun ScriptFrame.script(): ScriptContext {
    return context() as ScriptContext
}

/**
 * 继承变量
 */
fun ScriptContext.extend(map: Map<String, Any?>) {
    rootFrame().variables().run { map.forEach { (k, v) -> set(k, v) } }
}

/**
 * 获取所有变量
 */
fun ScriptFrame.deepVars(): HashMap<String, Any?> {
    val map = HashMap<String, Any?>()
    var parent = parent()
    while (parent.isPresent) {
        map.putAll(parent.get().variables().toMap())
        parent = parent.get().parent()
    }
    map.putAll(variables().toMap())
    return map
}

/**
 * 打印 Kether 错误信息
 */
fun Throwable.printKetherErrorMessage(detailError: Boolean = false) {
    if (localizedMessage == null || detailError) {
        printStackTrace()
        return
    }
    if (javaClass.name.endsWith("kether.LocalizedException") || javaClass.name.endsWith("kether.LocalizedException\$Concat")) {
        warning(
            """
                解析 Kether 语句时发生了意外的异常：
                Unexpected exception while parsing kether script:
            """.t()
        )
    } else {
        warning(
            """
                运行 Kether 语句时发生了意外的异常：
                Unexpected exception while running the kether script.
            """.t()
        )
    }
    localizedMessage.split('\n').forEach { warning(it) }
}

/**
 * 类型适配
 */
fun Any?.inferType(): Any? {
    if (this !is String) return this
    toIntOrNull()?.let { return it }
    toLongOrNull()?.let { return it }
    toDoubleOrNull()?.let { return it }
    toBooleanStrictOrNull()?.let { return it }
    return this
}