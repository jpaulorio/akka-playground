package com.jplfds.perceptron

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import com.jplfds.perceptron.Layer.{LayerDoubleInput, LayerInput}
import org.apache.commons.math3.analysis.function.Sigmoid
import org.slf4j.LoggerFactory

class Neuron {}

object Neuron {

  trait NeuronInput

  final case class TrainingInput(value: List[Double], expectedOutput: Double) extends NeuronInput
  final case class QueryInput(value: List[Double]) extends NeuronInput

  val logger = LoggerFactory.getLogger(classOf[Neuron])

  def apply(initialWeights: List[Double], nextLayer: ActorRef[LayerInput]): Behavior[NeuronInput] = neuron(initialWeights, nextLayer)

  private def neuron(weights: List[Double], nextLayer: ActorRef[LayerInput]): Behavior[NeuronInput] = {
    Behaviors.receive { (context, message) =>
      message match {
        case TrainingInput(inputs, expectedOutput) =>
          val computedOutput = computeOutput(inputs, weights)
          nextLayer ! LayerDoubleInput(computedOutput)
          neuron(updateWeights(inputs, weights, expectedOutput, computedOutput), nextLayer)
        case QueryInput(inputs) =>
          val computedOutput = computeOutput(inputs, weights)
          context.log.info2("Query output: {} for inputs: {}", computedOutput, inputs.dropRight(1))
          nextLayer ! LayerDoubleInput(computedOutput)
          Behaviors.same
      }
    }
  }

  private def updateWeights(inputs: List[Double], weights: List[Double], expectedOutput: Double, computedOutput: Double): List[Double] = {
    val learningRate = 0.1;
//    weights.foreach(logger.info("Updating weight: {}", _))
    inputs.zip(weights).map(x => x._2  + (learningRate * (expectedOutput - computedOutput) * x._1))
  }

  private def computeOutput(inputs: List[Double], weights: List[Double]): Double = {
//    inputs.foreach(logger.info("Processing input: {}", _))
    val result = inputs.zip(weights).map(x => x._1 * x._2).sum
    val resultAfterSigmoid = new Sigmoid(0, 1).value(result)
    val output = if (resultAfterSigmoid >= 0.5) 1 else 0
//    logger.info2("Result begore sigmoid {}. Result after sigmoid: {}", output, resultAfterSigmoid)
    output
  }
}
