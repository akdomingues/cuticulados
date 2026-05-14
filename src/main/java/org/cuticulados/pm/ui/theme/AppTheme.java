package org.cuticulados.pm.ui.theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class AppTheme {

    private AppTheme() {
    }

    // BOTAO PRIMARIO

    public static void stylePrimaryButton(
            JButton btn,
            boolean small) {

        btn.setFont(AppFonts.BUTTON);

        btn.setBackground(AppColors.ROSA);

        btn.setForeground(Color.WHITE);

        btn.setFocusPainted(false);

        btn.setBorderPainted(false);

        btn.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        btn.setOpaque(true);

        int height =
                small
                        ? AppDimensions.SMALL_BUTTON_HEIGHT
                        : AppDimensions.BUTTON_HEIGHT;

        int horizontalPadding =
                small
                        ? 10
                        : 14;

        btn.setBorder(
                new EmptyBorder(
                        0,
                        horizontalPadding,
                        0,
                        horizontalPadding
                )
        );

        btn.setPreferredSize(
                new Dimension(
                        btn.getPreferredSize().width,
                        height
                )
        );

        btn.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {

                        btn.setBackground(
                                AppColors.ROSA_CLARO
                        );
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {

                        btn.setBackground(
                                AppColors.ROSA
                        );
                    }
                }
        );
    }

    // CONTORNO DO BOTÃO

    public static void styleOutlineButton(
            JButton btn,
            boolean small) {

        btn.setFont(AppFonts.BUTTON);

        btn.setBackground(AppColors.FUNDO_CARD);

        btn.setForeground(AppColors.TEXTO_CORPO);

        btn.setFocusPainted(false);

        btn.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        btn.setOpaque(true);

        int horizontalPadding =
                small
                        ? 8
                        : 12;

        btn.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                AppColors.BORDA,
                                1,
                                false
                        ),
                        new EmptyBorder(
                                0,
                                horizontalPadding,
                                0,
                                horizontalPadding
                        )
                )
        );

        int height =
                small
                        ? AppDimensions.SMALL_BUTTON_HEIGHT
                        : AppDimensions.BUTTON_HEIGHT;

        btn.setPreferredSize(
                new Dimension(
                        btn.getPreferredSize().width,
                        height
                )
        );

        btn.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {

                        btn.setBackground(
                                AppColors.BOTAO_OUTLINE_FUNDO
                        );
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {

                        btn.setBackground(
                                AppColors.FUNDO_CARD
                        );
                    }
                }
        );
    }
}