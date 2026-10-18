package appeng.platform.bus.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FABRIC: Marker kept so that upstream event handler methods compile unchanged. It has no effect; the port registers
 * these handlers with the matching Fabric events explicitly.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
}
