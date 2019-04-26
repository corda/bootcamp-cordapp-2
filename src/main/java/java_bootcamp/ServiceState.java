package java_bootcamp;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* Our state, defining a shared fact on the ledger.
 * See src/main/java/examples/ArtState.java for an example. */
public class ServiceState implements ContractState {

    private final Party owner;
    private final Party mechanic;
    private final Party manufacturer;
    private final String servicesProvided;
    private final Boolean ecoFriendly;

    public ServiceState(Party owner, Party mechanic, Party manufacturer, String servicesProvided, Boolean ecoFriendly) {
        this.owner = owner;
        this.mechanic = mechanic;
        this.manufacturer = manufacturer;
        this.servicesProvided = servicesProvided;
        this.ecoFriendly = ecoFriendly;
    }

    public Party getOwner() {
        return owner;
    }

    public Party getMechanic() {
        return mechanic;
    }

    public Party getManufacturer() {
        return manufacturer;
    }

    public String getServicesProvided() {
        return servicesProvided;
    }

    public Boolean getEcoFriendly() {
        return ecoFriendly;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner, mechanic, manufacturer);
    }
}