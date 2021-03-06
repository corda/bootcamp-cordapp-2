package solutions;

import co.paralleluniverse.fibers.Suspendable;
import java_bootcamp.ServiceContract;
import java_bootcamp.ServiceState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

@InitiatingFlow
@StartableByRPC
public class RequestFlow extends FlowLogic<SignedTransaction> {
    private final Party mechanic;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public RequestFlow(Party mechanic) {
        this.mechanic = mechanic;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // We don't really care who the notary is here, so we're happy just grabbing the first one we find
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        Party ourIdentity = getOurIdentity();

        // For the purposes of the bootcamp, we're just going to create our service record for the first time here.
        // Normally this would be pulled from our vault and would have been issued by the manufacturer.
        //---------------------------------------------------------------------------------------------------------
        Party manufacturer = getServiceHub().getNetworkMapCache()
                .getPeerByLegalName(new CordaX500Name("CordaCar", "London", "GB"));
        ServiceState requestState = new ServiceState(ourIdentity, mechanic, manufacturer, "Initial record", true);
        //---------------------------------------------------------------------------------------------------------

        Command command = new Command(new ServiceContract.Commands.Request(), ourIdentity.getOwningKey());

        TransactionBuilder transactionBuilder = new TransactionBuilder(notary);

        transactionBuilder.addOutputState(requestState, ServiceContract.ID);
        transactionBuilder.addCommand(command);

        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        return subFlow(new FinalityFlow(signedTransaction));
    }
}