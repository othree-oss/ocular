package othree.ocular.clients

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{AmazonS3Exception, ObjectListing, ObjectMetadata}
import io.othree.ocular.{DataSource, InputStreamClient}
import org.apache.http.HttpStatus

import scala.collection.mutable.ArrayBuffer

class S3Client(amazonS3 : AmazonS3, bucket : String) extends InputStreamClient {

  override def read(key : String) : DataSource = {
    val obj = amazonS3.getObject(bucket, key)
    DataSource(obj.getObjectContent,Some(obj.getObjectMetadata.getContentLength))
  }

  override def write(key : String, data : DataSource) : Unit = {
    val metaData = new ObjectMetadata
    if (data.length.isDefined) {
      metaData.setContentLength(data.length.get)
    }
    amazonS3.putObject(bucket, key, data.inputStream, metaData)
  }

  override def delete(key : String) : Unit = {
    amazonS3.deleteObject(bucket, key)
  }

  override def exists(key: String): Boolean = {
    try {
      amazonS3.getObjectMetadata(bucket, key)
      true
    } catch {
      case e : AmazonS3Exception => {
        if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          false
        }
        else {throw e}
      }
    }
  }

  override def listContents(folder:String): Array[String] = {
    val fileList= new ArrayBuffer[String]
    var listing: ObjectListing = amazonS3.listObjects(bucket, folder)
    val summaries = listing.getObjectSummaries

    while (listing.isTruncated) {
      listing = amazonS3.listNextBatchOfObjects(listing)
      summaries.addAll(listing.getObjectSummaries)
    }

    summaries.forEach { s3ObjectSummary =>
      s3ObjectSummary.getKey
      fileList+=s3ObjectSummary.getKey
    }

    fileList.toArray
  }

}
