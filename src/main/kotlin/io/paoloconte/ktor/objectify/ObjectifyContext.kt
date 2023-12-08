package io.paoloconte.ktor.objectify

import com.googlecode.objectify.Objectify
import kotlinx.coroutines.ThreadContextElement
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class ObjectifyContext(
    private val factory: CoroutineObjectifyFactory
) : ThreadContextElement<Deque<Objectify>>, AbstractCoroutineContextElement(Key) {
    
    companion object Key : CoroutineContext.Key<ObjectifyContext>

    private val queue: Deque<Objectify> = ArrayDeque()

    override fun updateThreadContext(context: CoroutineContext): Deque<Objectify> {
        val old = factory.stacks.get()
        factory.stacks.set(queue)
        return old
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Deque<Objectify>) {
        factory.stacks.set(oldState)
    }
}
