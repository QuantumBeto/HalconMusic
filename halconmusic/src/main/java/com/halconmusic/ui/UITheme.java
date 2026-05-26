package com.halconmusic.ui;

import java.awt.Color;
import java.awt.Font;

public final class UITheme {

    private UITheme() {}

    // ── Colores ──────────────────────────────────────────
    public static final Color BG          = new Color(0x0A, 0x0A, 0x0A);
    public static final Color SIDEBAR     = new Color(0x11, 0x11, 0x11);
    public static final Color SURFACE     = new Color(0x1A, 0x1A, 0x1A);
    public static final Color CARD        = new Color(0x22, 0x22, 0x22);
    public static final Color PLAYER      = new Color(0x14, 0x14, 0x14);
    public static final Color ACCENT      = new Color(0xC8, 0xA8, 0x4B);
    public static final Color ACCENT2     = new Color(0xE8, 0xC9, 0x6A);
    public static final Color TEXT        = new Color(0xF0, 0xF0, 0xF0);
    public static final Color MUTED       = new Color(0x88, 0x88, 0x88);
    public static final Color BORDER      = new Color(0xFF, 0xFF, 0xFF, 18);
    public static final Color HOVER       = new Color(0xFF, 0xFF, 0xFF, 10);
    public static final Color ACCENT_SOFT = new Color(0xC8, 0xA8, 0x4B, 30);

    // ── Fuentes ──────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,   18);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD,   15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,   10);
    public static final Font FONT_NUM     = new Font("Segoe UI", Font.BOLD,   22);

    // ── Dimensiones ──────────────────────────────────────
    public static final int SIDEBAR_W = 200;
    public static final int PLAYER_H  = 80;
    public static final int CARD_SIZE = 150;
    public static final int RADIUS    = 10;

    // ── Utilidades ───────────────────────────────────────
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**
     * Genera HTML que renderiza el emoji con "Segoe UI Emoji" (color, Windows)
     * y el texto normal con "Segoe UI".
     *
     * Uso:  label.setText(UITheme.emoji("🎵", "Género: Rock"));
     *       label.setText(UITheme.emoji("✅", "Archivo cargado"));
     *
     * @param emojiChar  El emoji (puede ser un String con el codepoint completo)
     * @param texto      El texto que va después del emoji
     */
    public static String emoji(String emojiChar, String texto) {
        return "<html>"
             + "<font face='Segoe UI Emoji'>" + emojiChar + "</font>"
             + "&nbsp;"
             + "<font face='Segoe UI'>" + texto + "</font>"
             + "</html>";
    }

    /**
     * Sobrecarga: solo emoji, sin texto adicional.
     * Útil para botones o labels que solo muestran el emoji.
     */
    public static String emoji(String emojiChar) {
        return "<html><font face='Segoe UI Emoji'>" + emojiChar + "</font></html>";
    }

    /**
     * Convierte un color Java a hex CSS para usarlo dentro de HTML de Swing.
     * Ej: toHex(UITheme.ACCENT) → "#C8A84B"
     */
    public static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}