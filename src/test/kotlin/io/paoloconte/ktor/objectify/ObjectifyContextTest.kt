package io.paoloconte.ktor.objectify

import com.googlecode.objectify.ObjectifyService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forAll
import io.kotest.inspectors.forAtLeast
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*


class `Objectify Context Test` : BehaviorSpec({

    data class TestResultItem(val ofy: Int, val thread: String)
    data class TestResult(val instances: List<TestResultItem>)

    suspend fun task(): TestResult {
        ObjectifyService.begin().use {
            val ofy1 = ObjectifyService.ofy().hashCode()
            val thread1 = Thread.currentThread().name
            yield()
            val ofy2 = ObjectifyService.ofy().hashCode()
            val thread2 = Thread.currentThread().name
            yield()
            val ofy3 = ObjectifyService.ofy().hashCode()
            val thread3 = Thread.currentThread().name

            return TestResult(listOf(
                TestResultItem(ofy1, thread1),
                TestResultItem(ofy2, thread2),
                TestResultItem(ofy3, thread3),
            ))
        }
    }

    Given("a Objectify service with Coroutine Factory") {
        When("inside a ObjectifyContext") {
            Then("every coroutine should always get the same instance of ofy()") {

                val factory = CoroutineObjectifyFactory()
                ObjectifyService.init(factory)

                runBlocking(Dispatchers.IO) {
                    val tasks = (0 until 100).map {
                        async {
                            withContext(ObjectifyContext(factory)) {
                                task()
                            }
                        }
                    }
                    val results = tasks.map { it.await() }

                    results.size shouldBe 100

                    // verify that in all cases the instance is always the same
                    results.forAll {
                        val first = it.instances.first().ofy
                        it.instances.all { it.ofy == first }
                    }

                    // verify that the thread changes within the same coroutine
                    results.forAtLeast(90) {
                        val first = it.instances.first().thread
                        it.instances.any { it.thread != first }
                    }

                }
            }
        }
    }
})