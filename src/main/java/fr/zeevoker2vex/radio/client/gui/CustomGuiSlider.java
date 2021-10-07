package fr.zeevoker2vex.radio.client.gui;

import fr.nathanael2611.modularvoicechat.util.Helpers;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.server.RadioChangeVolumePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class CustomGuiSlider extends GuiButton {

    private float sliderValue;
    public boolean dragging;
    private final float minValue;
    private final float maxValue;

    public CustomGuiSlider(int buttonId, int x, int y, short volume, float minValue, float maxValue) {
        super(buttonId, x, y, 150, 20, "");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.sliderValue = (minValue + volume / maxValue);
        displayString = "Volume: "+volume+"%";
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        this.enabled = true;
        super.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);

                int val = (int) (minValue + Helpers.crossMult(sliderValue, 1, maxValue));

                displayString = "Volume: "+val+"%";
            }

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
        short val = (short) (minValue + Helpers.crossMult(sliderValue, 1, maxValue));
        NetworkHandler.getInstance().getNetwork().sendToServer(new RadioChangeVolumePacket(val));
    }
}