package com.xx.UI.complex.BDTabPane;

public record BDTabItemChild(BDTabItem first, BDTabItem second) {
    public BDTabItemChild {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Both child items must be non-null");
        }
    }
}
