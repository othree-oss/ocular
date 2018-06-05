package io.othree.ocular

trait InputStreamClient {
  def read(key : String) : DataSource
  def write(key : String, data : DataSource) : Unit
  def delete(key : String) : Unit
  def exists(key : String) : Boolean
  def listContents(folder:String):Array[String]
}
