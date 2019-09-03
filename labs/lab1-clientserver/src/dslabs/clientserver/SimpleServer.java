package dslabs.clientserver;

import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple server that receives requests and returns responses.
 *
 * See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
    private final Application application;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);
        this.application = new AMOApplication<>(app);
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        AMOResult amoResult = (AMOResult)application.execute(new AMOCommand(m.command(), sender, m.seqNumber()));
        this.send(new Reply(amoResult.result(), m.seqNumber()), sender);
    }
}
