/*
 * This file is part of TruffleHog.
 *
 * TruffleHog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TruffleHog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TruffleHog.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.kit.trufflehog.view.jung.visualization;

import edu.kit.trufflehog.model.jung.layout.ObservableLayout;
import edu.kit.trufflehog.model.network.graph.IComposition;
import edu.kit.trufflehog.model.network.graph.components.ViewComponent;
import edu.kit.trufflehog.model.network.graph.components.edge.IEdgeRenderer;
import edu.kit.trufflehog.model.network.graph.components.node.NodeStatisticsComponent;
import edu.kit.trufflehog.util.bindings.MyBindings;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Map;

/**
 * \brief
 * \details
 * \date 21.03.16
 * \copyright GNU Public License
 *
 * @author Jan Hermes
 * @version 0.0.1
 */
public class FXVisualizationViewer<V extends IComposition, E extends IComposition> extends Group implements VisualizationServer<V, E> {

    private static final Logger logger = LogManager.getLogger();

    private final DoubleProperty myScale = new SimpleDoubleProperty(1.0);


    private final PannableCanvas canvas;
    private final NodeGestures nodeGestures;

    private ObservableLayout<V, E> layout;

    public FXVisualizationViewer(final ObservableLayout<V, E> layout) {

        // create canvas
        canvas = new PannableCanvas();

        // we don't want the canvas on the top/left in this example => just
        // translate it a bit
        canvas.setTranslateX(100);
        canvas.setTranslateY(100);

        // create sample nodes which can be dragged
        nodeGestures = new NodeGestures( canvas);

        this.getChildren().add(canvas);
        this.layout = layout;

        this.layout.getObservableGraph().addGraphEventListener(e -> {

            //Platform.runLater(() -> {

                switch (e.getType()) {
                    case VERTEX_ADDED:

                        final V node = ((GraphEvent.Vertex<V, E>) e).getVertex();

                        logger.debug("adding: " + node);

                        Platform.runLater(() -> initVertex(node));

                        break;
                    case EDGE_ADDED:

                        final E edge = ((GraphEvent.Edge<V, E>) e).getEdge();
                        Platform.runLater(() -> initEdge(edge));
                        break;
                    case VERTEX_CHANGED:

                        break;
                    case EDGE_CHANGED:

                        break;
                }
            //});
        });

        layout.getGraph().getVertices().forEach(v -> Platform.runLater(() -> this.initVertex(v)));
        layout.getGraph().getEdges().forEach(e -> Platform.runLater(() -> this.initEdge(e)));
    }

    // TODO check if synch is needed
    synchronized
    private void initEdge(E edge) {

        final Pair<V> pair = this.layout.getGraph().getEndpoints(edge);

        final Shape srcShape = pair.getFirst().getComponent(ViewComponent.class).getRenderer().getShape();
        final Shape destShape = pair.getSecond().getComponent(ViewComponent.class).getRenderer().getShape();

        final DoubleProperty srcX = srcShape.translateXProperty();
        final DoubleProperty srcY = srcShape.translateYProperty();
        final DoubleProperty destX = destShape.translateXProperty();
        final DoubleProperty destY = destShape.translateYProperty();


        final DoubleBinding deltaX = destX.subtract(srcX);
        final DoubleBinding deltaY = destY.subtract(srcY);

        final DoubleBinding length = MyBindings.sqrt(Bindings.add(MyBindings.pow2(deltaX), MyBindings.pow2(deltaY)));

        final DoubleBinding normalX = deltaX.divide(length);
        final DoubleBinding normalY = deltaY.divide(length);


        final Circle destCircle = (Circle) destShape;
        final Circle srcCircle = (Circle) srcShape;

        final DoubleBinding realSoureX = srcX.add(normalX.multiply(srcCircle.radiusProperty().multiply(srcShape.scaleXProperty())));
        final DoubleBinding realSoureY = srcY.add(normalY.multiply(srcCircle.radiusProperty().multiply(srcShape.scaleXProperty())));

        final DoubleBinding realDestX = srcX.add(normalX.multiply(length.subtract(destCircle.radiusProperty().multiply(destShape.scaleXProperty()))));
        final DoubleBinding realDestY = srcY.add(normalY.multiply(length.subtract(destCircle.radiusProperty().multiply(destShape.scaleXProperty()))));


        final Circle mirkle = new Circle(10);
        mirkle.setFill(Color.GREEN);
        mirkle.translateXProperty().bind(realSoureX);
        mirkle.translateYProperty().bind(realSoureY);
        canvas.getChildren().add(mirkle);


/*        final Circle circle = new Circle(50);
        circle.translateXProperty().bind(srcX.add(destX.subtract(srcX).divide(2)));
        circle.translateYProperty().bind(srcY.add(destY.subtract(srcY).divide(2)));
        canvas.getChildren().add(circle);*/

        IEdgeRenderer edgeRenderer = (IEdgeRenderer) edge.getComponent(ViewComponent.class).getRenderer();

        // TODO create proper bezier curve and take the shape from the viewcomponent of the edge maybe??!
        final Line curve = edgeRenderer.getLine();
        curve.setStrokeWidth(2);
        curve.setFill(null);

       // Path path = new Path(curve);


        curve.endXProperty().bind(realDestX);
        curve.endYProperty().bind(realDestY);

        curve.startXProperty().bind(realSoureX);
        curve.startYProperty().bind(realSoureY);

        canvas.getChildren().add(curve);
    }
    synchronized
    private void initVertex(V vertex) {

        final Shape nodeShape = vertex.getComponent(ViewComponent.class).getRenderer().getShape();
        nodeShape.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
        nodeShape.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());

        final NodeStatisticsComponent nsc = vertex.getComponent(NodeStatisticsComponent.class);

        logger.debug(nodeShape);
        //nodeShape.scaleXProperty().bind(nsc.getCommunicationCountProperty().divide(port.getMaxThroughputProperty()).multiply(20));
       // nodeShape.scaleYProperty().bind(nsc.getCommunicationCountProperty().divide(port.getMaxThroughputProperty()).multiply(20));


        nodeShape.setTranslateX(layout.transform(vertex).getX() / myScale.get());
        nodeShape.setTranslateY(layout.transform(vertex).getY() / myScale.get());

        canvas.getChildren().add(nodeShape);
    }


    public double getScale() {
        return myScale.get();
    }

    public void setScale( double scale) {
        myScale.set(scale);
    }

    public void setPivot( double x, double y) {
        setTranslateX(getTranslateX()-x);
        setTranslateY(getTranslateY()-y);
    }
    public void refreshLayout() {

        this.layout = new ObservableLayout<>(new FRLayout<>(this.layout.getObservableGraph()));
        layout.setSize(new Dimension(700,700));

        this.layout.getGraph().getVertices().forEach(v -> System.out.println(layout.apply(v)));

        this.repaint();
    }

    @Override
    public void setDoubleBuffered(boolean b) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public boolean isDoubleBuffered() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public VisualizationModel<V, E> getModel() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setModel(VisualizationModel<V, E> visualizationModel) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setRenderer(Renderer<V, E> renderer) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public Renderer<V, E> getRenderer() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setGraphLayout(Layout<V, E> layout) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public Layout<V, E> getGraphLayout() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }


    @Override
    public Map<RenderingHints.Key, Object> getRenderingHints() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setRenderingHints(Map<RenderingHints.Key, Object> map) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void addPreRenderPaintable(Paintable paintable) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void removePreRenderPaintable(Paintable paintable) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void addPostRenderPaintable(Paintable paintable) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void removePostRenderPaintable(Paintable paintable) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public ChangeListener[] getChangeListeners() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void fireStateChanged() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public PickedState<V> getPickedVertexState() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public PickedState<E> getPickedEdgeState() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setPickedVertexState(PickedState<V> pickedState) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setPickedEdgeState(PickedState<E> pickedState) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public GraphElementAccessor<V, E> getPickSupport() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setPickSupport(GraphElementAccessor<V, E> graphElementAccessor) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public Point2D getCenter() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public RenderContext<V, E> getRenderContext() {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void setRenderContext(RenderContext<V, E> renderContext) {
        throw new UnsupportedOperationException("Operation not implemented yet");
    }

    @Override
    public void repaint() {


       this.layout.getGraph().getVertices().forEach(v -> {

            //this.layout = new FRLayout<>(observableGraph);

            final ViewComponent vc = v.getComponent(ViewComponent.class);

            //System.out.println(layout.transform(v));

           vc.getRenderer().getShape().setTranslateX(layout.transform(v).getX() / myScale.get());
           vc.getRenderer().getShape().setTranslateY(layout.transform(v).getY() / myScale.get());

        });


    }

    public PannableCanvas getCanvas() {
        return canvas;
    }
}