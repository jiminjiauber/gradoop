package org.gradoop.algorithms;

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: algorithm description
 */
public class LabelPropagationComputation extends
  BasicComputation<IntWritable, IntWritable, NullWritable, IntWritable> {
  /**
   * Returns the current new value. This value is based on all incoming
   * messages. Depending on the number of messages sent to the vertex, the
   * method returns:
   * <p/>
   * 0 messages:   The current value
   * <p/>
   * 1 message:    The minimum of the message and the current vertex value
   * <p/>
   * >1 messages:  The most frequent of all message values
   *
   * @param vertex   The current vertex
   * @param messages All incoming messages
   * @return the new Value the vertex will become
   */
  private int getNewValue(Vertex<IntWritable, IntWritable, NullWritable> vertex,
    Iterable<IntWritable> messages) {
    int newValue;
    //TODO: create allMessages more efficient
    //List<IntWritable> allMessages = Lists.newArrayList(messages);
    List<Integer> allMessages = new ArrayList<>();
    for (IntWritable message : messages) {
      allMessages.add(message.get());
    }
    if (allMessages.isEmpty()) {
      // 1. if no messages are received
      newValue = vertex.getValue().get();
    } else if (allMessages.size() == 1) {
      // 2. if just one message are received
      newValue = Math.min(vertex.getValue().get(), allMessages.get(0));
    } else {
      // 3. if multiple messages are received
      newValue = getMostFrequent(vertex, allMessages);
    }
    return newValue;
  }

  /**
   * Returns the most frequent value based on all received messages.
   *
   * @param vertex      The current vertex
   * @param allMessages All messages the current vertex has received
   * @return the maximal frequent number in all received messages
   */
  private int getMostFrequent(
    Vertex<IntWritable, IntWritable, NullWritable> vertex,
    List<Integer> allMessages) {
    Collections.sort(allMessages);
    int newValue;
    int currentCounter = 1;
    int currentValue = allMessages.get(0);
    int maxCounter = 1;
    int maxValue = 1;
    for (int i = 1; i < allMessages.size(); i++) {
      if (currentValue == allMessages.get(i)) {
        currentCounter++;
        if (maxCounter < currentCounter) {
          maxCounter = currentCounter;
          maxValue = currentValue;
        }
      } else {
        currentCounter = 1;
        currentValue = allMessages.get(i);
      }
    }
    // if the frequent of all received messages are just one
    if (maxCounter == 1) {
      // to avoid an oscillating state of the calculation we will just use
      // the smaller value
      newValue = Math.min(vertex.getValue().get(), allMessages.get(0));
    } else {
      newValue = maxValue;
    }
    return newValue;
  }

  /**
   * The actual LabelPropagation Computation
   *
   * @param vertex   Vertex
   * @param messages Messages that were sent to this vertex in the previous
   *                 superstep.
   * @throws IOException
   */
  @Override
  public void compute(Vertex<IntWritable, IntWritable, NullWritable> vertex,
    Iterable<IntWritable> messages) throws IOException {
    if (getSuperstep() == 0) {
      sendMessageToAllEdges(vertex, vertex.getId());
    } else {
      int currentMinValue = vertex.getValue().get();
      int newValue = getNewValue(vertex, messages);
      boolean changed = currentMinValue != newValue;
      if (changed) {
        vertex.setValue(new IntWritable(newValue));
        sendMessageToAllEdges(vertex, vertex.getValue());
      }
    }
    vertex.voteToHalt();
  }
}