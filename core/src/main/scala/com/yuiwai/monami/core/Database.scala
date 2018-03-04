package com.yuiwai.monami.core

import com.yuiwai.monami.core.GraphDatabase.{Edge, Node, NodeId}

trait Database[K, V] {
  def find(id: K): Option[V]
}
trait DatabaseBuilder[DB <: Database[_, _]] {
  def build: DB
}

trait IndexedDatabase[K, V] extends Database[K, V] {
  val data: Map[K, V]
  val indexes: Map[Symbol, Map[Any, Set[K]]]
  override def find(id: K): Option[V] = data.get(id)
  def findByIndex(indexName: Symbol, value: Any): List[V] = {
    indexes.get(indexName)
      .flatMap(_.get(value)
        .map(_.foldLeft(List.empty[V]) {
          case (rows, id) => data(id) :: rows
        })
      )
      .getOrElse(Nil)
  }
}

trait IndexedDatabaseBuilder[K, V] extends DatabaseBuilder[IndexedDatabase[K, V]] {
  type IDX = Map[Symbol, Map[Any, Set[K]]]
  val data: Iterable[(K, V)]
  def keys: Seq[Symbol]
  def indexKey(row: V, key: Symbol): Any
  def build: IndexedDatabase[K, V] = {
    val r = data.foldLeft((Map.empty[K, V], Map.empty[Symbol, Map[Any, Set[K]]])) {
      case ((d, i), (k, v)) => (d.updated(k, v), updateIndexesForRow(k, v, i))
    }
    new IndexedDatabase[K, V] {
      override val data: Map[K, V] = r._1
      override val indexes: Map[Symbol, Map[Any, Set[K]]] = r._2
    }
  }
  private def updateIndexesForRow(id: K, row: V, indexes: IDX): IDX = keys.foldLeft(indexes) {
    case (i, k) =>
      val iKey = indexKey(row, k)
      i.get(k) match {
        case Some(idx) => i.updated(k, idx.updated(iKey, idx.getOrElse(iKey, Set.empty[K]) + id))
        case None => i.updated(k, Map(iKey -> Set(id)))
      }
  }
}

trait GraphDatabase[K, V] extends Database[NodeId[K], Node[K, V]] {
  import GraphDatabase._
  val nodes: Map[NodeId[K], Node[K, V]]
  val edges: Set[Edge[K]]
  override def find(id: NodeId[K]): Option[Node[K, V]] = nodes.get(id)
}
object GraphDatabase {
  case class NodeId[T](id: T, nodeType: Symbol = 'default)
  case class Node[K, V](id: NodeId[K], payload: V)
  case class Edge[T](from: NodeId[T], to: NodeId[T])
}
trait GraphDatabaseBuilder[K, V] extends DatabaseBuilder[GraphDatabase[K, V]] {
  private var ns = Map.empty[NodeId[K], Node[K, V]]
  private var es = Set.empty[Edge[K]]
  def addNode(node: Node[K, V]): GraphDatabaseBuilder[K, V] = {
    ns = ns.updated(node.id, node)
    this
  }
  def addEdge(edge: Edge[K]): GraphDatabaseBuilder[K, V] = {
    es = es + edge
    this
  }
  override def build: GraphDatabase[K, V] = new GraphDatabase[K, V] {
    override val nodes: Map[NodeId[K], Node[K, V]] = ns
    override val edges: Set[Edge[K]] = es
  }
}
