package io.othree.ocular.exceptions

class FileDeleteException(val key: String,
                          message: String,
                          cause: Throwable)
  extends OcularException(message, Some(cause))
