package com.curio.utilities.compat;

import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;

/**
 * Interface implementee par ExplorersCompassItemMixin pour lire le champ prive
 * "workerManager" d'ExplorersCompassItem (il n'a pas de getter).
 *
 * IMPORTANT : cette interface est VOLONTAIREMENT hors du package com.curio.utilities.mixin.
 * Mixin interdit de referencer directement une classe du package des mixins
 * (IllegalClassLoadError).
 *
 * Ne la charger que si Explorer's Compass est present.
 */
public interface ExplorersCompassWorkerAccess {

    SearchWorkerManager curioUtilities$getWorkerManager();
}
