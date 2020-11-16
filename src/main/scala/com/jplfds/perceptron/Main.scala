package com.jplfds.perceptron

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.util.Timeout
import com.jplfds.perceptron.Neuron.{QueryInput, TrainingInput}
import org.jzy3d.chart.AWTChart
import org.jzy3d.colors.{Color, ColorMapper}
import org.jzy3d.colors.colormaps.{ColorMapRainbow, IColorMap}
import org.jzy3d.maths.Coord3d
import org.jzy3d.plot3d.builder.{Builder, Mapper}
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.rendering.canvas.Quality
import org.jzy3d.plot3d.primitives.Scatter
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.runtime.RichInt

case class Reply(msg: List[Double])

object Main {

  implicit var system: ActorSystem[_] = null
  val logger = LoggerFactory.getLogger("com.jplfds.perceptron.Main")

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val initialWeights: List[Double] = List(0, 0, 0, 0)
      val hiddenLayer = context.spawn(Layer(), "hidden-layer")
      val neuron = context.spawn(Neuron(initialWeights, hiddenLayer), "a-neuron")
      context.watch(hiddenLayer)

      trainPerceptron(neuron)

      queryPerceptron(neuron)

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

  private def trainPerceptron(neuron: ActorRef[Neuron.NeuronInput]): Unit = {
    implicit val context: ExecutionContextExecutor = system.executionContext
    import scala.language.postfixOps

    implicit val timeout: Timeout = Timeout(50 seconds)
    (1 to 6).foreach(x => {
      (0 to 7).foreach(y => {
        val input = generateInput(y)
        val result = not(input.dropRight(1).reduce((a, b) => a & b)) // nand
        //        val result = (input.dropRight(1).reduce((a, b) => a & b)) // and
        //        val result = (input.dropRight(1).reduce((a, b) => a | b)) // or
        //        val result = not(input.dropRight(1).reduce((a, b) => a | b)) // nor
        val future: Future[List[Double]] = neuron ? (replyTo => TrainingInput(input.map(_.toDouble), result, replyTo))
        future.onComplete({
          case scala.util.Success(w) => {
            if (y == 7)
              plotDecisionBoundary(x, y, w)
//            println("Enter any key to continue:")
//            StdIn.readLine()
          }
          case scala.util.Failure(e) => logger.error("Message failed: {}", e)
        })
      })
    })
  }

  private def plotDecisionBoundary(iteration: Int, inputNo: Int, weights: List[Double]): Unit = {
    logger.info("Plotting decision boundary for iteration {} input {}: {}", iteration, inputNo, weights)

    val range = new org.jzy3d.maths.Range(-2, 2)
    val steps: Int = 20

    val a :: b :: c :: d :: rest = weights
    val mapper = new Mapper {
      def f(x: Double, y: Double): Double = - ((a * x + b * y) + d) / c
    }
    val surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps), mapper)
    val rainbow: IColorMap = new ColorMapRainbow
    val bounds = surface.getBounds.getXRange
    val colorMapper = new ColorMapper(rainbow, bounds)
    surface.setColorMapper(colorMapper)
    surface.setFaceDisplayed(true)
    surface.setWireframeDisplayed(false)
    surface.setWireframeColor(Color.BLACK)

    val points = (0 to 7).map(x => {
      val input = generateInput(x).map(x => if (x.toFloat == 1.0f) 1.0f else -1.0f).toArray
      new Coord3d(input.dropRight(1))
    }).toArray
    val colors = points.indices.map(_ => new Color(0, 0, 1.0f, 1.0f)).toArray
    val scatter = new Scatter(points, colors)
    scatter.width = 20.0f;

    val chart = new AWTChart(Quality.Advanced)
    chart.setViewPoint(new Coord3d(-0.5,-0.5,0))
    chart.addKeyboardCameraController()
    chart.addMouseCameraController()
    chart.getScene.add(surface, true)
    chart.getScene.add(scatter, true)
    chart.open(s"Iteration: $iteration", 1000, 1000)
  }

  private def queryPerceptron(neuron: ActorRef[Neuron.NeuronInput]): Unit = {
    (0 to 7).foreach(x => neuron ! QueryInput(generateInput(x).map(_.toDouble)))
  }

  def not(x: Int): Int = {
    Math.abs(1 - x)
  }

  def generateInput(x: Int): List[Int] = {
    val temp = decimalToBinary(x) :+ 1
    val padding: Int = 4 - temp.length
    List.fill(padding)(0) ++ temp
  }

  def decimalToBinary(x: Int): List[Int] = {
    new RichInt(x).toBinaryString.split("").map(_.toInt).toList
  }

  def main(args: Array[String]): Unit = {
    system = ActorSystem(Main(), "NeuralNet")
  }
}
