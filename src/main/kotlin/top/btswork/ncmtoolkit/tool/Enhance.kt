package top.btswork.ncmtoolkit.tool

fun Int.reverseBytes() = Integer.reverseBytes(this)

fun Boolean.ifYes(func: () -> Unit) = also { if (this) func() }
fun Boolean.ifNot(func: () -> Unit) = also { if (this.not()) func() }