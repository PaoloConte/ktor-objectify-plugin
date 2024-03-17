# What is this?
The Objectify library is not compatible with coroutines, as it relies on Thread locals to manage its state. For this reason, the ObjectifyFilter (or similar approaches) can't be used with Ktor.  This plugin provides the necessary glue code to make Objectify work with coroutines in Ktor.

# Usage
```kotlin
dependencies {
    implementation("io.paoloconte:ktor-objectify:1.0")
}
```
```kotlin
fun Application.module() {
    install(ObjectifyPlugin)
}
```

# How it works
The plugin installs a call hook that allows to wrap the execution of the call to create and close the objectify instance.  
ObjectifyFactory uses thread local to store Objectify instances, but coroutines can run on different threads during
their lives; so a ThreadContextElement is used to switch the correct list of Objectify instances for each thread.  
Since the ThreadLocal with the lists is private in ObjectifyFactory and the factory is accessed with static functions,
we also need to create a new factory that allows us to control the ThreadLocal and associated methods.
