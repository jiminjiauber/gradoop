package org.gradoop.model.impl.operators.transformation;

import com.google.common.collect.Lists;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.model.GradoopFlinkTestBase;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.api.functions.TransformationFunction;
import org.gradoop.model.impl.LogicalGraph;
import org.gradoop.model.impl.functions.epgm.Id;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.pojo.EdgePojo;
import org.gradoop.model.impl.pojo.GraphHeadPojo;
import org.gradoop.model.impl.pojo.VertexPojo;
import org.gradoop.util.FlinkAsciiGraphLoader;
import org.junit.Test;

import java.util.List;

import static org.gradoop.GradoopTestUtils.validateIdEquality;

public class TransformationTest extends GradoopFlinkTestBase {

  protected static final String TEST_GRAPH = "" +
    "g0:A  { a = 1 } [(:A { a = 1, b = 2 })-[:a { a = 1, b = 2 }]->(:B { c = 2 })]" +
    "g1:B  { a = 2 } [(:A { a = 2, b = 2 })-[:a { a = 2, b = 2 }]->(:B { c = 3 })]" +
    // full graph transformation
    "g01:A { a = 2 } [(:A { a = 2, b = 1 })-->(:B { d = 2 })]" +
    "g11:B { a = 3 } [(:A { a = 3, b = 1 })-->(:B { d = 3 })]" +
    // graph head only transformation
    "g02:A { a = 2 } [(:A { a = 1, b = 2 })-[:a { a = 1, b = 2 }]->(:B { c = 2 })]" +
    "g12:B { a = 3 } [(:A { a = 2, b = 2 })-[:a { a = 2, b = 2 }]->(:B { c = 3 })]" +
    // vertex only transformation
    "g03:A { a = 1 } [(:A { a = 2, b = 1 })-[:a { a = 1, b = 2 }]->(:B { d = 2 })]" +
    "g13:B { a = 2 } [(:A { a = 3, b = 1 })-[:a { a = 2, b = 2 }]->(:B { d = 3 })]" +
    // edge only transformation
    "g04:A { a = 1 } [(:A { a = 1, b = 2 })-->(:B { c = 2 })]" +
    "g14:B { a = 2 } [(:A { a = 2, b = 2 })-->(:B { c = 3 })]";

  @Test(expected = IllegalArgumentException.class)
  public void testMissingFunctions() {
    new Transformation<>(null, null, null);
  }

  /**
   * Tests if the identifiers of the resulting elements are the same as in the
   * input graph.
   *
   * @throws Exception
   */
  @Test
  public void testIdEquality() throws Exception {
    FlinkAsciiGraphLoader<GraphHeadPojo, VertexPojo, EdgePojo> loader =
      getLoaderFromString(TEST_GRAPH);

    List<GradoopId> expectedGraphHeadIds = Lists.newArrayList();
    List<GradoopId> expectedVertexIds = Lists.newArrayList();
    List<GradoopId> expectedEdgeIds = Lists.newArrayList();

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> inputGraph =
      loader.getLogicalGraphByVariable("g0");

    inputGraph.getGraphHead().map(new Id<GraphHeadPojo>()).output(
      new LocalCollectionOutputFormat<>(expectedGraphHeadIds));
    inputGraph.getVertices().map(new Id<VertexPojo>()).output(
      new LocalCollectionOutputFormat<>(expectedVertexIds));
    inputGraph.getEdges().map(new Id<EdgePojo>()).output(
      new LocalCollectionOutputFormat<>(expectedEdgeIds));

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> result = inputGraph
      .transform(
        new GraphHeadModifier<GraphHeadPojo>(),
        new VertexModifier<VertexPojo>(),
        new EdgeModifier<EdgePojo>()
      );

    List<GradoopId> resultGraphHeadIds = Lists.newArrayList();
    List<GradoopId> resultVertexIds = Lists.newArrayList();
    List<GradoopId> resultEdgeIds = Lists.newArrayList();

    result.getGraphHead()
      .map(new Id<GraphHeadPojo>())
      .output(new LocalCollectionOutputFormat<>(resultGraphHeadIds));
    result.getVertices()
      .map(new Id<VertexPojo>())
      .output(new LocalCollectionOutputFormat<>(resultVertexIds));
    result.getEdges()
      .map(new Id<EdgePojo>())
      .output(new LocalCollectionOutputFormat<>(resultEdgeIds));

    getExecutionEnvironment().execute();

    validateIdEquality(expectedGraphHeadIds, resultGraphHeadIds);
    validateIdEquality(expectedVertexIds, resultVertexIds);
    validateIdEquality(expectedEdgeIds, resultEdgeIds);
  }


  /**
   * Tests the data in the resulting graph.
   *
   * @throws Exception
   */
  @Test
  public void testDataEquality() throws Exception {
    FlinkAsciiGraphLoader<GraphHeadPojo, VertexPojo, EdgePojo> loader =
      getLoaderFromString(TEST_GRAPH);

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> original = loader
      .getLogicalGraphByVariable("g0");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> expected = loader
      .getLogicalGraphByVariable("g01");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo>
      result = original.transform(
      new GraphHeadModifier<GraphHeadPojo>(),
      new VertexModifier<VertexPojo>(),
      new EdgeModifier<EdgePojo>()
    );

    collectAndAssertTrue(result.equalsByData(expected));
  }

  @Test
  public void testGraphHeadOnlyTransformation() throws Exception {
    FlinkAsciiGraphLoader<GraphHeadPojo, VertexPojo, EdgePojo> loader =
      getLoaderFromString(TEST_GRAPH);

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> original = loader
      .getLogicalGraphByVariable("g0");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> expected = loader
      .getLogicalGraphByVariable("g02");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> result = original
      .transformGraphHead(new GraphHeadModifier<GraphHeadPojo>());

    collectAndAssertTrue(result.equalsByData(expected));
  }

  @Test
  public void testVertexOnlyTransformation() throws Exception {
    FlinkAsciiGraphLoader<GraphHeadPojo, VertexPojo, EdgePojo> loader =
      getLoaderFromString(TEST_GRAPH);

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> original = loader
      .getLogicalGraphByVariable("g0");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> expected = loader
      .getLogicalGraphByVariable("g03");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> result = original
      .transformVertices(new VertexModifier<VertexPojo>());

    collectAndAssertTrue(result.equalsByData(expected));
  }

  @Test
  public void testEdgeOnlyTransformation() throws Exception {
    FlinkAsciiGraphLoader<GraphHeadPojo, VertexPojo, EdgePojo> loader =
      getLoaderFromString(TEST_GRAPH);

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> original = loader
      .getLogicalGraphByVariable("g0");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> expected = loader
      .getLogicalGraphByVariable("g04");

    LogicalGraph<GraphHeadPojo, VertexPojo, EdgePojo> result = original
      .transformEdges(new EdgeModifier<EdgePojo>());

    collectAndAssertTrue(result.equalsByData(expected));
  }

  public static class GraphHeadModifier<G extends EPGMGraphHead>
    implements TransformationFunction<G> {

    @Override
    public G execute(G current, G transformed) {
      transformed.setLabel(current.getLabel());
      transformed.setProperty("a", current.getPropertyValue("a").getInt() + 1);
      return transformed;
    }
  }

  public static class VertexModifier<V extends EPGMVertex>
    implements TransformationFunction<V> {

    @Override
    public V execute(V current, V transformed) {
      transformed.setLabel(current.getLabel());
      if (current.getLabel().equals("A")) {
        transformed.setProperty("a", current.getPropertyValue("a").getInt() + 1);
        transformed.setProperty("b", current.getPropertyValue("b").getInt() - 1);
      } else if (current.getLabel().equals("B")) {
        transformed.setProperty("d", current.getPropertyValue("c"));
      }
      return transformed;
    }
  }

  public static class EdgeModifier<E extends EPGMEdge>
    implements TransformationFunction<E> {

    @Override
    public E execute(E current, E transformed) {
      return transformed;
    }
  }
}