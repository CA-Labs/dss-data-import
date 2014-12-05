package com.calabs.dss.dataimport

import java.util

import org.scalatest.FunSpec

import scala.collection.mutable.{Map => MutableMap}
import scala.util.Success

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class XMLDataResourceSpec extends FunSpec {

  describe("An XML Data Resource extractor"){

    it("should correctly extract metrics from an XML data resource (resourceType=xml)"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/xml/file/example-file.ok.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/xml/file/example-file.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          // How to test files that are loaded from a path property read from a file? This will be different between machines!
          // Overwrite data source path by now with one existing relative to resources folder
          val newConfig = DataResourceConfig(getClass.getResource("/xml/file/example-file.xml").getPath, c._2, c._3)
          val xmlResource = XMLResource(newConfig, DataResourceMapping(m))
          val metrics = xmlResource.extractMetrics
          assert(metrics.isSuccess)
          assert(metrics.get.get("metric1") == Some("Everyday Italian"))
          assert(metrics.get.get("metric2") == Some("Giada De Laurentiis"))
          assert(metrics.get.get("metric3") == Some(2005))
          assert(metrics.get.get("metric4") == Some(30))
        }
        case _ => assert(false)
      }
    }

    it("should fail when trying to extract metrics from an XML data resource (resourceType=xml) with invalid config or mapping files"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/xml/file/example-file.ko.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/xml/file/example-file.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => assert(false)
        case _ => assert(true)
      }
    }

  }

}
