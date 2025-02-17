
internal typealias Pointer = com.sun.jna.Pointer
internal val NullPointer: Pointer? = com.sun.jna.Pointer.NULL
internal fun getPointerNativeValue(ptr: Pointer): Long = Pointer.nativeValue(ptr)
internal fun kotlin.Long.toPointer() = com.sun.jna.Pointer(this)
