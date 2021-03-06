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
package edu.kit.trufflehog.model.network.recording;

import edu.kit.trufflehog.model.network.graph.IComponent;
import edu.kit.trufflehog.model.network.graph.IConnection;
import edu.kit.trufflehog.model.network.graph.INode;
import edu.kit.trufflehog.model.network.graph.IUpdater;
import edu.kit.trufflehog.model.network.graph.components.IRenderer;
import edu.kit.trufflehog.model.network.graph.components.ViewComponent;
import edu.kit.trufflehog.model.network.graph.components.edge.BasicEdgeRenderer;
import edu.kit.trufflehog.model.network.graph.components.edge.EdgeStatisticsComponent;
import edu.kit.trufflehog.model.network.graph.components.edge.MulticastEdgeRenderer;
import edu.kit.trufflehog.model.network.graph.components.edge.StaticRenderer;
import edu.kit.trufflehog.model.network.graph.components.node.FilterPropertiesComponent;
import edu.kit.trufflehog.model.network.graph.components.node.NodeInfoComponent;
import edu.kit.trufflehog.model.network.graph.components.node.NodeRenderer;
import edu.kit.trufflehog.model.network.graph.components.node.NodeStatisticsComponent;
import edu.kit.trufflehog.model.network.graph.components.node.PacketDataLoggingComponent;
import edu.uci.ics.jung.graph.GraphUpdater;

/**
 * \brief
 * \details
 * \date 05.03.16
 * \copyright GNU Public License
 *
 * @author Jan Hermes
 * @version 0.0.1
 */
public class ReplayUpdater implements IUpdater, GraphUpdater<INode, IConnection> {

    @Override
    public boolean update(INode node, INode update) {

        update.stream().filter(IComponent::isMutable).forEach(c -> {
            final IComponent existing = node.getComponent(c.getClass());
            existing.update(c, this);
        });
        // TODO check if really some was changed
        return true;
    }

    @Override
    public boolean update(IConnection oldValue, IConnection newValue) {

        newValue.stream().filter(IComponent::isMutable).forEach(c -> {
            final IComponent existing = oldValue.getComponent(c.getClass());
            existing.update(c, this);
        });
        // TODO check if really some was changed
        return true;
    }

    @Override
    public boolean update(NodeStatisticsComponent nodeStatisticsComponent, IComponent instance) {

        if (!(instance instanceof NodeStatisticsComponent)) {
            return false;
        }
        final NodeStatisticsComponent comp = (NodeStatisticsComponent) instance;

        nodeStatisticsComponent.setIncomingCount(comp.getIncomingCount());
        nodeStatisticsComponent.setOutgoingCount(comp.getOutgoingCount());

        //nodeStatisticsComponent.setCommunicationCountProperty(comp.getCommunicationCount());
        return true;
    }

    @Override
    public boolean update(NodeRenderer nodeRenderer, IRenderer instance) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean update(PacketDataLoggingComponent packetDataLoggingComponent, IComponent instance) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean update(MulticastEdgeRenderer multicastEdgeRendererComponent, IRenderer instance) {

        if (!(instance instanceof MulticastEdgeRenderer)) {
            return false;
        }
        final MulticastEdgeRenderer comp = (MulticastEdgeRenderer) instance;

        multicastEdgeRendererComponent.setShape(comp.getShape());
        //multicastEdgeRendererComponent.setLastUpdate(comp.getLastUpdate());
        return true;
    }

    @Override
    public boolean update(BasicEdgeRenderer basicEdgeRendererComponent, IRenderer instance) {

        if (!(instance instanceof BasicEdgeRenderer)) {
            return false;
        }
        final BasicEdgeRenderer comp = (BasicEdgeRenderer) instance;

        basicEdgeRendererComponent.setShape(comp.getShape());
        basicEdgeRendererComponent.setCurrentBrightness(comp.getCurrentBrightness());

        return true;
    }

    @Override
    public boolean update(EdgeStatisticsComponent edgeStatisticsComponent, IComponent instance) {

        if (!(instance instanceof EdgeStatisticsComponent)) {
            return false;
        }
        final EdgeStatisticsComponent comp = (EdgeStatisticsComponent) instance;

        edgeStatisticsComponent.setLastUpdateTimeProperty(comp.getLastUpdateTime());
        edgeStatisticsComponent.setTrafficProperty(comp.getTraffic());
        return true;
    }

    @Override
    public boolean update(NodeInfoComponent nodeInfoComponent, IComponent instance) {
        if (!nodeInfoComponent.equals(instance)) {
            return false;
        }

        final NodeInfoComponent other = (NodeInfoComponent) instance;

        boolean changed = false;

        if (other.getDeviceName() != null) {
            changed = !nodeInfoComponent.getDeviceName().equals(other.getDeviceName()) || changed;
            nodeInfoComponent.setDeviceName(other.getDeviceName());
        }

        if (other.getIPAddress() != null) {
            changed = !nodeInfoComponent.getDeviceName().equals(other.getDeviceName()) || changed;
            nodeInfoComponent.setIPAddress(other.getIPAddress());
        }

        return changed;
    }

    @Override
    public boolean update(StaticRenderer component, IRenderer instance) {

        component.setShape(instance.getShape());
        return true;
    }

    @Override
    public boolean update(ViewComponent viewComponent, IComponent instance) {

        if (!(instance instanceof ViewComponent)) {
            return false;
        }
        final ViewComponent other = (ViewComponent) instance;

        // FIXME correct updating
        return true;
    }

    @Override
    public boolean update(FilterPropertiesComponent filterPropertiesComponent, IComponent instance) {
        filterPropertiesComponent.getFilterColors().putAll(filterPropertiesComponent.getFilterColors());
        return true;
    }

    public boolean updateVertex(INode existingVertex, INode newVertex) {

        newVertex.stream().filter(IComponent::isMutable).forEach(c -> {
            final IComponent existing = existingVertex.getComponent(c.getClass());
            existing.update(c, this);
        });
        // TODO check if really some was changed
        return true;
    }

    @Override
    public boolean updateEdge(IConnection existingEdge, IConnection newEdge) {

        newEdge.stream().filter(IComponent::isMutable).forEach(c -> {
            final IComponent existing = existingEdge.getComponent(c.getClass());
            existing.update(c, this);
        });
        // TODO check if really some was changed
        return true;
    }
}
