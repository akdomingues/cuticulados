package org.cuticulados.pm.ui.theme;

import javax.swing.border.*;
import java.awt.*;

public final class AppBorders {

    private AppBorders() {
    }

    // BORDAS

    public static final Border LINE_BORDER =
            new LineBorder(
                    AppColors.BORDA,
                    1,
                    true
            );

    public static Border cardBorder() {

        return new CompoundBorder(
                new LineBorder(
                        AppColors.BORDA,
                        1,
                        false
                ),
                new EmptyBorder(
                        12,
                        14,
                        12,
                        14
                )
        );
    }

    public static Border paddingBorder(
            int top,
            int left,
            int bottom,
            int right) {

        return new EmptyBorder(
                top,
                left,
                bottom,
                right
        );
    }
}