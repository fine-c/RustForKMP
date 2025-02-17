{% include "ffi/ObjectCleanerHelper.kt" %}
{{- self.add_import("kotlinx.atomicfu.atomic") }}
{{- self.add_import("kotlinx.atomicfu.AtomicBoolean") }}
{{- self.add_import("kotlinx.coroutines.Runnable") }}
{{- self.add_import("kotlin.native.ref.createCleaner") }}

private class NativeCleaner : UniffiCleaner {
    override fun register(value: Any, cleanUpTask: Runnable): UniffiCleaner.Cleanable =
        // TODO: ignore value here. See UniffiNativeCleanable.pseudoResource for details.
        UniffiNativeCleanable(cleanUpTask)
}

private class UniffiNativeCleanable(val cleanUpTask: Runnable) : UniffiCleaner.Cleanable {
    // In Kotlin/Native, if a cleaner object is referenced by the resource object, both objects
    // might leak and cleanUpTask won't be called.
    //
    // Since this cleanable instance is referenced by the resource instance, making a "pseudo"
    // resource object prevents the cleaner from referencing the real resource instance while
    // invoking the clean up task at a proper moment.
    private val pseudoResource = Any()
    private val cleaned = atomic(false)

    @Suppress("unused")
    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(
        pseudoResource, UniffiNativeCleanAction(cleaned, cleanUpTask),
    )

    override fun clean() {
        if (cleaned.compareAndSet(false, true)) {
            cleanUpTask.run()
        }
    }

    private class UniffiNativeCleanAction(
        private val cleaned: AtomicBoolean,
        private val cleanUpTask: Runnable,
    ) : (Any) -> Unit {
        override fun invoke(resource: Any) {
            if (cleaned.compareAndSet(false, true)) {
                cleanUpTask.run()
            }
        }
    }
}

private fun UniffiCleaner.Companion.create(): UniffiCleaner =
    NativeCleaner()
