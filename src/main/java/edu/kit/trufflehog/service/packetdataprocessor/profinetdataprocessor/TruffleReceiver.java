package edu.kit.trufflehog.service.packetdataprocessor.profinetdataprocessor;

import edu.kit.trufflehog.command.trufflecommand.ITruffleCommand;
import edu.kit.trufflehog.command.trufflecommand.ReceiverErrorCommand;
import edu.kit.trufflehog.util.INotifier;
import edu.kit.trufflehog.util.Notifier;

/**
 * <p>
 *     This class is a runnable notifier service that fetches packet data from the spp_profinet snort plugin, generates
 *     packet data objects and packs them into commands.
 *     Any other service can register as a listener and will receive the commands generated by this service.
 *     The class generalises the different types of inter-process communication.
 * </p>
 * <p>
 *     Possible implementations: {@link UnixSocketReceiver}
 * </p>
 *
 * @author Mark Giraud
 * @version 1.0
 */
public abstract class TruffleReceiver extends Notifier<ITruffleCommand> implements INotifier<ITruffleCommand>, Runnable {

    /**
     * <p>
     *     This method connects the {@link TruffleReceiver} to the snort process.
     *     If the connection failed a {@link ReceiverErrorCommand} is sent to all listeners.
     * </p>
     */
    public abstract void connect();

    /**
     * <p>
     *     This method disconnects the {@link TruffleReceiver} from the snort process.
     * </p>
     */
    public abstract void disconnect();
}
