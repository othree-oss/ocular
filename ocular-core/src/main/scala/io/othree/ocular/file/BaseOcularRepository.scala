package io.othree.ocular.file

import io.othree.ocular.{DataSource, InputStreamClient, OcularRepository}

import scala.concurrent.ExecutionContext

abstract class BaseOcularRepository[A](isClient: InputStreamClient)(implicit val ec : ExecutionContext)
  extends OcularRepository[DataSource, A](isClient) {

  override protected def readData(fileContent: DataSource)(implicit classTag : scala.reflect.ClassTag[DataSource]): DataSource = {
    fileContent
  }

  override protected def prepareData(fileContent: DataSource): DataSource = {
    fileContent
  }
}
