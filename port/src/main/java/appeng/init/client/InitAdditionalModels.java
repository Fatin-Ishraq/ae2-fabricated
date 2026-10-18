/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.init.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import appeng.api.parts.PartModelsInternal;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.tesr.CrankRenderer;

/**
 * FABRIC: Additional models are registered through a Fabric model loading plugin instead of NeoForge's
 * ModelEvent.RegisterAdditional.
 */
public class InitAdditionalModels {

    public static void init() {
        ModelLoadingPlugin.register(context -> {
            context.addModels(MolecularAssemblerRenderer.LIGHTS_MODEL.id());
            context.addModels(CrankRenderer.BASE_MODEL.id());
            context.addModels(CrankRenderer.HANDLE_MODEL.id());

            PartModelsInternal.freeze();
            context.addModels(PartModelsInternal.getModels());
        });
    }

}
