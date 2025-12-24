package com.xx.UI.complex.splitPane;

public record BDTabItemChild(BDTabItem first, BDTabItem second) {
    public BDTabItemChild {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Both child items must be non-null");
        }
    }
}
