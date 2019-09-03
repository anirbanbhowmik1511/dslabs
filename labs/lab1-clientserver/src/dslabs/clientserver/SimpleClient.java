package dslabs.clientserver;

import dslabs.atmostonce.AMOCommand;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * See the documentation of {@link Client} and {@link Node} for important
 * implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
    private final Address serverAddress;
    private Request request;
    private Reply reply;
    private int seqNumber = 0;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        this.request = new Request(command,seqNumber++);
        this.reply = null;
        this.send(request, serverAddress);
        this.set(new ClientTimer(request), ClientTimer.CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        return reply != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        while (reply == null){
            this.wait();
        }
        return reply.result();
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        if(m.seqNumber() == request.seqNumber()){
            reply = m;
            notify();
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        if(request != null && reply == null && Objects.equals(t.request(), request)){
            this.seqNumber = t.request().seqNumber();
            this.sendCommand(t.request().command());
        }
    }
}
