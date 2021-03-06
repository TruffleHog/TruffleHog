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
package edu.kit.trufflehog.model.network.graph;

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
import edu.kit.trufflehog.service.packetdataprocessor.IPacketData;
import edu.uci.ics.jung.graph.GraphUpdater;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

/**
 * \brief
 * \details
 * \date 05.03.16
 * \copyright GNU Public License
 *
 * @author Jan Hermes
 * @version 0.0.1
 */
public class LiveUpdater implements IUpdater, GraphUpdater<INode, IConnection> {

    private Logger logger = LogManager.getLogger();

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
    public boolean update(NodeStatisticsComponent nodeStatisticsComponent, IComponent instance) {

        if (!nodeStatisticsComponent.equals(instance))
            return false;

        final NodeStatisticsComponent other = (NodeStatisticsComponent) instance;

        Platform.runLater(() -> {

            nodeStatisticsComponent.setOutgoingCount(nodeStatisticsComponent.getOutgoingCount() + other.getOutgoingCount());

            nodeStatisticsComponent.setIncomingCount(nodeStatisticsComponent.getIncomingCount() + other.getIncomingCount());


        });


        // TODO maybe check for more variants of values (potential bug???)
/*        if (other.getOutgoingCount() == 0 && other.getIncomingCount() == 0)
            nodeStatisticsComponent.setOutgoingCount(nodeStatisticsComponent.getOutgoingCount() + other.getOutgoingCount());
        else if (other.getOutgoingCount() == 1 && other.getIncomingCount() == 0)
            nodeStatisticsComponent.setIncomingCount(nodeStatisticsComponent.getIncomingCount() + other.getIncomingCount());
        else if (other.getOutgoingCount() == 0 && other.getIncomingCount() == 1)
            nodeStatisticsComponent.setIncomingCount(nodeStatisticsComponent.getIncomingCount() + other.getIncomingCount());
        else if (other.getOutgoingCount() == 1 && other.getIncomingCount() == 1)
            nodeStatisticsComponent.setIncomingCount(nodeStatisticsComponent.getIncomingCount() + other.getIncomingCount());
        else
            throw new UnsupportedOperationException("not supported to add other numbers than 0 or 1 for incoming and outgoing updates yet");*/
        //nodeStatisticsComponent.incrementThroughput(1);
        return true;
    }

    @Override
    public boolean update(NodeRenderer nodeRendererComponent, IRenderer instance) {
        //TODO does one need to update this?
        return true;
    }

    @Override
    public boolean update(PacketDataLoggingComponent packetDataLoggingComponent, IComponent instance) {
        if (!packetDataLoggingComponent.equals(instance)) return false;

        PacketDataLoggingComponent updater = (PacketDataLoggingComponent)instance;

        for (IPacketData packetData : updater.getObservablePackets()) {

            Platform.runLater(() -> {

                packetDataLoggingComponent.addPacket(packetData);

            });
        }

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
    public boolean update(MulticastEdgeRenderer multicastEdgeRenderer, IRenderer instance) {

        // TODO implement
/*        multicastEdgeRenderer.setStrokeWidth(5f);
        multicastEdgeRenderer.setMultiplier(1.05f);
        multicastEdgeRenderer.setOpacity(170);*/

        //multicastEdgeRenderer.setLastUpdate(Instant.now().toEpochMilli());
        return true;
    }

    @Override
    public boolean update(BasicEdgeRenderer basicEdgeRenderer, IRenderer instance) {
/*        if (basicEdgeRenderer.getCurrentBrightness() > 0.7) {
            return true;
        }

        // TODO implement more
        basicEdgeRenderer.setCurrentBrightness(1.0f);
        return true;*/
        return true;
    }

    @Override
    public boolean update(EdgeStatisticsComponent edgeStatisticsComponent, IComponent instance) {
        // TODO maybe change to another value
        Platform.runLater(() -> {

            edgeStatisticsComponent.setLastUpdateTimeProperty(Instant.now().toEpochMilli());
            edgeStatisticsComponent.incrementTraffic(1);
        });

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
            Platform.runLater(() -> {
                nodeInfoComponent.setDeviceName(other.getDeviceName());
            });

            changed = true;
        }

        if (other.getIPAddress() != null) {
            Platform.runLater(() -> {
                nodeInfoComponent.setIPAddress(other.getIPAddress());
            });

            changed = true;
        }
        return changed;
    }

    @Override
    public boolean update(StaticRenderer component, IRenderer instance) {

        throw new UnsupportedOperationException("this operation should not be performed by the live updater");
    }

    @Override
    public boolean update(ViewComponent viewComponent, IComponent instance) {

        if (!viewComponent.equals(instance)) {
            return false;
        }
        final ViewComponent other = (ViewComponent) instance;

        // FIXME correct updating
        return true;
    }

    @Override
    public boolean update(FilterPropertiesComponent filterPropertiesComponent, IComponent instance) {
        if (!filterPropertiesComponent.equals(instance))
            return false;

        Platform.runLater(() -> {
            filterPropertiesComponent.addFilterColors(((FilterPropertiesComponent)instance).getFilterColors());
        });


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
