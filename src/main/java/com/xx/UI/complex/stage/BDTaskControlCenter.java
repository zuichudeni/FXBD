package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;

public class BDTaskControlCenter extends BDControl {
    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDTaskControlCenterSkin(this);
    }
}
