package io.othree.ocular.utils

import java.io.{ByteArrayOutputStream, InputStream}

class InputStreamIterator(is: InputStream, chunkSize: Int) extends Iterator[Array[Byte]] {

  require(chunkSize > 0)

  private var hasRemaining = true
  private var nextByte : Int = -1

  getNextByte()

  override def hasNext : Boolean = hasRemaining

  override def next(): Array[Byte] = {
    var i: Int = 0
    var bytesRead = 1

    val output: ByteArrayOutputStream = new ByteArrayOutputStream
    output.write(nextByte)

    while (hasRemaining && bytesRead < chunkSize) {
      i=is.read()
      if (i == -1) {
        hasRemaining = false
      }else{
        output.write(i)
        bytesRead += 1
      }
    }

    getNextByte()

    output.toByteArray
  }

  private def getNextByte() : Unit = {
    nextByte = is.read()
    if (nextByte == -1) {
      hasRemaining = false
    }
  }
}
