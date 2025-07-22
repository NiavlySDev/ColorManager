package cm.main;

public enum Colors {
    RED("#FF0000"),
    GREEN("#00FF00"),
    BLUE("#0000FF"),
    YELLOW("#FFFF00"),
    CYAN("#00FFFF"),
    WHITE("#FFFFFF"),
    BLACK("#000000"),
    GRAY("#808080"),
    DARK_GRAY("#404040"),
    LIGHT_GRAY("#C0C0C0"),
    ORANGE("#FFA500"),
    PINK("#FFC0CB"),
    PURPLE("#800080"),
    BROWN("#A52A2A"),
    LIME("#00FF00"),
    MAROON("#800000"),
    LIGHT_BLUE("#ADD8E6"),
    LIGHT_GREEN("#90EE90"),
    LIGHT_YELLOW("#FFFFE0"),
    LIGHT_PINK("#FFB6C1"),
    LIGHT_PURPLE("#D3D3D3"),
    SILVER("#C0C0C0"),
    AQUA("#00FFFF"),
    MAGENTA("#FF00FF");

    private final String hexCode;

    Colors(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }
}