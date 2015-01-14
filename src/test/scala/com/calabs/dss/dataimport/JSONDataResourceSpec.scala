package com.calabs.dss.dataimport

import java.util

import org.scalatest.FunSpec
import scala.collection.mutable.{Map => MutableMap}
import scala.io.Source
import scala.util.{Failure, Success}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class JSONDataResourceSpec extends FunSpec {

  import Config._
  val resourceMapper = DataResourceMapper()

  describe("A JSON Data Resource extractor"){

    it("should correctly extract metrics from a JSON data resource (resourceType=json)"){
      val config = jsonResourceConfig.load(getClass.getResource("/json/file/example-file.ok.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/json/file/example-file.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          // How to test files that are loaded from a path property read from a file? This will be different between machines!
          // Overwrite data source path by now with one existing relative to resources folder
          val newConfig = DataResourceConfig(getClass.getResource("/json/file/example-file.json").getPath, c.productElement(1))
          val jsonResource = JSONResource(newConfig, DataResourceMapping(m))
          val documents = jsonResource.extractDocuments
          documents match {
            case Success(docs) => {
              val (vertices, edges) = (docs._1, docs._2)
              assert(vertices(0).isVertex)
              assert(vertices(0).props.get("metric1") == Some("bar"))
              assert(vertices(0).props.get("metric2") == Some(2))
              assert(vertices(0).props.get("metric3") == Some(List(1,2,3)))
            }
            case Failure(e) => fail(s"Some error occured while trying to extract documents from the JSON resource: ${e.getMessage}.")
          }
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the JSON resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the JSON resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the JSON resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should correctly extract metrics from a JSON data resource (resourceType=jsonAPI)"){
      val config = jsonApiResourceConfig.load(getClass.getResource("/json/api/example-api.ok.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/json/api/example-api.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          val jsonResource = JSONAPIResource(DataResourceConfig(c), DataResourceMapping(m))
          val documents = jsonResource.extractDocuments
          documents match {
            case Success(docs) => {
              val (vertices, edges) = (docs._1, docs._2)
              assert(vertices(0).isVertex)
              assert(vertices(0).props.get("login") == Some("jarandaf"))
              assert(vertices(0).props.get("url") == Some("https://api.github.com/users/jarandaf"))
              assert(vertices(0).props.get("email") == Some("jordi.aranda@bsc.es"))
            }
            case Failure(e) => fail(s"Some error occurred while trying to extract documents from the JSON resource: ${e.getMessage}.")
          }
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the JSON resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the JSON resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the JSON resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should fail when trying to extract metrics from a JSON data resource (resourceType=json) with invalid config or mapping files"){
      val config = jsonResourceConfig.load(getClass.getResource("/json/file/example-file.ko.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/json/file/example-file.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

    it("should fail when trying to extract metrics from a JSON data resource (resourceType=jsonAPI) with invalid config or mapping files"){
      val config = jsonApiResourceConfig.load(getClass.getResource("/json/api/example-api.ko.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/json/api/example-api.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

  }

}
