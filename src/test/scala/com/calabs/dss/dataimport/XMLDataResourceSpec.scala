package com.calabs.dss.dataimport

import java.util

import org.json4s.JsonAST.{JString, JDouble}
import org.scalatest.FunSpec

import scala.collection.mutable.{Map => MutableMap}
import scala.util.{Failure, Success}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class XMLDataResourceSpec extends FunSpec {

  import Config._

  val resourceMapper = DataResourceMapper()

  describe("An XML Data Resource extractor"){

    it("should correctly extract metrics from an XML data resource (resourceType=xml)"){
      val config = xmlResourceConfig.load(getClass.getResource("/xml/file/example-file.ok.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/xml/file/example-file.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          // How to test files that are loaded from a path property read from a file? This will be different between machines!
          // Overwrite data source path by now with one existing relative to resources folder
          val newConfig = DataResourceConfig(Map[String,Any](("source" -> (getClass.getResource("/xml/file/example-file.xml").getPath)), ("resourceType" -> ResourceType.XML)))
          val xmlResource = XMLResource(newConfig, DataResourceMapping(m))
          val documents = xmlResource.extractDocuments
          documents match {
            case Success(docs) => {
              val (vertices, edges) = (docs._1, docs._2)
              assert(!vertices.isEmpty)
              assert(edges.isEmpty)
              assert(vertices(0).isVertex)
              assert(vertices(0).props.get("metric1") == Some(JString("Everyday Italian")))
              assert(vertices(0).props.get("metric2") == Some(JString("Giada De Laurentiis")))
              assert(vertices(0).props.get("metric3") == Some(JDouble(2005.0)))
              assert(vertices(0).props.get("metric4") == Some(JDouble(30.0)))
            }
            case Failure(e) => fail(s"Some error occured while trying to extract documents from the XML resource: ${e.getMessage}.")
          }
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the XML resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the XML resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the XML resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should correctly extract metrics from an XML data resource (resourceType=xmlAPI)"){
      val config = xmlApiResourceConfig.load(getClass.getResource("/xml/api/example-api.ok.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/xml/api/example-api.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          val xmlResource = XMLAPIResource(DataResourceConfig(c), DataResourceMapping(m))
          val documents = xmlResource.extractDocuments
          documents match {
            case Success(docs) => {
              val (vertices, edges) = (docs._1, docs._2)
              assert(!vertices.isEmpty)
              assert(edges.isEmpty)
              assert(vertices(0).isVertex)
              assert(vertices(0).props.get("name") == Some(JString("London")))
              assert(vertices(0).props.get("country") == Some(JString("GB")))
            }
            case Failure(e) => fail(s"Some error occured while trying to extract documents from the XML resource: ${e.getMessage}.")
          }
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the XML resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the XML resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the XML resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should fail when trying to extract metrics from an XML data resource (resourceType=xml) with invalid config or mapping files"){
      val config = xmlResourceConfig.load(getClass.getResource("/xml/file/example-file.ko.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/xml/file/example-file.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

    it("should fail when trying to extract metrics from an XML data resource (resourceType=xmlAPI) with invalid config or mapping files"){
      val config = xmlApiResourceConfig.load(getClass.getResource("/xml/api/example-api.ko.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/xml/api/example-api.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

  }

}
