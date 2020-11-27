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
 * @author BATTLEMASTER
 */
public class ISEnhancedLRM5 extends EnhancedLRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 3287950524687857609L;

    /**
     *
     */
    public ISEnhancedLRM5() {
        super();
        name = "Enhanced LRM 5";
        setInternalName(name);
        addLookupName("ISEnhancedLRM5");
        heat = 2;
        rackSize = 5;
        minimumRange = 3;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 3.0;
        criticals = 2;
        bv = 52;
        cost = 37500;
        shortAV = 3;
        medAV = 3;
        longAV = 3;
        maxRange = RANGE_LONG;
        rulesRefs = "326,TO";
        techAdvancement.setTechBase(TECH_BASE_IS).setTechRating(RATING_E)
            .setAvailability(RATING_C, RATING_F, RATING_E, RATING_D)
            .setISAdvancement(3058, 3082, 3090).setPrototypeFactions(F_FS)
            .setProductionFactions(F_FS).setStaticTechLevel(SimpleTechLevel.EXPERIMENTAL);
    }
}
