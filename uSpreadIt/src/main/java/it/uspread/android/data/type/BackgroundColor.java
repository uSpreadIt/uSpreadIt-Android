package it.uspread.android.data.type;

/**
 * Couleur de fond disponible.
 *
 * @author Lone Décosterd,
 */
public enum BackgroundColor {
    BLACK("#000000"),
    WHITE("#FFFAFA"),
    GREY("#C0C0C0"),
    RED("#FF5353"),
    BROWN("#C87C5B"),
    ORANGE("#FFAC62"),
    YELLOW("#FFF06A"),
    GREEN("#4AE371"),
    BLUE("#5FFEF7"),
    PURPLE("#A41CC6"),
    PINK("#FFB6C1");

    /** Notation HTML de la couleur */
    private final String htmlColor;

    /**
     * Constructeur
     *
     * @param htmlColor
     *         {@link #htmlColor}
     */
    private BackgroundColor(final String htmlColor) {
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
