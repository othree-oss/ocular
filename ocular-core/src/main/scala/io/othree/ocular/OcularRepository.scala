package io.othree.ocular

import com.typesafe.scalalogging.LazyLogging
import io.othree.ocular.exceptions.{FileDeleteException, FileGetException, FilePutException}

import scala.concurrent.{ExecutionContext, Future}

abstract class OcularRepository[A, B](protected val isClient: InputStreamClient)(implicit ec: ExecutionContext)
  extends LazyLogging {

  def getKey(entityKey: B): String

  protected def readData(fileContent: DataSource)(implicit classTag: scala.reflect.ClassTag[A]): A

  protected def prepareData(entity: A): DataSource

  def get(entityKey: B)(implicit classTag: scala.reflect.ClassTag[A]): Future[Option[A]] = {
    val key = this.getKey(entityKey)

    Future {
      try {
        if (isClient.exists(key)) {
          val fileContent = isClient.read(key)
          val entity = readData(DataSource(fileContent.inputStream, fileContent.length.orElse(None)))
          logger.debug(s"File $key read")
          Some(entity)
        } else {
          None
        }
      } catch {
        case ex: Exception =>
          val errorMsg = s"Failed to get $key"
          logger.error(errorMsg, ex)
          throw new FileGetException(key, errorMsg, ex)
      }
    }
  }

  def put(entityKey: B, entity: A): Future[String] = {
    val key = this.getKey(entityKey)
    Future {
      logger.debug(s"Getting ready to save entity to key: $key")
      val data = prepareData(entity)
      try {

        isClient.write(key, DataSource(data.inputStream, data.length))
        logger.debug(s"File $key written}")
        key
      } catch {
        case ex: Exception =>
          val errorMsg = s"Failed to save data to key: $key"
          logger.error(errorMsg, ex)
          throw new FilePutException(key, errorMsg, ex)
      }
    }
  }

  def delete(entityKey: B): Future[String] = {
    val key = this.getKey(entityKey)
    Future {
      logger.debug(s"Getting ready to delete $key")
      try {
        if (isClient.exists(key)) {
          isClient.delete(key)
          logger.debug(s"File $key deleted")
        }
        key
      } catch {
        case ex: Exception =>
          val errorMsg = s"Failed to delete key $key"
          logger.error(errorMsg, ex)
          throw new FileDeleteException(key, errorMsg, ex)
      }
    }
  }

  def exists(entityKey: B): Boolean = {
    val key = this.getKey(entityKey)
    logger.debug(s"Getting ready to verify existence of key: $key")

    isClient.exists(key)
  }
}
