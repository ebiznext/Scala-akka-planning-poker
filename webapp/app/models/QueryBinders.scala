package models

import play.api.mvc.{ JavascriptLitteral, QueryStringBindable }

object QueryBinders {

  /**
   * QueryString binder for List
   */
  implicit def bindableList[T: QueryStringBindable] = new QueryStringBindable[List[T]] {
    def bind(key: String, params: Map[String, Seq[String]]) = Some(Right(bindList[T](key, params)))
    def unbind(key: String, values: List[T]) = unbindList(key, values)
  }

  private def bindList[T: QueryStringBindable](key: String, params: Map[String, Seq[String]]): List[T] = {
    for {
      values <- params.get(key).toList
      rawValue <- values
      bound <- implicitly[QueryStringBindable[T]].bind(key, Map(key -> Seq(rawValue)))
      value <- bound.right.toOption
    } yield value
  }

  private def unbindList[T: QueryStringBindable](key: String, values: Iterable[T]): String = {
    (for (value <- values) yield {
      implicitly[QueryStringBindable[T]].unbind(key, value)
    }).mkString("&")
  }
}