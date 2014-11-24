package com.calabs.dss.dataimport

import java.util

import org.scalatest.FunSpec
import scala.collection.mutable.{Map => MutableMap}
import scala.io.Source

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class DataResourceSpec extends FunSpec {

  describe("A data resource extractor"){

    it("should correctly extract metrics from a JSON data resource"){
      // Populate data mapping (metric name, metric key)
      val dataMapping = MutableMap[String,String]()
      val metricNameKeysFile = Source.fromFile(getClass.getResource("/json/mapping.txt").getPath)
      val metricNameKeys = for {metricKey <- metricNameKeysFile.getLines()} yield (metricKey.split("=").head, metricKey.split("=").tail.head)
      metricNameKeys.foreach(nameKey => dataMapping.update(nameKey._1, nameKey._2))

      // Extract metrics from JSONResource
      val jsonDataResource = JSONResource(DataResourceMapping(dataMapping.toMap), getClass.getResource("/json/example.json").getPath)
      // Not much idiomatic, but ArrayList is used for array values instead of Scala collections
      assert(jsonDataResource.extractMetrics == Map(("metric1", Right("bar")), ("metric2", Right(2.0)), ("metric3", Right(new util.ArrayList(util.Arrays.asList(1,2,3))))))
    }

    it("should correctly parse an XML data resource"){
      // Populate data mapping (metric name, metric key)
      val dataMapping = MutableMap[String,String]()
      val metricNameKeysFile = Source.fromFile(getClass.getResource("/xml/mapping.txt").getPath)
      val metricNameKeys = for {metricKey <- metricNameKeysFile.getLines()} yield (metricKey.split("=").head, metricKey.split("=").tail.head)
      metricNameKeys.foreach(nameKey => dataMapping.update(nameKey._1, nameKey._2))

      // Extract metrics from XMLResource
      val xmlDataResource = XMLResource(DataResourceMapping(dataMapping.toMap), getClass.getResource("/xml/example.xml").getPath)
      assert(xmlDataResource.extractMetrics == Map(("metric1", Right("Everyday Italian")), ("metric2", Right("Giada De Laurentiis")), ("metric3", Right(2005)), ("metric4", Right(30))))
    }

  }

}
