package appeng.platform.neoforge.network.event;

import appeng.platform.neoforge.network.registration.PayloadRegistrar;

/**
 * FABRIC: Passed to AE2's payload registration during mod initialization.
 */
public class RegisterPayloadHandlersEvent {
    public PayloadRegistrar registrar(String modId) {
        return new PayloadRegistrar();
    }
}
