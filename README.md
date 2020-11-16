# Akka Playground

## Description

This repo contains a couple of programs that shows the basics of the Akka framework.

The AkkaBasics and ChatRoom programs are from the [Akka Tutorial](https://doc.akka.io/docs/akka/current/typed/actors.html).

The Perceptron project was written by me.

## Building

1. Install sbt
2. Run `sbt assembly`

### Running the Main Program

3. Run `java -jar ./target/scala-2.13/akkaBasics-assembly-0.1-SNAPSHOT.jar`

### Running the Perceptron Program

3. Run `java -cp ./target/scala-2.13/akkaBasics-assembly-0.1-SNAPSHOT.jar com.jplfds.perceptron.Main`

### Running the ChatRoom Program

3. Run `java -cp ./target/scala-2.13/akkaBasics-assembly-0.1-SNAPSHOT.jar com.jplfds.chat.Main`

## Perceptron

The perceptron example trains a perceptron to learn the NAND boolean operation.

After 6 iterations it learns to separate the input 1, 1, 1 from the others.

Below are the plots of the decision boundary after each training iteration:

Iteration 1:

 ![Iteration 1](./images/iteration-1.png)
 
Iteration 2:

 ![Iteration 2](./images/iteration-2.png)
 
Iteration 3:

 ![Iteration 3](./images/iteration-3.png)
 
Iteration 4:

 ![Iteration 4](./images/iteration-4.png)
 
Iteration 5:

 ![Iteration 5](./images/iteration-5.png)
 
Iteration 6:

 ![Iteration 6](./images/iteration-6.png)     