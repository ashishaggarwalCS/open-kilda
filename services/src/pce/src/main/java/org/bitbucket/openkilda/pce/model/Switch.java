package org.bitbucket.openkilda.pce.model;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.bitbucket.openkilda.messaging.info.event.SwitchEventType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents switch entity.
 */
@JsonSerialize
public class Switch implements Serializable {
    /**
     * Serialization version number constant.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Switch id.
     */
    @JsonProperty("switch_id")
    private String switchId;

    /**
     * Switch ip address.
     */
    @JsonProperty("address")
    private String address;

    /**
     * Switch name.
     */
    @JsonProperty("hostname")
    private String hostname;

    /**
     * Switch description.
     */
    @JsonProperty("description")
    private String description;

    /**
     * Switch state.
     */
    @JsonProperty("state")
    private SwitchEventType state;

    /**
     * Switch controller ip address.
     */
    @JsonProperty("controller")
    private String controller;

    /**
     * Instance constructor.
     *
     * @param switchId    switch id
     * @param address     switch ip address
     * @param hostname    switch name
     * @param description switch description
     * @param state       switch state
     * @param controller  switch controller
     */
    @JsonCreator
    public Switch(@JsonProperty("switch_id") String switchId,
                  @JsonProperty("address") String address,
                  @JsonProperty("hostname") String hostname,
                  @JsonProperty("description") String description,
                  @JsonProperty("state") SwitchEventType state,
                  @JsonProperty("controller") String controller) {
        this.switchId = switchId;
        this.address = address;
        this.hostname = hostname;
        this.description = description;
        this.state = state;
        this.controller = controller;
    }

    /**
     * Returns switch id.
     *
     * @return switch id
     */
    public String getSwitchId() {
        return switchId;
    }

    /**
     * Sets switch id.
     *
     * @param switchId switch id to set
     */
    public void setSwitchId(final String switchId) {
        this.switchId = switchId;
    }

    /**
     * Returns switch state.
     *
     * @return switch state
     */
    public SwitchEventType getState() {
        return state;
    }

    /**
     * Sets switch state.
     *
     * @param state switch state to set
     */
    public void setState(final SwitchEventType state) {
        this.state = state;
    }

    /**
     * Gets switch ip address.
     *
     * @return switch ip address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets switch ip address.
     *
     * @param address switch ip address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets switch hostname.
     *
     * @return switch hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets switch hostname.
     *
     * @param hostname switch hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets switch description.
     *
     * @return switch description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets switch description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets switch controller.
     *
     * @return switch controller
     */
    public String getController() {
        return controller;
    }

    /**
     * Sets switch controller.
     *
     * @param controller controller
     */
    public void setController(String controller) {
        this.controller = controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this)
                .add("switch_id", switchId)
                .add("state", state)
                .add("address", address)
                .add("hostname", hostname)
                .add("description", description)
                .add("controller", controller)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(switchId, state, address, hostname, description, controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Switch that = (Switch) object;
        return Objects.equals(getSwitchId(), that.getSwitchId())
                && Objects.equals(getState(), that.getState())
                && Objects.equals(getAddress(), that.getAddress())
                && Objects.equals(getHostname(), that.getHostname())
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getController(), that.getController());
    }
}
