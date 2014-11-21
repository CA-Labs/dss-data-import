package com.calabs.dss.dataimport

import org.scalatest.FunSpec

import scala.io.Source

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class DataResourceSpec extends FunSpec {

  describe("A data resource"){

    it("should correctly parse a JSON data resource"){
      val mapping = Source.fromURL(getClass.getResource("/json/mapping.txt")).getLines().map(line => line.split("="))
      val jsonDataResource = JSONResource(DataResourceMapping(Map("")))
    }

    ignore("should correctly parse an XML data resource"){
      assert(false)
    }

  }

}
