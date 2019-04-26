package solutions;

import java_bootcamp.ServiceState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class ServiceContract implements Contract {
    public static String ID = "java_bootcamp.ServiceContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        ServiceState outputState = tx.outputsOfType(ServiceState.class).get(0);
        ServiceState inputState = tx.getInputStates().size() == 1 ? (ServiceState)tx.getInputStates().get(0) : null;
        Command<CommandData> command = tx.getCommand(0);

        if(command.getValue() instanceof Commands.Request) {
            requireThat(req -> {
                req.using("one output has been created", tx.getOutputStates().size() == 1);
                req.using("owner is a required signer", command.getSigners().contains(outputState.getOwner().getOwningKey()));
                req.using("only one command", tx.getCommands().size() == 1);

                return null;
            });
        } else if (command.getValue() instanceof Commands.Service) {
            requireThat(req -> {
                req.using("services provided is not empty", outputState.getServicesProvided() != "");
                req.using("input is consumed and output created", (inputState != null && outputState != null));
                req.using("only one command", tx.getCommands().size() == 1);

                req.using("owner has not changed", outputState.getOwner().equals(inputState.getOwner()));
                req.using("manufacturer has not changed", outputState.getManufacturer().equals(inputState.getManufacturer()));

                req.using("owner is a required signer", command.getSigners().contains(outputState.getOwner().getOwningKey()));
                req.using("mechanic is a required signer", command.getSigners().contains(outputState.getMechanic().getOwningKey()));

                return null;
            });
        } else {
            throw new IllegalArgumentException("Unexpected command");
        }
    }

    public interface Commands extends CommandData {
        class Request implements Commands { }
        class Service implements Commands { }
    }
}