package com.jplfds.perceptron

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import com.jplfds.perceptron.Neuron.{QueryInput, TrainingInput}

import scala.runtime.RichInt

object Main {


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
    (1 to 10).foreach(_ => {
      (0 to 7).foreach(x => {
        val input = generateInput(x)
        val result = not(input.dropRight(1).reduce((a, b) => a & b)) // nand
        //        val result = (input.dropRight(1).reduce((a, b) => a & b)) // and
        //        val result = (input.dropRight(1).reduce((a, b) => a | b)) // or
        //        val result = not(input.dropRight(1).reduce((a, b) => a | b)) // nor
        neuron ! TrainingInput(input.map(_.toDouble), result)
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
    ActorSystem(Main(), "NeuralNet")
  }
}
