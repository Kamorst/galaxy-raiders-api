/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package galaxyraiders

import kotlin.math.truncate
import kotlin.math.roundToInt
import galaxyraiders.impactVector
import galaxyraiders.contactVector

class App {
  val greeting: String
    get() {
      return "Hello World!"
    }
}

data class Point(val x: Double, val y: Double) {
  operator fun plus(p: Point): Point {
    return Point(x = this.x + p.x, y = this.y + p.y)
  }

  operator fun plus(v: Vector): Point {
    return Point(x = this.x + v.dx, y = this.y + v.dy)
  }

  override fun toString(): String {
    return String.format("Point(x=%.1f, y=%.1f)", this.x, this.y)
  }
}

data class Vector(val dx: Double, val dy: Double) {
  val magnitude: Double
    get() = Math.sqrt(this.dot(this))

  val radiant: Double
    get() = Math.atan2(this.dy, this.dx)

  val degrees: Double
    get() = Math.toDegrees(this.radiant)

  val unit: Vector
    get() = Vector(
      dx = this.dx / this.magnitude,
      dy = this.dy / this.magnitude
    )

  val normal: Vector
    get() = Vector(dx = this.dy, dy = -this.dx).unit

  operator fun plus(v: Vector): Vector {
    return Vector(dx = this.dx + v.dx, dy = this.dy + v.dy)
  }

  operator fun times(scalar: Double): Vector {
    return Vector(dx = this.dx * scalar, dy = this.dy * scalar)
  }

  operator fun unaryMinus(): Vector {
    return this * -1.0
  }

  operator fun minus(v: Vector): Vector {
    return this + (-v)
  }

  operator fun times(v: Vector): Double {
    return this.dot(v)
  }

  fun dot(v: Vector): Double {
    return this.dx * v.dx + this.dy * v.dy
  }

  fun scalarProject(target: Vector): Double {
    return this.dot(target) / target.magnitude
  }

  fun vectorProject(target: Vector): Vector {
    return target.unit * this.scalarProject(target)
  }

  override fun toString(): String {
    return String.format("Vector(x=%.1f, y=%.1f)", this.dx, this.dy)
  }
}

operator fun Double.times(v: Vector): Vector {
  return Vector(dx = this * v.dx, dy = this * v.dy)
}

fun distance(p1: Point, p2: Point): Double {
  val dx = Math.abs(p1.x - p2.x)
  val dy = Math.abs(p1.y - p2.y)
  return Math.sqrt(dx * dx + dy * dy)
}

fun impactVector(p1: Point, p2: Point): Vector {
  return Vector(
    dx = Math.abs(p1.x - p2.x),
    dy = Math.abs(p1.y - p2.y)
  )
}

fun impactDirection(p1: Point, p2: Point): Vector {
  return impactVector(p1, p2).unit
}

fun contactVector(p1: Point, p2: Point): Vector {
  return impactVector(p1, p2).normal
}

fun contactDirection(p1: Point, p2: Point): Vector {
  return contactVector(p1, p2).unit
}

class SpaceObject(
  val symbol: Char,
  val initialPosition: Point,
  val radius: Double,
  var angle: Double,
  var speed: Double,
  val mass: Double
) {
  var center: Point = initialPosition
    private set

  val radiant: Double
    get() = Math.toRadians(angle)

  val momentum: Double
    get() = this.speed * this.mass

  val velocity: Vector
    get() = Vector(
      dx = (this.speed * Math.cos(this.radiant)),
      dy = (this.speed * Math.sin(this.radiant))
    )

  fun shift(delta: Vector) {
    val finalVelocity = this.velocity + delta
    this.speed = finalVelocity.magnitude
    this.angle = finalVelocity.degrees
  }

  fun move() {
    this.center += this.velocity
  }

  fun inArea(p: Point): Boolean {
    return distance(this.center, p) < this.radius;
  }

  override fun toString(): String {
    return "ship at ${this.center}"
  }
}

fun distance(so1: SpaceObject, so2: SpaceObject): Double {
  return distance(so1.center, so2.center) - (so1.radius + so2.radius)
}

fun hasCollided(so1: SpaceObject, so2: SpaceObject): Boolean {
  return distance(so1, so2) <= 0
}

data class Map(val height: Int, val width: Int) {
  fun print(resolution: Int, so1: SpaceObject, so2: SpaceObject) {
    val pixelHeight = this.height * resolution
    val pixelWidth = this.width * resolution

    for (i in 0..pixelHeight) {
      for (j in 0..pixelWidth) {
        val currentPoint = Point(
          x = j.toDouble() / resolution,
          y = (pixelHeight-i).toDouble() / resolution
        )

        val inS1 = so1.inArea(currentPoint)
        val inS2 = so2.inArea(currentPoint)

        if (inS1 && inS2) { print("#") }
        else if (inS1) { print(so1.symbol) }
        else if (inS2) { print(so2.symbol) }
        else { print(" ") }
      }
      println()
    }
  }
}

fun main() {
  val map = Map(height = 6, width = 18)

  val c1 = Point(x = 1.0, y = 1.0)
  val c2 = Point(x = 9.0, y = 1.0)
  val d = distance(c1, c2)

  println("Distance between $c1 and $c2 is $d")

  val so1 = SpaceObject(
    symbol = '1',
    initialPosition = c1,
    radius = 1.0,
    angle = 0.0,
    speed = 1.0,
    mass = 1.0
  )

  val so2 = SpaceObject(
    symbol = '2',
    initialPosition = c2,
    radius = 1.0,
    angle = 180.0,
    speed = 1.0,
    mass = 1.0
  )

  for (k in 1..6) {
    println("Iteration $k")

    map.print(resolution=10, so1 = so1, so2 = so2)

    val ssd = distance(so1, so2)
    val collided = hasCollided(so1, so2)

    println("Distance between $so1 and $so2 is $ssd")
    println("Ships have collided? $collided")

    val Cr = 1.0;

    if (collided) {
      println("Velocities v1:${so1.velocity} v2:${so2.velocity}")

      val impactVector = impactVector(so1.center, so2.center)
      println("Impact Vector $impactVector")

      val impactDirection = impactDirection(so1.center, so2.center)
      println("Impact Direction $impactDirection")

      val is1i = so1.velocity.scalarProject(impactDirection)
      val is2i = so2.velocity.scalarProject(impactDirection)
      println("Initial impact speeds is1i:$is1i is2i:$is2i")

      val m1 = so1.mass
      val m2 = so2.mass

      val is1f = (Cr*m2*(is2i-is1i) + (m1*is1i) + (m2*is2i)) / (m1+m2)
      val is2f = (Cr*m1*(is1i-is2i) + (m1*is1i) + (m2*is2i)) / (m1+m2)
      println("Final impact speeds is1f:$is1f is2f:$is2f")

      val is1d = is1f-is1i
      val is2d = is2f-is2i
      println("Delta impact speeds is1d:$is1d is2d:$is2d")

      println("===================")

      val iv1i = so1.velocity.vectorProject(impactDirection)
      val iv2i = so2.velocity.vectorProject(impactDirection)
      println("initial velocity iv1i:$iv1i iv2i:$iv2i")

      var impulse = ((m1*m2)/(m1+m2)) * (1+Cr) * (iv2i-iv1i) * impactDirection
      println("Impulse i1:${impulse} i2:${-impulse}")

      // arrow_n is the impact vector
      val dv1 = (impulse/m1) * impactDirection
      val dv2 = (-impulse/m2) * impactDirection
      println("Delta velocity iv1d:$dv1 iv2d:$dv2")

      val iv1f = iv1i + dv1
      val iv2f = iv2i + dv2
      println("final velocity iv1f:$iv1f iv2f:$iv2f")

      println("===================")

      so1.shift(dv1)
      so2.shift(dv2)
    }

    so1.move()
    so2.move()
  }
}
