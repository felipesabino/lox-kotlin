package com.sabino.lox

import java.util.Optional

class Return(val value: Optional<Any>) : RuntimeException(null, null, false, false)