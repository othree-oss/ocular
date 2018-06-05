
package io.othree.ocular

import java.io.{ByteArrayInputStream, InputStream}

import io.othree.aok.AsyncBaseTest
import io.othree.ocular.exceptions.{FileDeleteException, FilePutException}
import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mockEq, _}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OcularRepositoryTest extends AsyncBaseTest {

  var isClient: InputStreamClient = _

  class MockOcularRepository(isClient: InputStreamClient)
    extends OcularRepository[String, String](isClient) {

    override def getKey(entityKey: String): String = entityKey

    override def readData(fileContent: DataSource)(implicit classTag: scala.reflect.ClassTag[String]): String = {
      val data = IOUtils.toByteArray(fileContent.inputStream)
      new String(data)
    }

    override def prepareData(entity: String): DataSource = {
      val byteData: Array[Byte] = entity.getBytes
      val inputStream: InputStream = new ByteArrayInputStream(byteData)
      DataSource(inputStream, Some(byteData.length))
    }

  }

  var fileRepository: MockOcularRepository = _
  val dataJson = """{"name":"othree"}"""


  before {

    isClient = org.mockito.Mockito.mock(classOf[InputStreamClient])

    val is = new ByteArrayInputStream(dataJson.getBytes)
    when(isClient.read("testkey")).thenReturn(DataSource(is, Some(dataJson.length)))

    when(isClient.exists("mock")).thenReturn(true)
    when(isClient.exists("testkey")).thenReturn(true)
    when(isClient.exists("deleteException")).thenReturn(true)

    when(isClient.write(mockEq("writeException"), any(classOf[DataSource]))).thenThrow(new RuntimeException("kapow!"))
    when(isClient.delete(mockEq("deleteException"))).thenThrow(new RuntimeException("kapow!"))

    fileRepository = new MockOcularRepository(isClient)

  }


  "FileRepository" when {

    "get file method is called " must {

      "return file content serialized" in {

        val eventualResult = fileRepository.get("testkey")

        eventualResult.map { maybeData =>
          maybeData shouldBe defined

          maybeData.get shouldBe dataJson
        }
      }
    }

    "get file method is called with invalid file" must {
      "return empty result" in {

        val eventualResult = fileRepository.get("invalidkey")

        eventualResult.map { maybeData =>
          maybeData shouldBe empty
        }
      }
    }

    "put method called" must {
      "add the file successfully" in {

        val eventualResult = fileRepository.put("newID", "some data")

        eventualResult.map { fileKey =>
          verify(isClient, times(1)).write(mockEq("newID"), any())
          fileKey shouldEqual "newID"
        }
      }
    }

    "put method called with invalid key" must {
      "throw BaseJsonException" in {

        val eventualException = recoverToExceptionIf[FilePutException] {
          fileRepository.put("writeException", "data")
        }

        eventualException.map { thrown =>
          thrown.key shouldEqual "writeException"
          thrown.getMessage shouldEqual "Failed to save data to key: writeException"
        }
      }
    }

    "delete method called with valid key" must {
      "execute delete successfully" in {

        val eventualResult = fileRepository.delete("mock")

        eventualResult.map { fileKey =>
          verify(isClient, times(1)).delete("mock")
          fileKey shouldEqual "mock"
        }
      }
    }

    "delete method called with nonexistent file" must {
      "delete method is not call" in {

        val eventualResult = fileRepository.delete("notFound")

        eventualResult.map { fileKey =>
          verify(isClient, times(0)).delete("notFound")
          fileKey shouldEqual "notFound"
        }
      }
    }


    "delete method called with invalid key" must {
      "throw BaseJsonException" in {

        val eventualException = recoverToExceptionIf[FileDeleteException] {
          fileRepository.delete("deleteException")
        }

        eventualException.map { thrown =>
          thrown.key shouldEqual "deleteException"
          thrown.getMessage shouldEqual "Failed to delete key deleteException"
        }
      }
    }


    "exists method called with valid key" must {
      "return true" in {

        val result = fileRepository.exists("mock")

        verify(isClient, times(1)).exists("mock")
        result shouldBe true
      }
    }


    "exists method called with invalid key" must {
      "return false" in {

        val result = fileRepository.exists("nonExistentKey")

        result shouldEqual false
      }
    }


    "getKey method called" must {
      "return complete file key" in {
        val result = fileRepository.getKey("thisIsTheKey")

        result shouldEqual "thisIsTheKey"
      }
    }
  }
}
