import io.Source
import java.nio.charset.Charset

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 20/11/14
 */

case class DataResourceMapping(mapping: Map[String, String])
sealed trait DataResource {
  def mapping: DataResourceMapping
}
trait DataResourceExtractor {
  def extractMetrics: Map[String, Double]
}

case class JSONResource(mapping: DataResourceMapping, source: String) extends DataResource with DataResourceExtractor {
  override def extractMetrics: Map[String, Double] = ???
}

case class XMLResource(mapping: DataResourceMapping, source: String) extends DataResource with DataResourceExtractor {
  override def extractMetrics: Map[String, Double] = ???
}

