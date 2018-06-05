package io.othree.ocular.exceptions

class FilePutException(val key: String,
                       message: String,
                       cause: Throwable)
  extends OcularException(message, Some(cause))
