package com.fine.samples.rustforkmp

import com.fine.samples.arithmeticpm.add
import com.fine.samples.arithmeticpm.div
import com.fine.samples.arithmeticpm.equal
import com.fine.samples.arithmeticpm.sub


actual fun rustAdd(a: ULong, b: ULong): ULong = add(a, b)

actual fun rustSub(a: ULong, b: ULong): ULong = sub(a, b)

actual fun rustDiv(a: ULong, b: ULong): ULong = div(a, b)

actual fun rustEqual(a: ULong, b: ULong): Boolean = equal(a, b)
