package java_bootcamp;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class ServiceFlow extends FlowLogic<SignedTransaction> {

    private final Party owner;
    private final String servicesProvided;
    private final Boolean ecoFriendly;

    public ServiceFlow(Party owner, String servicesProvided, Boolean ecoFriendly) {
        this.owner = owner;
        this.servicesProvided = servicesProvided;
        this.ecoFriendly = ecoFriendly;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }


    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        /* ============================================================================
         *         Here we retrieve the existing state from the node's vault
         * ===========================================================================*/
        List<StateAndRef<ServiceState>> serviceStates = getServiceHub().getVaultService().queryBy(ServiceState.class).getStates();
        StateAndRef<ServiceState> inputServiceStateAndRef = serviceStates.stream().filter(record -> {
            return record.getState().getData().getOwner().equals(owner);
        }).findAny().orElseThrow(() -> new IllegalArgumentException("No service record found for owner"));

        ServiceState inputServiceState = inputServiceStateAndRef.getState().getData();

        /* ===========================================================================*/

        /* ============================================================================
         *         TODO 1 - Create our output ServiceState to reflect any services carried out!
         *         Hint: you can use the existing properties found in the input state
         * ===========================================================================*/

        ServiceState outputServiceState = null;

        /* ============================================================================
         *         TODO 2 - Create our Service command. Both owner and mechanic should be signers
         * ===========================================================================*/

        Command command = null;


        /* ============================================================================
         *         TODO 3 - Obtain a reference to the notary
         *         Hint: notary should be the same as input state. See if you can get the notary from there
         * ===========================================================================*/

        Party notary = null;

        /* ============================================================================
         *         TODO 4 - Create a transaction builder with the input StateAndRef, output and command
         *         POI: Why is the input a StateAndRef and not just a state?
         * ===========================================================================*/

        TransactionBuilder transactionBuilder = null;

        SignedTransaction partSignedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        FlowSession session = initiateFlow(owner);
        SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(partSignedTransaction, Arrays.asList(session)));

        return subFlow(new FinalityFlow(signedTransaction));
    }
}