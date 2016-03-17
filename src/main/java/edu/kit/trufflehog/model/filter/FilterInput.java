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
 * You should have received a copy of the GNU General Public License
 * along with TruffleHog.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.kit.trufflehog.model.filter;

import edu.kit.trufflehog.model.configdata.ConfigData;
import javafx.beans.property.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *     The FilterInput class contains the data necessary to create a filter. From this class a filter can be created.
 *     That means the following:
 *     <ul>
 *         <li>
 *             Name: The name of the filter. It has to be unique.
 *         </li>
 *         <li>
 *             Selection model: The selection type of the filter. A filter can either be based on a selection or on an
 *             inverse selection.
 *         </li>
 *         <li>
 *             Origin: The origin of the filter. A filter can originate from an IP Address, from a MAC Address, or
 *             from the current selection. This indicates upon what criteria the filter filters.
 *         </li>
 *         <li>
 *             Rules: The rules of the filter define what the filter matches. These are regular expressions matching
 *             IP addresses, MAC addresses and more.
 *         </li>
 *         <li>
 *             Priority: This priority is used to determine which filter color should be rendered when multiple
 *             filters collide on the same node.
 *         </li>
 *         <li>
 *             Color: The color of the filter determines what color a matched node should become.
 *         </li>
 *         <li>
 *             Legal: If true, all nodes filtered by the filter will be considered as "good" or legal nodes. If set to
 *             false all nodes filtered by the filter will be considered as "evil" or illegal nodes.
 *         </li>
 *         <li>
 *             Active: Whether this filter is currently being applied on the network or not.
 *         </li>
 *     </ul>
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
public class FilterInput implements Serializable {
    private static final transient Logger logger = LogManager.getLogger();

    // Serializable variables
    private String name;
    private SelectionModel selectionModel;
    private FilterOrigin origin;
    private List<String> rules;
    private Color color;
    private boolean legal;
    private boolean active;
    private int priority;

    // Is this filter deleted?
    private transient boolean deleted;

    // Property variables for table view
    private transient StringProperty nameProperty;
    private transient StringProperty selectionModelProperty;
    private transient StringProperty originProperty;
    private transient ObjectProperty<Color> colorProperty;
    private transient BooleanProperty legalProperty;
    private transient BooleanProperty activeProperty;
    private transient IntegerProperty priorityProperty;


    /**
     * <p>
     *     Creates a new FilterInput object that is inactive. That means it will at first not be applied onto the
     *     current network.
     * </p>
     * <p>
     *     <ul>
     *         <li>
     *             Name: The name of the filter. It has to be unique.
     *         </li>
     *         <li>
     *             Selection model: The selection type of the filter. A filter can either be based on a selection or on
     *             an inverse selection.
     *         </li>
     *         <li>
     *             Origin: The origin of the filter. A filter can originate from an IP Address, from a MAC Address, or
     *             from the current selection. This indicates upon what criteria the filter filters.
     *         </li>
     *         <li>
     *             Rules: The rules of the filter define what the filter matches. These are regular expressions matching
     *             IP addresses, MAC addresses and more.
     *         </li>
     *         <li>
     *             Priority: This priority is used to determine which filter color should be rendered when multiple
     *             filters collide on the same node.
     *         </li>
     *         <li>
     *             Color: The color of the filter determines what color a matched node should become.
     *         </li>
     *         <li>
     *             Legal: If true, all nodes filtered by the filter will be considered as "good" or legal nodes. If set
     *             to false all nodes filtered by the filter will be considered as "evil" or illegal nodes.
     *         </li>
     *         <li>
     *             Active: Whether this filter is currently being applied on the network or not.
     *         </li>
     *     </ul>
     * </p>
     *
     * @param name The name of this filter.
     * @param selectionModel The selection model of this filter (inverse selection vs selection).
     * @param origin The origin of this filter.
     * @param rules The rules that define this filter.
     * @param color The color that a node should become if it matches with the filter.
     */
    public FilterInput(final String name,
                       final SelectionModel selectionModel,
                       final FilterOrigin origin,
                       final List<String> rules,
                       final Color color,
                       final boolean legal,
                       final int priority) {
        this.name = name;
        this.selectionModel = selectionModel;
        this.origin = origin;
        this.rules = rules;
        this.color = color;
        this.active = false;
        this.legal = legal;
        this.priority = priority;
        this.deleted = false;
    }

    /**
     * <p>
     *     Gets the name of this filter. It has to be unique.
     * </p>
     *
     * @return The name of this filter.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     *     Sets the name of this filter. It has to be unique.
     * </p>
     *
     * @param name The name of this filter.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>
     *     Gets the name property of this filter. It has to be unique.
     * </p>
     *
     * @return The name property of this filter.
     */
    public StringProperty getNameProperty() {
        return nameProperty;
    }

    /**
     * <p>
     *    Gets the selection type of the filter. A filter can either be based on a selection or on an inverse selection.
     * </p>
     *
     * @return The selection type of this filter.
     */
    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * <p>
     *    Gets the selection type property of the filter. A filter can either be based on a selection or on an inverse
     *    selection.
     * </p>
     *
     * @return The selection type property of this filter.
     */
    public StringProperty getSelectionModelProperty() {
        return selectionModelProperty;
    }

    /**
     * <p>
     *     Gets the origin of the filter. A filter can originate from an IP Address, from a MAC Address, or from the
     *     current selection. This indicates upon what criteria the filter filters.
     * </p>
     *
     * @return The origin of this filter.
     */
    public FilterOrigin getOrigin() {
        return origin;
    }

    /**
     * <p>
     *     Gets the origin property of the filter. A filter can originate from an IP Address, from a MAC Address, or
     *     from the current selection. This indicates upon what criteria the filter filters.
     * </p>
     *
     * @return The origin property of this filter.
     */
    public StringProperty getOriginProperty() {
        return originProperty;
    }

    /**
     * <p>
     *     Gets the set of rules for this filter. The rules of the filter define what the filter matches. These are
     *     regular expressions matching IP addresses, MAC addresses and more.
     * </p>
     *
     * @return The set of rules for this filter.
     */
    public List<String> getRules() {
        return rules;
    }

    /**
     * <p>
     *     Gets the color for this filter. The color of the filter determines what color a matched node should
     *     become.
     * </p>
     *
     * @return The color for this filter.
     */
    public Color getColor() {
        return color;
    }

    /**
     * <p>
     *     Gets the color property for this filter. The color of the filter determines what color a matched node should
     *     become.
     * </p>
     *
     * @return The color property for this filter.
     */
    public ObjectProperty<Color> getColorProperty() {
        return colorProperty;
    }

    /**
     * <p>
     *     Gets the legality state of this filter. If it is set to true, all nodes filtered by the filter will be
     *     considered as "good" or legal nodes. If it is set to false all nodes filtered by the filter will be
     *     considered as "evil" or illegal nodes.
     * </p>
     *
     * @return The legality state of this filter.
     */
    public boolean isLegal() {
        return legal;
    }

    /**
     * <p>
     *     Gets the legality state property of this filter. If it is set to true, all nodes filtered by the filter will
     *     be considered as "good" or legal nodes. If it is set to false all nodes filtered by the filter will be
     *     considered as "evil" or illegal nodes.
     * </p>
     *
     * @return The legality state property of this filter.
     */
    public BooleanProperty getLegalProperty() {
        return legalProperty;
    }

    /**
     * <p>
     *     Gets the current activity state. That means this method returns true if the filter is currently being applied
     *     to the network, and otherwise false.
     * </p>
     *
     * @return True if the filter is currently being applied o the network, else false.
     */
    public boolean isActive() {
        return activeProperty.getValue();
    }

    /**
     * <p>
     *     Sets the set of rules for this filter. The rules of the filter define what the filter matches. These are
     *     regular expressions matching IP addresses, MAC addresses and more.
     * </p>
     *
     * @param rules The set of rules for this filter.
     */
    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    /**
     * <p>
     *     Gets the BooleanProperty behind the activity state. This is mapped to the {@link CheckBoxTableCell} in the
     *     table view in the filters menu.
     * </p>
     *
     * @return the BooleanProperty that is is mapped to the {@link CheckBoxTableCell} in the table view in the filters menu.
     */
    public BooleanProperty getActiveProperty() {
        return activeProperty;
    }

    /**
     * <p>
     *     Gets the priority of the filter. This priority is used to determine which filter color should
     *     be rendered when multiple filters collide on the same node.
     * </p>
     *
     * @return the priority of the filter.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * <p>
     *     Gets the priority property of the filter. This priority is used to determine which filter color should
     *     be rendered when multiple filters collide on the same node.
     * </p>
     *
     * @return the priority property of the filter.
     */
    public IntegerProperty getPriorityProperty() {
        return priorityProperty;
    }

    /**
     * <p>
     *     Sets a flag that the Filter is to be deleted.
     * </p>
     */
    public void setDeleted() {
        deleted = true;
    }

    /**
     * <p>
     *     Gets the deletion flag that specifies whether or not a filter is to be deleted.
     * </p>
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * <p>
     *     Since {@link Property} objects cannot be serialized, THIS METHOD HAS TO BE CALLED AFTER EACH
     *     DESERIALIZATION OF A FILTERINPUT OBJECT to recreate the connection between the Properties and the
     *     normal values that were serialized.
     * </p>
     *
     * @param configData The {@link ConfigData} object used to update this filter to the database.
     */
    public void load(ConfigData configData) {
        // Instantiate property objects
        nameProperty = new SimpleStringProperty(name);
        colorProperty = new SimpleObjectProperty<>(color);
        priorityProperty = new SimpleIntegerProperty(priority);
        legalProperty = new SimpleBooleanProperty(legal);
        activeProperty = new SimpleBooleanProperty(active);

        // Make the selection model look nicer on screen
        if (selectionModel.equals(SelectionModel.SELECTION)) {
            selectionModelProperty = new SimpleStringProperty(configData.getProperty("SELECTION_LABEL"));
        } else {
            selectionModelProperty = new SimpleStringProperty(configData.getProperty("INVERSE_SELECTION_LABEL"));
        }

        // Make the origin look nicer on screen
        if (origin.equals(FilterOrigin.NAME)) {
            originProperty = new SimpleStringProperty(configData.getProperty("NAME_LABEL"));
        } else {
            originProperty = new SimpleStringProperty(origin.name());
        }

        bind(configData);
    }

    /**
     * <p>
     *     Binds this filterInput to the database update function, so that when their value change, they are automatically
     *     updated.
     * </p>
     *
     * @param configData The {@link ConfigData} object used to update this filter to the database.
     */
    private void bind(ConfigData configData) {
        // Bind name to database update function
        nameProperty.addListener((observable, oldValue, newValue) -> {
            configData.updateFilterInput(this, newValue);
            logger.debug("Updated name for FilterInput: " + name + " to table view and database.");
        });

        // Bind type to database update function
        selectionModelProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(configData.getProperty("SELECTION_LABEL"))) {
                selectionModel = SelectionModel.SELECTION;
            } else {
                selectionModel = SelectionModel.INVERSE_SELECTION;
            }

            configData.updateFilterInput(this);
            logger.debug("Updated type for FilterInput: " + name + " to table view and database.");
        });

        // Bind origin to database update function
        originProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(FilterOrigin.IP.name())) {
                origin = FilterOrigin.IP;
            } else if (newValue.equals(FilterOrigin.MAC.name())) {
                origin = FilterOrigin.MAC;
            } else {
                origin = FilterOrigin.NAME;
            }

            configData.updateFilterInput(this);
            logger.debug("Updated origin for FilterInput: " + name + " to table view and database.");
        });

        // Bind color to database update function
        colorProperty.addListener((observable, oldValue, newValue) -> {
            color = newValue;

            configData.updateFilterInput(this);
            logger.debug("Updated color for FilterInput: " + name + " to table view and database.");
        });

        // Bind legal to database update function
        legalProperty.addListener((observable, oldValue, newValue) -> {
            legal = newValue;

            configData.updateFilterInput(this);
            logger.debug("Updated legality for FilterInput: " + name + " to table view and database.");
        });

        // Bind priority to database update function
        priorityProperty.addListener((observable, oldValue, newValue) -> {
            priority = newValue.intValue();

            configData.updateFilterInput(this);
            logger.debug("Updated priority for FilterInput: " + name + " to table view and database.");
        });

        // Bind activity state to database update function
        activeProperty.addListener((observable, oldValue, newValue) -> {
            active = newValue;

            configData.updateFilterInput(this);

            logger.debug("Updated activity state for FilterInput: " + name + " to table view and database.");
        });
    }
}
