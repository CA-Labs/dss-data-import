package com.calabs.dss.dataimport

import org.json4s.JsonAST._

import scala.annotation.tailrec
import scala.collection.MapLike
import scala.collection.mutable.{Map => MutableMap}
import scala.util.{Failure, Success}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 13/1/15
 */

// Base trait which represents a database entity
sealed trait Element {
  def props: Map[String, JValue]
  def isVertex: Boolean
  def isEdge: Boolean
  def isDocument: Boolean
}

// Database entity in NoSQL document stores
sealed trait Document extends Element {
  override def isVertex: Boolean = false
  override def isEdge: Boolean = false
  override def isDocument: Boolean = true
}

// Database entity (vertex) in NoSQL graph databases
case class Vertex(props: Map[String, JValue]) extends Document {
  override def isVertex: Boolean = true
  override def isEdge: Boolean = false
}

// Database entity (edge) in NoSQL graph databases
case class Edge(props: Map[String, JValue]) extends Document {
  override def isVertex: Boolean = false
  override def isEdge: Boolean = true
}

private[calabs] object Parsing {

  type Chunk = List[String]

  object Tags {
    val PROPS_SEPARATOR = "\n"
    val KEY_VALUE_SEPARATOR = "::"
    val MAP_KEY_VALUE_SEPARATOR = "=>"
    val MAP_SEPARATOR = ","
    val SEARCHABLE_CRITERIA = "__"
    val FROM = SEARCHABLE_CRITERIA + "from"
    val TO = SEARCHABLE_CRITERIA + "to"
    val LABEL = "label"
  }

  /**
   * Tries to parse a line representing a map of keys and values.
   * For example: "a=>1,b=>2,c=>3".
   * @param line The input line to parse.
   * @return The map extracted.
   */
  def stringToMap(line: String) : Map[String, String] = {
    line.split(Tags.MAP_SEPARATOR).map(mapValue => {
      val keyValue = mapValue.split(Tags.MAP_KEY_VALUE_SEPARATOR)
      if (keyValue.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters in key/value $line")
      else (keyValue(0), keyValue(1))
    }).toMap
  }

  /**
   * Determines whether an element properties map indicates an update action needs to be carried out.
   * @param props The element properties (key/value pairs).
   * @return
   */
  def updateRequired(props: Map[String, JValue]) : Boolean = {
    props.filterKeys(key => !key.startsWith(Tags.FROM) && !key.startsWith(Tags.TO) && key.startsWith(Tags.SEARCHABLE_CRITERIA)).size > 0
  }

  /**
   * Extracts a document resource configuration, which is in turn a simple keys/values map.
   * @param lines The configuration input (raw format, [[List]] of [[String]]
   * @return A map of configuration keys and values.
   */
  def extractConfig(lines: List[String]) : Map[String, Any] = {
    def parseLine(line: String) : (String, String) = {
      val params = line.split(Tags.KEY_VALUE_SEPARATOR)
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    lines.map(line => parseLine(line)).toList.groupBy(_._1).map{ case(k,v) => (k, v.head._2) }
  }

  /**
   * Finds out whether a document is a vertex or an edge depending on its property keys.
   * If the properties map contains keys starting with [[Tags.FROM]] and [[Tags.TO]], this
   * document is considered to be an Edge. If not, it is considered a Vertex. For edges, [[Tags.FROM]]
   * and [[Tags.TO]] values must be searchable criteria in a [[Map]] fashion.
   * @param props The key/values map associated to this element.
   * @return The element extracted.
   */
  def extractDocument(props: Map[String, JValue]) : Document = {
    if (props.contains(Tags.FROM) && props.contains(Tags.TO)) Edge(props) else Vertex(props)
  }

  /**
   * Extracts documents mappings, which are in turn simple keys/values.
   * @param lines The mapping input (raw format, [[List]] of [[String]]).
   * @return A pair of vertices and edges mappings.
   */
  def extractMappings(lines: List[String]) : (List[Map[String, String]]) = {

    /**
     * Extracts a chunk of lines (it assumes every chunk of data is separated by a blank line).
     * @param lines The total lines to read.
     * @return A pair of chunk of lines read and lines left to be read.
     */
    def extractChunk(lines: List[String], acc: Chunk) : (Chunk, List[String]) = lines match {
      case head :: tail if(head.isEmpty) => (acc, tail)
      case head :: tail if(!head.isEmpty) => extractChunk(tail, head :: acc)
      case _ => (acc, List.empty)
    }

    /**
     * Extracts chunks of lines (it assumes every chunk of data is separated by a blank line).
     * @param lines The input lines from where chunks should be extracted.
     * @param acc The current number of chunks extracted.
     * @return A list of extracted chunks.
     */
    @tailrec
    def extractChunks(lines: List[String], acc: List[Chunk]) : List[Chunk] = {
      if (lines.isEmpty) acc
      else {
        val nextChunk = extractChunk(lines, List.empty)
        extractChunks(nextChunk._2, nextChunk._1 :: acc)
      }
    }

    val mappings = extractChunks(lines, List.empty).map(props => {
        // Create the props map from key/values
        val propsMap = props.map(prop => {
          val keyValue = prop.split(Tags.KEY_VALUE_SEPARATOR)
          if (keyValue.length != 2) throw new IllegalArgumentException(s"Wrong key/value in mapping $prop.")
          else (keyValue(0), keyValue(1))
        }).toMap
        propsMap
    })

    mappings

  }

  /**
   * Converts weakly-typed document mappings/values to json AST ones.
   * @param props The document properties.
   * @return
   */
  def checkProps(props: Map[String, Any]) : Map[String, JValue] = {
    def checkProp(prop: Any) : JValue = prop match {
      case string: String => JString(string)
      case int: Int => JInt(int)
      case double: Double => JDouble(double)
      case boolean: Boolean => JBool(boolean)
      case seq: Seq[Any] => JArray(seq.map(element => checkProp(element)).toList)
      case map: MapLike[String, Any, Map[String,Any]] => JObject(map.mapValues(element => checkProp(element)).toList)
      case set: Set[Any] => JArray(set.toList.map(element => checkProp(element)).toList)
      case _ => throw new IllegalArgumentException(s"Invalid property value $prop")
    }
    props.mapValues(checkProp)
  }

  /**
   * Given a document mapping, determines whether it contains valid searchable criteria or not
   * @param mapping A document mapping.
   * @return
   */
  def validSearchableCriteria(mapping: Map[String, JValue]) : Boolean = {

    /**
     * Given a searchable criteria object, determines whether their values types are supported or not.
     * @param searchableCriteria A searchable criteria object.
     * @return
     */
    def validCriteria(searchableCriteria: JObject) : Boolean = {
      searchableCriteria.obj.forall(criteria => criteria._2 match {
        case s: JString => true
        case i: JInt => true
        case d: JDouble => true
        case b: JBool => true
        case _ => false
      })
    }

    (mapping.contains(Tags.FROM), mapping.contains(Tags.TO)) match {
      case (true, true) => {
        (mapping.get(Tags.FROM).get, mapping.get(Tags.TO).get) match {
          case (from: JObject, to: JObject) => if (validCriteria(from) && validCriteria(to)) true else false
          case (from: JObject, _) => throw new IllegalArgumentException(s"${Tags.TO} property must contain valid values (only String, Int, Double, Bool are supported)")
          case (_, to: JObject) => throw new IllegalArgumentException(s"${Tags.FROM} property must contain valid values (only String, Int, Double, Bool are supported)")
          case _ => throw new IllegalArgumentException(s"${Tags.FROM} and ${Tags.TO} properties must contain valid values (only String, Int, Double, Bool are supported)")
        }
      }
      case (true, false) => throw new IllegalArgumentException(s"${Tags.FROM} is a reserved key (you can only use it if ${Tags.TO} is also present")
      case (false, true) => throw new IllegalArgumentException(s"${Tags.TO} is a reserved key (you can only use it if ${Tags.FROM} is also present")
      case (false, false) => true
    }

  }

}
