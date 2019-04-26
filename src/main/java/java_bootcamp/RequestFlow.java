package java_bootcamp;

import co.paralleluniverse.fibers.Suspendable;
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

        /* ============================================================================
         *         TODO 1 - Create a new Request command with owner as a required signer
         * ===========================================================================*/

        Command command = null;

        /* ============================================================================
         *         TODO 1 - Create a new TransactionBuilder, adding in your command and the requestState created above
         *         POI: Why are we adding requestState as an output, wy not an input?
         * ===========================================================================*/

        TransactionBuilder transactionBuilder = null;

        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        return subFlow(new FinalityFlow(signedTransaction));
    }
}