package io.othree.ocular.exceptions

class FileGetException(val key: String,
                       message: String,
                       cause: Throwable)
  extends OcularException(message, Some(cause))
