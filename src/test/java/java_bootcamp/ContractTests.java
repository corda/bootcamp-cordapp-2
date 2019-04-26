package java_bootcamp;

import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity owner = new TestIdentity(new CordaX500Name("Alice", "", "GB"));
    private final TestIdentity mechanic = new TestIdentity(new CordaX500Name("BobTheMechanic", "", "GB"));
    private final TestIdentity manufacturer = new TestIdentity(new CordaX500Name("CordaCars", "", "GB"));
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "GB")));
    private ServiceState serviceState = new ServiceState(owner.getParty(), mechanic.getParty(), manufacturer.getParty(), "many services", true);

    @Test
    public void serviceContractImplementsContract() {
        assert(new ServiceContract() instanceof Contract);
    }

    @Test
    public void serviceContractRequestRequiresOneOutputInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, serviceState);
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractServiceRequiresOneInputAndOneOutputInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, serviceState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output and one input, will verify.
            tx.input(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, serviceState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractRequiresOneCommandInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(ServiceContract.ID, serviceState);
            // Has two commands, will fail.
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(ServiceContract.ID, serviceState);
            // Has one command, will verify.
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractRequestRequiresTheTransactionsOutputToBeAServiceState() {
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(ServiceContract.ID, new DummyState());
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractServiceRequiresTheTransactionsOutputToBeAServiceState() {
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(ServiceContract.ID, new DummyState());
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.input(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, serviceState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractServiceRequiresTheTransactionsOutputToHaveANonEmptyDescription() {
        ServiceState emptyDescriptionState = new ServiceState(owner.getParty(), mechanic.getParty(), manufacturer.getParty(), "", true);
        ServiceState validDescriptionState = new ServiceState(owner.getParty(), mechanic.getParty(), manufacturer.getParty(), "all four wheel changed", true);

        transaction(ledgerServices, tx -> {
            // Has empty description ServiceState, will fail.
            tx.output(ServiceContract.ID, emptyDescriptionState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.fails();
            return null;
        });


        transaction(ledgerServices, tx -> {
            // Has a description ServiceState, will verify.
            tx.input(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, serviceState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Also has a description ServiceState, will verify.
            tx.input(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, validDescriptionState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractRequiresTheTransactionsCommandToBeASupportedCommand() {
        transaction(ledgerServices, tx -> {
            // Has wrong command type, will fail.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(owner.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command type, will verify.
            tx.input(ServiceContract.ID, serviceState);
            tx.output(ServiceContract.ID, serviceState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Service());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command type, will verify.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractRequestRequiresTheOwnerToBeARequiredSignerInTheTransaction() {
        ServiceState serviceState = new ServiceState(owner.getParty(), mechanic.getParty(), manufacturer.getParty(), "many services", true);

        transaction(ledgerServices, tx -> {
            // Owner is not a required signer, will fail.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(mechanic.getPublicKey(), new ServiceContract.Commands.Request());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Owner is a required signer, will verify.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(owner.getPublicKey(), new ServiceContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void serviceContractServiceRequiresTheOwnerAndMechanicToBeARequiredSignerInTheTransaction() {
        ServiceState serviceState = new ServiceState(owner.getParty(), mechanic.getParty(), manufacturer.getParty(), "many services", true);

        transaction(ledgerServices, tx -> {
            // Owner and Mechanic are not a required signer, will fail.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(mechanic.getPublicKey(), new ServiceContract.Commands.Request());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer and Mechanic are required signers, will verify.
            tx.output(ServiceContract.ID, serviceState);
            tx.command(Arrays.asList(owner.getPublicKey(), mechanic.getPublicKey()), new ServiceContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }
}
