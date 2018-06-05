package io.othree.ocular.clients

import java.io.{File, FileInputStream}

import io.othree.ocular.{DataSource, InputStreamClient}
import org.apache.commons.io.FileUtils

class LocalFileClient(baseDirectoryPath : String) extends InputStreamClient {
  override def read(key: String): DataSource = {
    val file = getFile(key)

    val is = new FileInputStream(file)

    DataSource(is, Some(file.length()))
  }

  override def listContents(folder: String): Array[String] = {
    val file = getFile(folder)

    file.listFiles().map(f => f.getAbsolutePath.replaceFirst(baseDirectoryPath,""))
  }

  override def delete(key: String): Unit = {
    val file = getFile(key)

    file.delete()
  }

  override def write(key: String, data: DataSource): Unit = {
    val file = getFile(key)

    FileUtils.copyInputStreamToFile(data.inputStream, file)
  }

  override def exists(key: String): Boolean = {
    val file = getFile(key)

    file.exists() && !file.isDirectory
  }

  private def getFile(key : String) : File = {
    new File(s"$baseDirectoryPath${java.io.File.separator}$key")
  }
}
