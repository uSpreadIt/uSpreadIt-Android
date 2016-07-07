package it.uspread.android.data.type;

/**
 * Couleur de texte disponible.
 *
 * @author Lone Décosterd,
 */
public enum TextColor {
    BLACK("#000000"),
    WHITE("#FFFAFA"),
    GREY("#808080"),
    RED("#F70000"),
    BROWN("#B05F3C"),
    ORANGE("#FF800D"),
    YELLOW("#F7DE00"),
    GREEN("#1FCB4A"),
    BLUE("#01FCEF"),
    PURPLE("#74138C"),
    PINK("#FF69B4");

    /** Notation HTML de la couleur */
    private final String htmlColor;

    /**
     * Constructeur
     *
     * @param htmlColor
     *         {@link #htmlColor}
     */
    private TextColor(final String htmlColor) {
        this.htmlColor = htmlColor;
    }

    /**
     * @return {@link #htmlColor}
     */
    public String getHtmlColor() {
        return htmlColor;
    }

    /**
     * Permet d'obtenir la couleur suivante
     *
     * @param htmlColor
     *         {@link #htmlColor}
     * @return la couleur
     */
    public static BackgroundColor getNextColor(final String htmlColor) {
        final BackgroundColor[] backgroundColors = BackgroundColor.values();
        if (!backgroundColors[backgroundColors.length - 1].getHtmlColor().equals(htmlColor)) {
            for (int i = 0; i < backgroundColors.length - 1; i++) {
                if (backgroundColors[i].getHtmlColor().equals(htmlColor)) {
                    return backgroundColors[i + 1];
                }
            }
        }
        return backgroundColors[0];
    }

    /**
     * Permet d'obtenir la couleur précédente
     *
     * @param htmlColor
     *         {@link #htmlColor}
     * @return la couleur
     */
    public static BackgroundColor getPreviousColor(final String htmlColor) {
        final BackgroundColor[] backgroundColors = BackgroundColor.values();
        if (!backgroundColors[0].getHtmlColor().equals(htmlColor)) {
            for (int i = 1; i < backgroundColors.length; i++) {
                if (backgroundColors[i].getHtmlColor().equals(htmlColor)) {
                    return backgroundColors[i - 1];
                }
            }
        }
        return backgroundColors[backgroundColors.length - 1];
    }
}
