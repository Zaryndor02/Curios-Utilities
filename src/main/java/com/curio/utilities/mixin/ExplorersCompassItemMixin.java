package com.curio.utilities.mixin;

import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;
import com.curio.utilities.compat.ExplorersCompassWorkerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Donne acces au champ prive ExplorersCompassItem.workerManager, necessaire pour
 * ARRETER reellement une recherche (workerManager.stop() puis clear(), comme le fait
 * ExplorersCompassItem.use() quand on clique en s'accroupissant).
 *
 * @Pseudo + cible par nom de classe (String) : si Explorer's Compass est absent,
 * ce mixin est ignore au lieu de faire planter le jeu.
 */
@Pseudo
@Mixin(targets = "com.chaosthedude.explorerscompass.items.ExplorersCompassItem", remap = false)
public class ExplorersCompassItemMixin implements ExplorersCompassWorkerAccess {

    @Shadow
    private SearchWorkerManager workerManager;

    @Override
    public SearchWorkerManager curioUtilities$getWorkerManager() {
        return this.workerManager;
    }
}
