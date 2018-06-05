package io.othree.ocular.clients

import java.io.{ByteArrayInputStream, File}
import java.nio.file.Paths

import io.othree.aok.BaseTest
import io.othree.ocular.DataSource
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class LocalFileClientTest extends BaseTest {

  var fileClient : LocalFileClient = _
  var baseDirectory : String = _

  before {
    baseDirectory = new File(classOf[LocalFileClientTest].getResource("/baseDirectory/MockEntityJson.json").getPath).getParent
    fileClient = new LocalFileClient(baseDirectory)
  }

  "The LocalFileClient" when {

    "read method is called for a json file" must {
      "return file content" in {

        val content = fileClient.read("MockEntityJson.json")

        content.length.isDefined shouldEqual true
        content.length.get shouldEqual 89L

      }
    }

    "read method is called for a csv file" must {
      "return file content" in {
        val content = fileClient.read("MockEntityCsv.csv")

        content.length.isDefined shouldEqual true
        content.length.get shouldEqual 35L
      }
    }

    "write file method is called with valid content" must {
      "save file successfully" in {

        val json = """{"name":"othree"}"""

        val is = new ByteArrayInputStream(json.getBytes())

        fileClient.write("entity.json", DataSource(is, Some(json.getBytes.length)))

        val file = Paths.get(baseDirectory, "entity.json").toFile

        file.exists() shouldEqual true
      }
    }

    "delete file method" must {
      "remove file successfully" in {

        val fileKey = "willBeDeletedInTest.json"

        val file = Paths.get(baseDirectory, fileKey).toFile

        file.exists() shouldEqual true

        fileClient.delete(fileKey)

        file.exists() shouldEqual false

      }
    }

    "exists file method" must {
      "return true" in {

        val exists = fileClient.exists("MockEntityJson.json")

        exists shouldEqual true
      }
    }

    "list folder contents method" must {
      "return list of files" in {

        val list = fileClient.listContents("")

        list should contain ("/MockEntityCsv.csv")
        list should contain ("/MockEntityJson.json")
        list should contain ("/entity.json")
      }
    }
  }
}
