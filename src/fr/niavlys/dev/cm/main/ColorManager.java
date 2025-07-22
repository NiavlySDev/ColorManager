package fr.niavlys.dev.cm.main;

import net.kyori.adventure.text.format.TextColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorManager {

    private static List<String> generateGradient(String startHex, String endHex, int steps, boolean bold, boolean italic) {
        Color startColor = Color.decode(startHex);
        Color endColor = Color.decode(endHex);
        List<String> gradient = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            Color stepColor = interpolateColor(startColor, endColor, i / (double) (steps - 1));
            TextColor chatColor = TextColor.color(stepColor.getRed(), stepColor.getGreen(), stepColor.getBlue());

            String prefix = "";
            if (bold) prefix += net.kyori.adventure.text.format.TextDecoration.BOLD.toString();
            if (italic) prefix += net.kyori.adventure.text.format.TextDecoration.ITALIC.toString();

            gradient.add(prefix + chatColor);
        }
        return gradient;
    }

    private static List<String> applyGradientToText(String text, String startHex, String endHex, boolean bold, boolean italic) {
        int textLength = text.length();
        List<String> gradient = generateGradient(startHex, endHex, textLength, bold, italic);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < textLength; i++) {
            result.add(gradient.get(i) + text.charAt(i));
        }
        return result;
    }

    private static Color interpolateColor(Color start, Color end, double fraction) {
        int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * fraction);
        int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * fraction);
        int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * fraction);
        return new Color(red, green, blue);
    }

    public static String getText(String text, Colors un, Colors deux) {
        return String.join("", applyGradientToText(text, un.getHexCode(),deux.getHexCode(), false, false));
    }
}