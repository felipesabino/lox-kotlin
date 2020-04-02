package com.sabino.lox.types

import java.util.Optional

class Return(val value: Optional<Any>) : RuntimeException(null, null, false, false)