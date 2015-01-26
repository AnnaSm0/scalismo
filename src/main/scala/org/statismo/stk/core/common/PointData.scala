package org.statismo.stk.core.common

import org.statismo.stk.core.geometry.{NDSpace, Dim, Point, Vector}
import scala.reflect.ClassTag
import spire.math.Numeric

/**
 * Defines a discrete set of values, where each associated to a point of the domain.
 */
trait PointData[D <: Dim, A] extends PartialFunction[Int, A] { self =>

  def domain: FiniteDiscreteDomain[D]

  def values: Iterator[A]
  def pointsWithValues = domain.points zip values
  def pointsWithIds = domain.points.zipWithIndex

  def foreach(f: A => A): Unit = values.foreach(f)

  def map[B](f: A => B): PointData[D, B] = new PointData[D, B] {
    override def domain = self.domain
    override def values = self.values.map(f)
    override def apply(i : Int) = f(self(i))
    override def isDefinedAt(i : Int) = self.isDefinedAt(i : Int)
  }

}

/**
 *
 */
class ScalarPointData[D <: Dim, A : Numeric](val domain : FiniteDiscreteDomain[D], val data : Array[A]) extends PointData[D, A] {

  /** map the function f over the values, but ensures that the result is scalar valued as well */
  def mapScalar[B: Numeric : ClassTag](f: A => B): ScalarPointData[D, B] = {
    new ScalarPointData(domain, data.map(f))
  }

  override def values = data.iterator
  override def apply(ptId : Int) = data(ptId)
  override def isDefinedAt(ptId : Int) = data.isDefinedAt(ptId)


  override def equals(other: Any): Boolean =
    other match {

      case that: ScalarPointData[D, A]  =>
        (that canEqual this) &&
          data.deep == that.data.deep &&
          domain == that.domain

      case _ => false
    }

  def canEqual(other: Any): Boolean =
    other.isInstanceOf[PointData[D, A]]

  override lazy val hashCode: Int = data.hashCode() + domain.hashCode()


}

class VectorPointData[D <: Dim, DO <: Dim]private (val domain : FiniteDiscreteDomain[D], val data : IndexedSeq[Vector[DO]]) extends PointData[D, Vector[DO]] {

  override def values = data.iterator
  override def apply(ptId : Int) = data(ptId)
  override def isDefinedAt(ptId : Int) = data.isDefinedAt(ptId)


  /** map the function f over the values, but ensures that the result is scalar valued as well */
  def mapVector(f: Vector[DO] => Vector[DO]): VectorPointData[D, DO] = new VectorPointData(domain, data.map(f))

}


object VectorPointData {

  def apply[D <: Dim, DO <: Dim](domain : FiniteDiscreteDomain[D], data : IndexedSeq[Vector[DO]]) = {
    new VectorPointData(domain, data)
  }
}