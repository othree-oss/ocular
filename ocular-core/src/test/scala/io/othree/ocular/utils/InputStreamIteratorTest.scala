package io.othree.ocular.utils

import java.io.ByteArrayInputStream

import io.othree.aok.BaseTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class InputStreamIteratorTest extends BaseTest {

  "InputStreamIterator" when {
    "asked for bytes" must {
      "return the ones in range" in {
        val is = new ByteArrayInputStream("0123456789---".getBytes)

        val inputStreamIterator = new InputStreamIterator(is, 5)

        val first5 = inputStreamIterator.next()

        first5.length shouldEqual 5
        inputStreamIterator.hasNext shouldEqual true

        val next5 = inputStreamIterator.next

        next5.length shouldEqual 5
        inputStreamIterator.hasNext shouldEqual true

        val last3 = inputStreamIterator.next

        last3.length shouldEqual 3
        inputStreamIterator.hasNext shouldEqual false
      }
    }

    "built with an incorrect chunk size" must {
      "throw an exception" in {
        val is = new ByteArrayInputStream("0123456789---".getBytes)

        intercept[IllegalArgumentException] {
          new InputStreamIterator(is, 0)
        }
      }
    }
  }

}