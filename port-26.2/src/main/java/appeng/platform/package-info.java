/**
 * FABRIC: Fabric-backed stand-ins for the parts of the NeoForge API that AE2 uses. The package layout mirrors
 * {@code net.neoforged}, so upstream files only need their imports rewritten ({@code net.neoforged.X} becomes
 * {@code appeng.platform.X}; see tools/neoimports.py). These are independent implementations written for this port,
 * not copies of NeoForge code. Only the API surface that AE2 uses is provided.
 */
package appeng.platform;
