package edu.kit.trufflehog.model.network.graph.components.edge;

import edu.kit.trufflehog.model.network.graph.IComponent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.time.Instant;

/**
 * This Component of a node will handle how a node is displayed in the graph
 * e.g. it is also possible to include bitmaps etc for specific Nodes
 *
 * @author Jan Hermes
 * @version 0.1
 */
public class MulticastEdgeRendererComponent implements IRendererComponent {

    private long lastUpdate = Instant.now().toEpochMilli();

    private final Shape shape = new Ellipse2D.Float(-10, -10, 20, 20);

    Color colorUnpicked = new Color(0x7f7784);
    Color colorPicked = new Color(0xf0caa3);

    private float strokeWidth = 5f;
    private float multiplier = 1.05f;
    private float opacity = 170;

    public MulticastEdgeRendererComponent() {

        // TODO implement
    }

    @Override
    public Shape getShape() {

        if (multiplier >= 70.0f) {
            multiplier = 0;
        }
        multiplier *= 1.3f;

        return AffineTransform.getScaleInstance(multiplier, multiplier).createTransformedShape(shape);
    }

    @Override
    public Color getColorUnpicked() {

        opacity = opacity <= 10 ? 0 : opacity * 0.8f;
        return new Color(colorUnpicked.getRed(), colorUnpicked.getGreen(), colorUnpicked.getBlue(), (int) opacity);
    }

    @Override
    public Color getColorPicked() {

        opacity = opacity <= 10 ? 0 : opacity * 0.8f;
        return new Color(colorPicked.getRed(), colorPicked.getGreen(), colorPicked.getBlue(), (int) opacity);
    }

    @Override
    public Stroke getStroke() {

        strokeWidth *= 1.4f;
        return new BasicStroke(strokeWidth);
    }

    @Override
    public void setColorPicked(Color colorPicked) {

    }

    @Override
    public void setColorUnpicked(Color colorUnpicked) {

    }

    @Override
    public void setShape(Shape shape) {

    }

    @Override
    public void setStroke(Stroke stroke) {

    }


    @Override
    public String name() {
        return "Multicast Edge Renderer";
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public IComponent createDeepCopy() {

        final MulticastEdgeRendererComponent edgeRenderer = new MulticastEdgeRendererComponent();

        edgeRenderer.setColorUnpicked(getColorUnpicked());
        edgeRenderer.setColorPicked(getColorPicked());
        edgeRenderer.setShape(getShape());
        edgeRenderer.setStroke(getStroke());

        edgeRenderer.setOpacity(getOpacity());
        edgeRenderer.setLastUpdate(getLastUpdate());
        return edgeRenderer;
    }

    @Override
    public boolean update(IComponent update) {

        strokeWidth = 5f;
        multiplier = 1.05f;
        opacity = 170;

        setLastUpdate(Instant.now().toEpochMilli());
        return true;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public long getLastUpdate() {

        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
