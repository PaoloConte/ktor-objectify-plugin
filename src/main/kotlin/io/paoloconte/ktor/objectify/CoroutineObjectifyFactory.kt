package io.paoloconte.ktor.objectify

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.googlecode.objectify.Objectify
import com.googlecode.objectify.ObjectifyFactory
import com.googlecode.objectify.cache.MemcacheService
import com.googlecode.objectify.impl.ObjectifyImpl
import com.googlecode.objectify.impl.ObjectifyOptions
import com.googlecode.objectify.impl.Transactor
import com.googlecode.objectify.util.Closeable
import java.util.*

internal class CoroutineObjectifyFactory(
    datastore: Datastore = DatastoreOptions.getDefaultInstance().service,
    memcache: MemcacheService? = null
): ObjectifyFactory(datastore, memcache) {

    // this is private is super class so recreate it and reimplement methods
    // alternative could be to use reflection to access the private field but performance hit
    val stacks: ThreadLocal<Deque<Objectify>> = ThreadLocal.withInitial { ArrayDeque() }

    override fun ofy(): Objectify {
        val stack = stacks.get()

        check(!stack.isEmpty()) {
            "Objectify context not started!?!."
        }

        return stack.last
    }

    override fun begin(): Closeable {
        return open()
    }

    private fun open(): ObjectifyImpl {
        val objectify = ObjectifyImpl(this)
        stacks.get().add(objectify)
        return objectify
    }

    override fun open(opts: ObjectifyOptions?, transactor: Transactor?): ObjectifyImpl {
        val objectify = ObjectifyImpl(this, opts, transactor)
        stacks.get().add(objectify)
        return objectify
    }

    override fun close(ofy: Objectify) {
        val stack = stacks.get()
        check(!stack.isEmpty()) { "You have already destroyed the Objectify context." }

        val popped = stack.removeLast()
        assert(popped === ofy) { "Mismatched objectify instances; somehow the stack was corrupted" }
    }

}
