package fr.zeevoker2vex.radio.client.gui;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class GuiUtils {

    /**
     * Split the text in lines. Each line has a max width of maxWidth.
     * @param text The desired text to split
     * @param maxWidth The max width of a line
     * @param mc The minecraft instance
     * @return The lines in a List
     */
    public static List<String> splitString(String text, int maxWidth, Minecraft mc) {
        List<String> lines = new ArrayList<>();

        String[] spliced = text.split(" ");

        String currentLine = "";
        for(String word : spliced){
            if(!currentLine.equals("")) currentLine += " ";
            String localLine = currentLine+word;
            // Si la ligne + le mot ça dépasse, alors on ajoute la ligne et on en créé une nouvelle commençant par le mot. Sinon on ajoute juste le mot à la ligne.
            if(mc.fontRenderer.getStringWidth(localLine) >= maxWidth) {
                lines.add(currentLine);
                currentLine = word;
            }
            else currentLine += word;
        }
        if(currentLine.replaceAll(" ", "").length() > 0) {
            lines.add(currentLine);
        }
        return lines;
    }
}