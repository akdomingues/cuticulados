package org.cuticulados.pm.ui.theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public final class ThemeManager {

    private ThemeManager() {
    }

    // DEFINE TEMA GLOBAL (CHAMAR ELE PARA PREDEFINIR FACILMENTE)

    public static void applyGlobalTheme() {

        applyLookAndFeel();

        applyUIManager();
    }

    private static void applyLookAndFeel() {

        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception ignored) {

        }
    }

    private static void applyUIManager() {

        UIManager.put(
                "Panel.background",
                AppColors.FUNDO_APP
        );

        UIManager.put(
                "OptionPane.background",
                AppColors.FUNDO_CARD
        );

        UIManager.put(
                "Table.gridColor",
                AppColors.BORDA
        );

        UIManager.put(
                "Table.background",
                AppColors.FUNDO_CARD
        );

        UIManager.put(
                "Table.alternateRowColor",
                AppColors.FUNDO_LINHA_ALTERNADA
        );

        UIManager.put(
                "Table.selectionBackground",
                AppColors.ROSA_PALIDO
        );

        UIManager.put(
                "Table.selectionForeground",
                AppColors.TEXTO_TITULO
        );

        UIManager.put(
                "ScrollPane.border",
                AppBorders.LINE_BORDER
        );

        UIManager.put(
                "TextField.border",
                new CompoundBorder(
                        new LineBorder(
                                AppColors.BORDA,
                                1,
                                false
                        ),
                        new EmptyBorder(
                                4,
                                8,
                                4,
                                8
                        )
                )
        );

        UIManager.put(
                "PasswordField.border",
                new CompoundBorder(
                        new LineBorder(
                                AppColors.BORDA,
                                1,
                                false
                        ),
                        new EmptyBorder(
                                4,
                                8,
                                4,
                                8
                        )
                )
        );
    }
}