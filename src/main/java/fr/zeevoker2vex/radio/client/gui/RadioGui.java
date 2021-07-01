package fr.zeevoker2vex.radio.client.gui;

import com.google.common.base.Predicate;
import fr.nathanael2611.modularvoicechat.client.gui.GuiConfigSlider;
import fr.zeevoker2vex.radio.common.items.RadioItem;
import fr.zeevoker2vex.radio.common.network.NetworkHandler;
import fr.zeevoker2vex.radio.common.network.server.PlayerConfigRadioPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;
import java.io.IOException;

public class RadioGui extends GuiScreen {

    public short frequency = 1;

    public ItemStack radio;
    public GuiTextField frequencyField;

    public RadioGui(ItemStack radio) {
        NBTTagCompound nbt;
        if(!radio.hasTagCompound()) radio.setTagCompound(new NBTTagCompound());
        nbt = radio.getTagCompound();

        if(nbt.hasKey(RadioItem.FREQUENCY_KEY_TAG)){
            frequency = nbt.getShort(RadioItem.FREQUENCY_KEY_TAG);
        }
        this.radio = radio;
    }

    @Override
    public void initGui() {

        this.addButton(new GuiButton(0, this.width-60, this.height-80, 50, 20, "OFF"));
        this.addButton(new GuiButton(1, this.width-180, this.height-80, 50, 20, "Connect"));
        this.addButton(new CustomGuiSlider(3, this.width-170, this.height-55, this.radio, 0, 100));
        // 150 centré sur un truc qui fait 50 : 100 qui dépasse : 50 de chaque côté donc -120-50 = -170

        frequencyField = new GuiTextField(2, this.fontRenderer, this.width-120, this.height-80, 50, 20);
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
        frequencyField.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        frequencyField.drawTextBox();

        drawString(this.fontRenderer, "Mhz", this.width-90, this.height-83+fontRenderer.FONT_HEIGHT, new Color(28, 90, 5).getRGB());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id==0 || button.id==1){
            NetworkHandler.getInstance().getNetwork().sendToServer(new PlayerConfigRadioPacket(button.id==1, frequencyField.getText()));
        }
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

        if (keyCode == 15){
            this.frequencyField.setFocused(true);
        }
    }

    @Override
    public void updateScreen() {
        this.frequencyField.updateCursorCounter();
    }
}