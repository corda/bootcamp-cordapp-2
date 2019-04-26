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

        List<StateAndRef<ServiceState>> serviceStates = getServiceHub().getVaultService().queryBy(ServiceState.class).getStates();

        StateAndRef<ServiceState> inputServiceStateAndRef = serviceStates.stream().filter(record -> {
            return record.getState().getData().getOwner().equals(owner);
        }).findAny().orElseThrow(() -> new IllegalArgumentException("No service record found for owner"));

        ServiceState inputServiceState = inputServiceStateAndRef.getState().getData();

        ServiceState outputServiceState = new ServiceState(inputServiceState.getOwner(),
                inputServiceState.getMechanic(), inputServiceState.getManufacturer(), servicesProvided, ecoFriendly);

        Command command = new Command(new ServiceContract.Commands.Service(),
                Arrays.asList(owner.getOwningKey(), getOurIdentity().getOwningKey()));

        Party notary = inputServiceStateAndRef.getState().getNotary();

        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(inputServiceStateAndRef)
                .addOutputState(outputServiceState, ServiceContract.ID)
                .addCommand(command);

        SignedTransaction partSignedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        FlowSession session = initiateFlow(owner);
        SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(partSignedTransaction, Arrays.asList(session)));

        return subFlow(new FinalityFlow(signedTransaction));
    }
}