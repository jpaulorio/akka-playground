package com.jplfds.perceptron

import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, Behavior}
import com.jplfds.perceptron.Neuron.NeuronInput

object Layer {
  trait LayerInput
  case class LayerDoubleInput(value: Double) extends LayerInput

  def apply(): Behavior[LayerInput] = layer(List.empty)

  private def layer(neurons: List[ActorRef[NeuronInput]]): Behavior[LayerInput] =
    Behaviors.receive { (context, message) =>
      message match {
        case LayerDoubleInput(input) => {
//          context.log.info("Hidden layer input: {}", input)
          Behaviors.same
        }
      }
    }
}
