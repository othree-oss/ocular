package io.othree.ocular.exceptions

class OcularException(message: String,
                      cause: Option[Throwable] = None)
  extends Exception(message, cause.orNull)
