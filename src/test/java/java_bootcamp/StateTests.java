package java_bootcamp;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class StateTests {
    private final Party owner = new TestIdentity(new CordaX500Name("Alice", "", "GB")).getParty();
    private final Party mechanic = new TestIdentity(new CordaX500Name("BobTheMechanic", "", "GB")).getParty();
    private final Party manufacturer = new TestIdentity(new CordaX500Name("CordaCars", "", "GB")).getParty();

    @Test
    public void serviceStateHasOwnerMechanicManufacturerAndParamsOfCorrectTypeInConstructor() {
        new ServiceState(owner, mechanic, manufacturer, "many services", true);
    }

    @Test
    public void serviceStateHasGettersForOwnerMechanicManufacturerServicesAndEcofriendly() {
        ServiceState serviceState = new ServiceState(owner, mechanic, manufacturer, "many services", true);
        assertEquals(owner, serviceState.getOwner());
        assertEquals(mechanic, serviceState.getMechanic());
        assertEquals(manufacturer, serviceState.getManufacturer());
        assertEquals("many services", serviceState.getServicesProvided());
        assertEquals(true, serviceState.getEcoFriendly());
    }

    @Test
    public void serviceStateImplementsContractState() {
        assert(new ServiceState(owner, mechanic, manufacturer, "many services", true) instanceof ContractState);
    }

    @Test
    public void serviceStateHasThreeParticipantsTheOwnerMechanicAndTheManufacturer() {
        ServiceState serviceState = new ServiceState(owner, mechanic, manufacturer, "many services", true);
        assertEquals(3, serviceState.getParticipants().size());
        assert(serviceState.getParticipants().contains(owner));
        assert(serviceState.getParticipants().contains(mechanic));
        assert(serviceState.getParticipants().contains(manufacturer));
    }
}