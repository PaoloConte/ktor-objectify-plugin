package io.paoloconte.ktor.objectify

import com.googlecode.objectify.ObjectifyService
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.withContext

/**
 * The plugin install a call hook that allow to wrap the execution of the call to create and close the objectify instance
 * ObjectifyFactory uses thread local to store Objectify instances but coroutines can run on different threads during
 * their lives; so a ThreadContextElement is used to switch the correct list of Objectify instances for each thread.
 * Since the ThreadLocal with the lists is private in ObjectifyFactory and factory is accessed with static functions,
 * we also need to create a new factory that allows us to control the ThreadLocal
 */

val ObjectifyPlugin = createApplicationPlugin("ObjectifyPlugin") {
    // as a workaround to run multiple tests, factory is reused if already initialized
    val factory = try {
            ObjectifyService.factory() as CoroutineObjectifyFactory
        } catch(e: IllegalStateException) {
            initialise()
        }

    on(CallHook()) {  _, block ->
        withContext(ObjectifyContext(factory)) {
            val objectify = ObjectifyService.begin()
            objectify.use {
                block()
            }
        }
    }
}

private fun initialise(): CoroutineObjectifyFactory {
    val factory = CoroutineObjectifyFactory()
    ObjectifyService.init(factory)
    return factory
}

private class CallHook(): Hook<suspend (ApplicationCall, suspend () -> Unit) -> Unit> {
    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (ApplicationCall, suspend () -> Unit) -> Unit
    ) {
        val phase = ApplicationPhase.Call
        val objPhase = PipelinePhase("${phase.name}_objectify")
        pipeline.insertPhaseBefore(phase, objPhase)

        pipeline.intercept(objPhase) {
            handler(call, ::proceed)
        }
    }

}