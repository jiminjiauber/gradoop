/*
 * Copyright © 2014 - 2018 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradoop.flink.model.impl.operators.sampling.statistics;

/**
 * Collected constants for sampling evaluation
 */
public class SamplingEvaluationConstants {

  /**
   * Key for density property generated by {@link GraphDensity}
   */
  public static final String PROPERTY_KEY_DENSITY = "density";

  /**
   * Filename for the saved density value of the graph
   */
  public static final String FILE_DENSITY = "graph_density";

  /**
   * Key for the summed up degrees property generated by {@link AverageDegree},
   * {@link AverageIncomingDegree} and {@link AverageOutgoingDegree}
   */
  public static final String PROPERTY_KEY_SUM_DEGREES = "sum_degrees";

  /**
   * Key for average degree property generated by {@link AverageDegree}
   */
  public static final String PROPERTY_KEY_AVERAGE_DEGREE = "average_degree";

  /**
   * Filename for the saved average degree value of the graph
   */
  public static final String FILE_AVERAGE_DEGREE = "average_degree";

  /**
   * Key for average incoming degree property generated by {@link AverageIncomingDegree}
   */
  public static final String PROPERTY_KEY_AVERAGE_INCOMING_DEGREE = "average_incoming_degree";

  /**
   * Filename for the saved average incoming degree value of the graph
   */
  public static final String FILE_AVERAGE_INCOMING_DEGREE = "average_incoming_degree";

  /**
   * Key for average outgoing degree property generated by {@link AverageOutgoingDegree}
   */
  public static final String PROPERTY_KEY_AVERAGE_OUTGOING_DEGREE = "average_outgoing_degree";

  /**
   * Filename for the saved average outgoing degree value of the graph
   */
  public static final String FILE_AVERAGE_OUTGOING_DEGREE = "average_outgoing_degree";

  /**
   * private Constructor
   */
  private SamplingEvaluationConstants() { }
}
