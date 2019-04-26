package java_bootcamp;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;


public class ExampleContract implements Contract {
    public static String ID = "java_bootcamp.ExampleContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        Command<CommandData> command = tx.getCommand(0);

        if(command.getValue() instanceof Commands.Buy) {
            requireThat(req -> {
                req.using("a consensus rule for buying", 1 == 1);
                req.using("another consensus rule for buying", 1 == 1);
                return null;
            });

        } else if (command.getValue() instanceof Commands.Sell) {
            requireThat(req -> {
                req.using("a consensus rule for selling", 1 == 1);
                req.using("another consensus rule for selling", 1 == 1);
                return null;
            });
        }
    }

    public interface Commands extends CommandData {
        class Buy implements Commands { }
        class Sell implements Commands { }
    }
}