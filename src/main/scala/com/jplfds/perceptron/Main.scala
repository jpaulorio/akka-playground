package com.jplfds.perceptron

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.util.Timeout
import com.jplfds.perceptron.Neuron.{QueryInput, TrainingInput}
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

    implicit val timeout: Timeout = Timeout(5 seconds)
    (1 to 6).foreach(x => {
      (0 to 7).foreach(y => {
        val input = generateInput(y)
        val result = not(input.dropRight(1).reduce((a, b) => a & b)) // nand
        //        val result = (input.dropRight(1).reduce((a, b) => a & b)) // and
        //        val result = (input.dropRight(1).reduce((a, b) => a | b)) // or
        //        val result = not(input.dropRight(1).reduce((a, b) => a | b)) // nor
        val future: Future[List[Double]] = neuron ? (replyTo => TrainingInput(input.map(_.toDouble), result, replyTo))
        future.onComplete({
          case scala.util.Success(w) => logger.info("Weights in iteration {} sample {}: {}", x, y, w)
          case scala.util.Failure(e) => logger.error("Message failed: {}", e)
        })
      })
    })
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
