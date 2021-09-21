package ir.am3n.relog

enum class Type(val code: Byte) {
    VERBOSE    (1.toByte()),
    DEBUG      (2.toByte()),
    INFO       (4.toByte()),
    WARN       (8.toByte()),
    ERROR      (16.toByte())
}