package dslabs.atmostonce;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application>
        implements Application {
    @Getter @NonNull private final T application;

    Map<Address, AMOResult> clientStates = new HashMap<>();

    @Override
    public AMOResult execute(Command command) {
        if (!(command instanceof AMOCommand)) {
            throw new IllegalArgumentException();
        }

        AMOCommand amoCommand = (AMOCommand) command;
        if(!alreadyExecuted(amoCommand)){
            Result result = application.execute(amoCommand.command());
            AMOResult amoResult = new AMOResult(result, amoCommand.seqNumber());
            clientStates.put(amoCommand.clientAddress(), amoResult);
            return amoResult;
        }else if(amoCommand.seqNumber() == clientStates.get(amoCommand.clientAddress()).seqNumber()){
            return clientStates.get(amoCommand.clientAddress());
        }else{
            return new AMOResult(null, amoCommand.seqNumber());
        }
    }

    public Result executeReadOnly(Command command) {
        if (!command.readOnly()) {
            throw new IllegalArgumentException();
        }

        if (command instanceof AMOCommand) {
            return execute(command);
        }

        return application.execute(command);
    }

    public boolean alreadyExecuted(AMOCommand amoCommand) {
        if(clientStates.containsKey(amoCommand.clientAddress())) {
            return amoCommand.seqNumber() <= clientStates.get(amoCommand.clientAddress()).seqNumber();
        }
        return false;
    }
}
