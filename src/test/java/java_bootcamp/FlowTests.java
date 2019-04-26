package java_bootcamp;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode ownerNode;
    private StartedMockNode mechanicNode;
    private StartedMockNode manufacturerNode;

    @Before
    public void setup() {
        network = new MockNetwork(ImmutableList.of("java_bootcamp"));
        ownerNode = network.createPartyNode(null);
        mechanicNode = network.createPartyNode(null);
        manufacturerNode = network.createPartyNode(new CordaX500Name("CordaCar", "London", "GB"));
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void transactionConstructedByFlowUsesTheCorrectNotary() throws Exception {
        RequestFlow flow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = ownerNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), output.getNotary());
    }

    @Test
    public void transactionConstructedByRequestFlowHasOneServiceStateOutputWithTheCorrectOwnerMechanicAndManufacturer() throws Exception {
        RequestFlow flow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = ownerNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        ServiceState output = signedTransaction.getTx().outputsOfType(ServiceState.class).get(0);

        assertEquals(ownerNode.getInfo().getLegalIdentities().get(0), output.getOwner());
        assertEquals(mechanicNode.getInfo().getLegalIdentities().get(0), output.getMechanic());
        assertEquals(manufacturerNode.getInfo().getLegalIdentities().get(0), output.getManufacturer());
    }

    @Test
    public void transactionConstructedByFlowHasOneOutputUsingTheCorrectContract() throws Exception {
        RequestFlow flow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = ownerNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals("java_bootcamp.ServiceContract", output.getContract());
    }

    @Test
    public void transactionConstructedByRequestHasOneRequestCommand() throws Exception {
        RequestFlow flow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = ownerNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assert(command.getValue() instanceof ServiceContract.Commands.Request);
    }

    @Test
    public void transactionConstructedByServiceHasOneServiceCommand() throws Exception {
        //run request flow first to create state
        RequestFlow requestFlow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> requestFuture = ownerNode.startFlow(requestFlow);
        network.runNetwork();
        requestFuture.get();

        ServiceFlow flow = new ServiceFlow(ownerNode.getInfo().getLegalIdentities().get(0), "tyres changed", true);
        CordaFuture<SignedTransaction> future = mechanicNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assert(command.getValue() instanceof ServiceContract.Commands.Service);
    }

    @Test
    public void transactionConstructedByRequestFlowHasOneCommandWithTheOwnerAsASigner() throws Exception {
        RequestFlow flow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = ownerNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assertEquals(1, command.getSigners().size());
        assert(command.getSigners().contains(ownerNode.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void transactionConstructedByServiceFlowHasOneCommandWithTheOwnerAndMechanicAsASigner() throws Exception {
        //run request flow first to create state
        RequestFlow requestFlow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> requestFuture = ownerNode.startFlow(requestFlow);
        network.runNetwork();
        requestFuture.get();

        ServiceFlow flow = new ServiceFlow(ownerNode.getInfo().getLegalIdentities().get(0), "tyres changed", true);
        CordaFuture<SignedTransaction> future = mechanicNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assertEquals(2, command.getSigners().size());
        assert(command.getSigners().contains(ownerNode.getInfo().getLegalIdentities().get(0).getOwningKey()));
        assert(command.getSigners().contains(mechanicNode.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void transactionConstructedByRequestFlowHasNoInputsAttachmentsOrTimeWindows() throws Exception {
        RequestFlow flow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = ownerNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(0, signedTransaction.getTx().getInputs().size());
        // The single attachment is the contract attachment.
        assertEquals(1, signedTransaction.getTx().getAttachments().size());
        assertEquals(null, signedTransaction.getTx().getTimeWindow());
    }

    @Test
    public void transactionConstructedByServiceFlowHasNoInputsAttachmentsOrTimeWindows() throws Exception {
        //run request flow first to create state
        RequestFlow requestFlow = new RequestFlow(mechanicNode.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> requestFuture = ownerNode.startFlow(requestFlow);
        network.runNetwork();
        requestFuture.get();

        ServiceFlow flow = new ServiceFlow(ownerNode.getInfo().getLegalIdentities().get(0), "tyres changed", true);
        CordaFuture<SignedTransaction> future = mechanicNode.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getInputs().size());
        // The single attachment is the contract attachment.
        assertEquals(1, signedTransaction.getTx().getAttachments().size());
        assertEquals(null, signedTransaction.getTx().getTimeWindow());
    }
}