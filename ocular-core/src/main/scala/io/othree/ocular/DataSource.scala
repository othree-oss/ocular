package io.othree.ocular

import java.io.InputStream

case class DataSource(inputStream: InputStream, length: Option[Long])
