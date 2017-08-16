/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.persistence;

import android.content.res.Resources;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.typeface.IIcon;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.PresentationsTypeface;

/**
 * Created by joseangel.pineiro on 12/10/13.
 */
public enum Presentation {

    INJECTIONS(R.drawable.icp_injection, R.string.injections, R.string.injections_units),
    CAPSULES(R.drawable.icp_capsule, R.string.capsules, R.string.capsules_units),
    EFFERVESCENT(R.drawable.icp_effervescent, R.string.effervescent, R.string.effervescent_units),
    PILLS(R.drawable.icp_pill, R.string.pills, R.string.pills_units),
    SYRUP(R.drawable.icp_syrup, R.string.syrup, R.string.syrup_units),
    DROPS(R.drawable.icp_drop, R.string.drops, R.string.drops_units),
    POMADE(R.drawable.ic_ppomade, R.string.pomade, R.string.pomade_units),
    INHALER(R.drawable.icp_inhaler, R.string.inhaler, R.string.inhaler_units),
    SPRAY(R.drawable.icp_nasalspray, R.string.spray, R.string.spray_units),
    PATCHES(R.drawable.icp_patches, R.string.patches, R.string.patches_units),


    UNKNOWN(R.drawable.ic_presentation_6, R.string.unknown, R.string.unknown_units);

    private int drawable = R.drawable.icp_injection;
    private int nameString = R.string.unknown;
    private int unitsString = R.string.unknown_units;

    Presentation(int drawable, int nameString, int unitsString) {
        this.drawable = drawable;
        this.nameString = nameString;
        this.unitsString = unitsString;
    }

    public int getDrawable() {
        return drawable;
    }

    public String getName(Resources r) {
        return r.getString(nameString);
    }

    public String units(Resources r) {
        return r.getString(unitsString);
    }


    public static Presentation expected(String name, String content) {
        String n = name.toLowerCase() + " " + content.toLowerCase();
        if (n.contains("comprimidos")) {
            return Presentation.PILLS;
        } else if (n.contains("capsulas") || n.contains("cápsulas")) {
            return Presentation.CAPSULES;
        } else if (n.contains("inhala")) {
            return Presentation.INHALER;
        } else if (n.contains("viales") || n.contains("jeringa") || n.contains("perfusi") || n.contains("inyectable")) {
            return Presentation.INJECTIONS;
        } else if (n.contains("gotas") || n.contains("colirio")) {
            return Presentation.DROPS;
        } else if (n.contains("sobres")) {
            return Presentation.EFFERVESCENT;
        } else if (n.contains("tubo") || n.contains("crema") || n.contains("pomada")) {
            return Presentation.POMADE;
        } else if (n.contains("pulverizacion nasal") || n.contains("pulverización nasal") || n.contains("spray")) {
            return Presentation.SPRAY;
        } else if (n.contains("jarabe")) {
            return Presentation.SYRUP;
        }else if (n.contains("parche")) {
                return Presentation.PATCHES;
        } else if (n.contains("suspension oral")) {
            if (!n.contains("polvo") && !n.contains("granulado")) {
                return Presentation.SYRUP;
            } else if (!n.contains("polvo")) {
                // granulado
            } else {
                // sobres
            }
        }

        return null;
    }

    public IIcon icon(){
        return iconFor(this);
    }

    public static IIcon iconFor(Presentation p){

        switch (p){
            case CAPSULES:
                return PresentationsTypeface.Icon.ic_capsule;
            case DROPS:
                return PresentationsTypeface.Icon.ic_drops;
            case EFFERVESCENT:
                return PresentationsTypeface.Icon.ic_effervescent;
            case INHALER:
                return PresentationsTypeface.Icon.ic_inhaler;
            case INJECTIONS:
                return PresentationsTypeface.Icon.ic_injection;
            case PATCHES:
                return PresentationsTypeface.Icon.ic_patch;
            case POMADE:
                return PresentationsTypeface.Icon.ic_cream;
            case SPRAY:
                return PresentationsTypeface.Icon.ic_spray;
            case SYRUP:
                return PresentationsTypeface.Icon.ic_syrup;
            case PILLS:
                return PresentationsTypeface.Icon.ic_pill;
            default:
                return CommunityMaterial.Icon.cmd_help_circle;

        }


    }
}
