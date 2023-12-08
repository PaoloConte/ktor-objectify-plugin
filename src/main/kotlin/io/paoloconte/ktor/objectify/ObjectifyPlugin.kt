package io.paoloconte.ktor.objectify

import com.googlecode.objectify.ObjectifyService
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.withContext

val ObjectifyPlugin = createApplicationPlugin("ObjectifyPlugin") {
    // as a workaround to run multiple tests, factory is reused if already initialized
    val factory = try {
        ObjectifyService.factory() as CoroutineObjectifyFactory
    } catch(e: IllegalStateException) {
        initialise()
    }

    on(CallHook()) {  _, block ->
        withContext(ObjectifyContext(factory)) {
            ObjectifyService.begin().use {
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