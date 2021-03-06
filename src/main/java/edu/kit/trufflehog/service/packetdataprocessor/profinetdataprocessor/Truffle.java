package edu.kit.trufflehog.service.packetdataprocessor.profinetdataprocessor;

import edu.kit.trufflehog.model.network.IPAddress;
import edu.kit.trufflehog.model.network.InvalidIPAddress;
import edu.kit.trufflehog.model.network.InvalidMACAddress;
import edu.kit.trufflehog.model.network.MacAddress;
import edu.kit.trufflehog.service.packetdataprocessor.IPacketData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is used to store packet data which is received from the spp_profinet snort plugin using
 * the {@link TruffleReceiver}.
 * </p>
 *
 * @author Mark Giraud
 * @version 1.1
 */
public class Truffle implements IPacketData {

    private final static Logger logger = LogManager.getLogger();

    private final Map<Class<?>, HashMap<String, Object>> attributes = new HashMap<>();

    Truffle() {

    }

    /**
     *     /**
     * <p>
     * This method builds a {@link Truffle} with the specified arguments as data.
     * The source and destination mac addresses must always be valid.
     * If any of the other arguments is null or invalid it is omitted from the
     * data and an exception might be thrown.
     * </p>
     *
     * @param srcMACAddr the source MAC address. MUST be valid
     * @param dstMACAddr the destination MAC address. MUST be valid
     * @param srcIPAddr  the source IP address. Can be omitted by passing 0
     * @param dstIPAddr  the destination IP address. Can be omitted by passing 0
     * @param nameOfStation the device name. Can be omitted by passing null
     * @param etherType
     * @param serviceID
     * @param serviceIDName
     * @param serviceType
     * @param serviceTypeName
     * @param xid
     * @param responseDelay
     * @return the built {@link Truffle}
     * @throws InvalidProfinetPacket
     */
    static Truffle buildTruffle(final long srcMACAddr,
                                final long dstMACAddr,
                                final long srcIPAddr,
                                final long dstIPAddr,
                                final String nameOfStation,
                                final int etherType,
                                final int serviceID,
                                final String serviceIDName,
                                final int serviceType,
                                final String serviceTypeName,
                                final long xid,
                                final int responseDelay,
                                final int isResponse) throws InvalidProfinetPacket {
        final Truffle truffle = new Truffle();

        try {
            truffle.setAttribute(MacAddress.class, "sourceMacAddress", new MacAddress(srcMACAddr));
            truffle.setAttribute(MacAddress.class, "destMacAddress", new MacAddress(dstMACAddr));
        } catch (InvalidMACAddress invalidMACAddress) {
            throw new InvalidProfinetPacket("Error, invalid mac address");
        }

        try {
            truffle.setAttribute(IPAddress.class, "sourceIPAddress", new IPAddress(srcIPAddr));
        } catch (InvalidIPAddress invalidIPAddress) {
            throw new InvalidProfinetPacket("Invalid source ip address: " + srcIPAddr);
        }

        try {
            truffle.setAttribute(IPAddress.class, "destIPAddress", new IPAddress(dstIPAddr));
        } catch (InvalidIPAddress invalidIPAddress) {
           // throw new InvalidProfinetPacket("Invalid destination ip address: " + dstIPAddr);
        }

        if (nameOfStation != null) {
            truffle.setAttribute(String.class, "deviceName", nameOfStation);
        }

        truffle.setAttribute(Integer.class, "etherType", etherType);
        truffle.setAttribute(Integer.class, "serviceType", serviceType);
        truffle.setAttribute(Integer.class, "serviceID", serviceID);

        truffle.setAttribute(Long.class, "xid", xid);

        truffle.setAttribute(Integer.class, "responseDelay", responseDelay);

        truffle.setAttribute(Boolean.class, "isResponse", isResponse != 0);

        if (serviceIDName != null) {
            truffle.setAttribute(String.class, "serviceTypeName", serviceTypeName);
        }

        if (serviceTypeName != null) {
            truffle.setAttribute(String.class, "serviceIDName", serviceIDName);
        }

        truffle.setAttribute(Long.class, "timeOfArrival", Instant.now().toEpochMilli());

        return truffle;
    }

    /**
     * <p>
     * This method adds a new element under the specified type and name to the Truffle.
     * </p>
     *
     * @param attributeType       The type of the element to add.
     * @param attributeIdentifier The string identifier of the object.
     * @param value               The value to add under the identifier.
     * @param <T>                 The Type of the element to add.
     * @return The value of the previous mapping. If no element was mapped under the identifier then null is returned.
     */
    @SuppressWarnings("unchecked")
    <T> T setAttribute(final Class<T> attributeType, final String attributeIdentifier, final T value) {
        HashMap<String, Object> attributeMap = attributes.get(attributeType);

        if (attributeMap == null) {
            attributeMap = new HashMap<>();
            attributes.put(attributeType, attributeMap);
        }

        return (T) attributeMap.put(attributeIdentifier, value);

    }

    /**
     * {@inheritDoc}
     *
     * @param attributeType       The type of attribute that is supposed to be retrieved, for example Integer.class
     * @param attributeIdentifier The string identifier of the attribute that should be retrieved.
     * @param <T>                 The type of the attribute that is retrieved.
     * @return The value of the attribute or null if nothing was found under the specified identifier
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(final Class<T> attributeType, final String attributeIdentifier) {
        HashMap<?, ?> attributeMap = attributes.get(attributeType);

        if (attributeMap == null)
            return null;

        return (T) attributeMap.get(attributeIdentifier);
    }

    /**
     * This toString method is for debug purposes only.
     * @return debug information about this object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        attributes.values().stream().forEach(map -> {

            map.entrySet().stream().forEach(entry -> {

                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');

            });

        });

/*        MacAddress srcMacAddress = getAttribute(MacAddress.class, "sourceMacAddress");
        MacAddress destMacAddress = getAttribute(MacAddress.class, "destMacAddress");
        Short etherType = getAttribute(Short.class, "etherType");

        sb.append("Source mac address: ");
        if (srcMacAddress != null) {
            sb.append(srcMacAddress);
        }
        sb.append("\nDestination mac address: ");
        if (destMacAddress != null) {
            sb.append(destMacAddress);
        }
        sb.append("\nEther type: ");
        if (etherType != null) {
            sb.append(etherType.toString());
        }
        sb.append("\n\n");

        // rest of data:

        for (HashMap<String, Object> map : attributes.values()) {
            for (Object o : map.values()) {
                sb.append(o);
                sb.append(", ");
            }
        }

        sb.append("\n\n");*/

        return sb.toString();
    }
}