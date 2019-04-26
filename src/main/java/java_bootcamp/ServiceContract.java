package java_bootcamp;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import java.security.PublicKey;
import java.util.List;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class ServiceContract implements Contract {
    public static String ID = "java_bootcamp.ServiceContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        ServiceState outputState = tx.outputsOfType(ServiceState.class).get(0);
        ServiceState inputState = tx.getInputStates().size() == 1 ? (ServiceState) tx.getInputStates().get(0) : null;
        Command<CommandData> command = tx.getCommand(0);
        List<PublicKey> signers = command.getSigners();

        if (command.getValue() instanceof Commands.Request) {
            requireThat(req -> {
                req.using("only one command", tx.getCommands().size() == 1);
                // add rule to check only one output has been created
                // add rule to check owner is a required signer

                return null;
            });
        }

        if (command.getValue() instanceof Commands.Service) {
            requireThat(req -> {
                req.using("only one command", tx.getCommands().size() == 1);
                //TODO add shape rules
                // add rule to check input is consumed
                // add rule to check output is created

                //TODO add business rules
                // add rule to check owner has not changed
                // add rule to check manufacturer has not changed
                // add rule to check services provided is not empty

                //TODO add signer rules
                // add rule to check owner is a required signer. Hint: check signers contains owner;
                // add rule to check mechanic is a required signer. Hint: check signers contains mechanic;

                return null;
            });
        }
    }

    public interface Commands extends CommandData {
        class Request implements Commands { }
        class Service implements Commands { }
    }
}