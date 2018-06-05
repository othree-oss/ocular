package othree.ocular.clients

import java.io.{ByteArrayInputStream, FileInputStream, InputStream}
import java.util

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model._
import org.apache.http.HttpStatus
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import io.othree.aok.BaseTest
import io.othree.ocular.DataSource

@RunWith(classOf[JUnitRunner])
class S3ClientTest extends BaseTest {

  var amazonS3 : AmazonS3 = _
  var bucketName : String = _
  var s3Client : S3Client = _

  before {
    amazonS3 = mock[AmazonS3]
    bucketName = "testbucket"
    s3Client = new S3Client(amazonS3,bucketName)
  }

  "The S3Client" when {

    "read method is called with valid key" must {
      "read file content" in {
        val dataJson = new FileInputStream(classOf[S3ClientTest].getResource("/S3File").getPath)
        val s3Obj = new S3Object()
        s3Obj.setObjectContent(dataJson)

        when(amazonS3.getObject(bucketName, "testfile")).thenReturn(s3Obj)
        s3Client.read("testfile")

        verify(amazonS3, times(1)).getObject(bucketName, "testfile")
      }
    }

    "write method is called with valid key" must {
      "write file content" in {

        val testData = "test file data"
        s3Client.write("testkey", DataSource(new ByteArrayInputStream(testData.getBytes()), Some(testData.getBytes().length)))

        verify(amazonS3, times(1)).putObject(mockEq(bucketName), mockEq("testkey"), any(classOf[InputStream]), any(classOf[ObjectMetadata]))
      }
    }

    "delete method is called with valid key" must {
      "delete file" in {

        s3Client.delete("s3file")

        verify(amazonS3, times(1)).deleteObject(bucketName, "s3file")
      }
    }

    "exists methods is called with valid key" must {
      "object metadata must be return" in {

        val fileMetadata = new ObjectMetadata()
        when(amazonS3.getObjectMetadata(bucketName, "s3File")).thenReturn(fileMetadata)

        s3Client.exists("s3File")

        verify(amazonS3, times(1)).getObjectMetadata(bucketName, "s3File")
      }
    }

    "exists methods is called with invalid key" must {
      "throw AmazonS3Exception" in {

        when(amazonS3.getObjectMetadata(bucketName, "invalidKey")).thenThrow(classOf[AmazonS3Exception])

        val thrown = intercept[AmazonS3Exception] {
          s3Client.exists("invalidKey")
        }

        thrown.getClass.getName shouldEqual "com.amazonaws.services.s3.model.AmazonS3Exception"

      }
    }

    "exists methods is called with invalid key" must {
      "throw AmazonS3Exception with 404 status code and return false" in {

        val ase = new AmazonS3Exception("Failed getting Object Metadata")
        ase.setStatusCode(HttpStatus.SC_NOT_FOUND)

        when(amazonS3.getObjectMetadata(bucketName, "invalidKey")).thenThrow(ase)


        val result = s3Client.exists("invalidKey")

        result shouldEqual false

      }
    }

    "listContents is called with valid folder" must {
      "return list of object the folder contains" in {
        val listing: ObjectListing = mock[ObjectListing]
        val listNextBatch: ObjectListing = mock[ObjectListing]
        val fileSize = 946587

        val summary1 = new S3ObjectSummary()
        summary1.setBucketName(bucketName)
        summary1.setKey("file1")
        summary1.setSize(fileSize)

        val summary2 = new S3ObjectSummary()
        summary2.setBucketName(bucketName)
        summary2.setKey("fileA")
        summary2.setSize(fileSize)


        val summary3 = new S3ObjectSummary()
        summary3.setBucketName(bucketName)
        summary3.setKey("fileC")
        summary3.setSize(fileSize)

        val summaries = new util.ArrayList[S3ObjectSummary]
        summaries.add(summary1)
        summaries.add(summary2)

        val nextBatch = new util.ArrayList[S3ObjectSummary]
        nextBatch.add(summary3)

        when(listing.getObjectSummaries).thenReturn(summaries)
        when(listNextBatch.getObjectSummaries).thenReturn(nextBatch)

        when(listing.isTruncated).thenReturn(true)

        when(amazonS3.listObjects(bucketName,"foldername")).thenReturn(listing)
        when(amazonS3.listNextBatchOfObjects(listing)).thenReturn(listNextBatch)

        val contentList = s3Client.listContents("foldername")

        contentList.length shouldEqual 3

        contentList(0) shouldEqual "file1"
        contentList(1) shouldEqual "fileA"
        contentList(2) shouldEqual "fileC"
      }
    }

  }
}
