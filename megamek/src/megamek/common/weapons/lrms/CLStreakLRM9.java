/**
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package megamek.common.weapons.lrms;

import megamek.common.SimpleTechLevel;

/**
 * @author Sebastian Brocks
 */
public class CLStreakLRM9 extends StreakLRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 5240577239366457930L;

    /**
     *
     */
    public CLStreakLRM9() {
        super();
        name = "Streak LRM 9";
        setInternalName("CLStreakLRM9");
        addLookupName("Clan Streak LRM-9");
        addLookupName("Clan Streak LRM 9");
        heat = 0;
        rackSize = 9;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 3.6;
        criticals = 1;
        bv = 155;
        cost = 135000;
        // Per Herb all ProtoMech launcher use the ProtoMech Chassis progression. 
        //But LRM Tech Base and Avail Ratings.
        rulesRefs = "327,TO";
        techAdvancement.setTechBase(TECH_BASE_CLAN).setTechRating(RATING_F)
            .setAvailability(RATING_X, RATING_X, RATING_F, RATING_E)
            .setClanAdvancement(3057, 3079, 3088).setClanApproximate(false, true, false)
            .setPrototypeFactions(F_CCY).setProductionFactions(F_CJF)
            .setStaticTechLevel(SimpleTechLevel.EXPERIMENTAL);
    }
}
