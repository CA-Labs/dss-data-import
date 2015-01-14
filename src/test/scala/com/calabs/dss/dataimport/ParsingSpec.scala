package com.calabs.dss.dataimport

import com.calabs.dss.dataimport.Parsing.Tags
import org.scalatest.FunSpec

import scala.io.Source

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 13/1/15
 */

class ParsingSpec extends FunSpec {

  val mappingKoToFrom = Source.fromFile(getClass.getResource("/parsing/vertices-edges-from-to.ko.map").getPath).getLines.toList
  val mappingOkToFrom = Source.fromFile(getClass.getResource("/parsing/vertices-edges-from-to.ok.map").getPath).getLines.toList
  val mappingKoBadKeyValue = Source.fromFile(getClass.getResource("/parsing/vertices-edges-key-value.ko.map").getPath).getLines.toList
  val mappingOkNoEdges = Source.fromFile(getClass.getResource("/parsing/vertices-edges-no-edges.ok.map").getPath).getLines.toList

  describe("Parsing object"){

    it(s"should detect ${Tags.FROM} and ${Tags.TO} keys must contain searchable criteria when dealing with edges"){
      intercept[IllegalArgumentException]{
        val mappings = Parsing.extractMappings(mappingKoToFrom)
        val documents = mappings.map(mapping => Parsing.extractDocument(mapping))
      }
    }

    it(s"should read correctly ${Tags.FROM} and ${Tags.TO} searchable criteria when dealing with edges"){
      val mappings = Parsing.extractMappings(mappingOkToFrom)
      val documents = mappings.map(mapping => Parsing.extractDocument(mapping))
      assert(true)
    }

    it(s"should detect bad key/values"){
      intercept[IllegalArgumentException]{
        val mappings = Parsing.extractMappings(mappingKoBadKeyValue)
      }
    }

    it("should be able to extract chunk of vertices and edges"){
      val mappings = Parsing.extractMappings(mappingOkToFrom)
      val documents = mappings.map(mapping => Parsing.extractDocument(mapping))
      val verticesEdges = (documents.filter(_.isVertex), documents.filter(_.isEdge))
      assert(verticesEdges._1.length == 2 && verticesEdges._2.length == 2)
    }

    it("should be able to extract only vertices (documents) when no edges are present"){
      val mappings = Parsing.extractMappings(mappingOkNoEdges)
      val documents = mappings.map(mapping => Parsing.extractDocument(mapping))
      val verticesEdges = (documents.filter(_.isVertex), documents.filter(_.isEdge))
      assert(verticesEdges._1.length == 4 && verticesEdges._2.length == 0)
    }

  }

}
