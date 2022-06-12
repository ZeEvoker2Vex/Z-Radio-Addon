package fr.zeevoker2vex.radio.client.gui;

import com.google.common.base.Predicate;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.client.RadioResponsePacket;
import fr.zeevoker2vex.radio.common.network.server.PlayerConnectRadioPacket;
import fr.zeevoker2vex.radio.common.network.server.PlayerDisconnectRadioPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class RadioGui extends GuiScreen {

    public short frequency = 1;

    /**
     * The radio ItemStack
     */
    private final ItemStack stack;
    /**
     * The field to write the frequency
     */
    public GuiTextField frequencyField;
    /**
     * The buttons to connect and disconnect to frequency
     */
    private GuiButton connectButton, disconnectButton;

    /**
     * The color for the "Mhz" text and the text input.
     */
    private final int MHZ_TEXT_COLOR = new Color(28, 90, 5).getRGB();

    /**
     * The default duration of the notification text. 1s (2Oticks) x Number of seconds x4 (:shrug:)
     */
    private final float DEFAULT_TEXT_DURATION = (20*5*4)+.0f;

    /**
     * The text for the response code divided by lines.
     */
    private List<String> responseCodeText;
    /**
     * The left time before the responseCodeText disappear. In ticks.
     */
    private float textTicks = 0.0f;
    /**
     * The color of the responseCodeText. Depends on the response.
     */
    private int responseCodeTextColor;

    /**
     * The maximum characters allowed per line.
     */
    private final int MAX_WIDTH = 170;

    public RadioGui(ItemStack stack) {
        NBTTagCompound nbt = RadioItem.getTagCompound(stack);

        if(nbt.hasKey(RadioItem.FREQUENCY_KEY_TAG)) frequency = nbt.getShort(RadioItem.FREQUENCY_KEY_TAG);
        this.stack = stack;
    }

    @Override
    public void initGui() {
        // 10 entre le field et les boutons
        this.addButton(disconnectButton = new GuiButton(0, this.width-90, this.height-80, 80, 20, I18n.format("radio.gui.disconnectButton")));
        this.addButton(connectButton = new GuiButton(1, this.width-240, this.height-80, 80, 20, I18n.format("radio.gui.connectButton")));
        this.addButton(new CustomGuiSlider(3, this.width-200, this.height-55, RadioItem.getRadioVolume(stack), 0, 100));
        // 150 centré sur un truc qui fait 50 : 100 qui dépasse : 50 de chaque côté donc -120-50 = -170

        frequencyField = new GuiTextField(2, this.fontRenderer, this.width-150, this.height-80, 50, 20);
        Predicate<String> frequencyValidator = string -> {
            if(string.matches("(\\d)*")){
                if(string.length()==0) return true;
                int f = Integer.parseInt(string);
                return f>=1 && f<=1000;
            }
            return false;
        };
        frequencyField.setMaxStringLength(4);
        frequencyField.setValidator(frequencyValidator);
        frequencyField.setText(Short.toString(this.frequency));
        frequencyField.setTextColor(MHZ_TEXT_COLOR);
        frequencyField.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(frequencyField==null) { // Fix #3
            this.mc.setIngameFocus();
            Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation("radio.gui.nullInput"), true);
            return;
        }
        frequencyField.drawTextBox();
        drawString(this.fontRenderer, "Mhz", this.width-120, this.height-83+fontRenderer.FONT_HEIGHT, MHZ_TEXT_COLOR);

        if(textTicks>0){
            float alpha = Math.max(0.0f, Math.min(1.0f, textTicks / DEFAULT_TEXT_DURATION)) * 255;
            int newAlpha = (int) alpha;
            if(newAlpha > 8) {
                int color = 0xffffff & this.responseCodeTextColor;
                int finalColor = color + (newAlpha << 24 & -color);//alphaColor.getRGB();

                int numberOfLines = this.responseCodeText.size();
                int localY = this.height - 80 + (numberOfLines * (-5 - fontRenderer.FONT_HEIGHT));
                int l = 0;

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                for (String line : this.responseCodeText) {
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    this.fontRenderer.drawStringWithShadow(line, (float) (this.width - 125 - this.fontRenderer.getStringWidth(line) / 2), (float) localY, finalColor);
                    localY += ++l * (5 + fontRenderer.FONT_HEIGHT);
                }
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
            if(--textTicks<=0) textTicks = 0;
        }
        //GlStateManager.color(1, 1, 1, 1F);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Show a text notification for the response code.
     * @param responseCode A ResponseCode
     */
    public void showResponse(RadioResponsePacket.ResponseCode responseCode){
        String text = I18n.format(responseCode.getUnlocalizedText());
        this.responseCodeText = GuiUtils.splitString(text, MAX_WIDTH, mc);
        this.responseCodeTextColor = responseCode.getTextColor();
        this.textTicks = DEFAULT_TEXT_DURATION;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id== connectButton.id) NetworkHandler.getInstance().getNetwork().sendToServer(new PlayerConnectRadioPacket(frequencyField.getText()));
        else if(button.id== disconnectButton.id) NetworkHandler.getInstance().getNetwork().sendToServer(new PlayerDisconnectRadioPacket((byte)0));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.frequencyField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.frequencyField.textboxKeyTyped(typedChar, keyCode);

        if(keyCode == 15) this.frequencyField.setFocused(true);
    }

    @Override
    public void updateScreen() {
        this.frequencyField.updateCursorCounter();
    }
}