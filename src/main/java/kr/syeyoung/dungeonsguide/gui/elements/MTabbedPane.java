package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MTabbedPane extends MPanel {

    private Map<String, MPanel> tabs = new HashMap<String, MPanel>();
    private Map<String, MTabButton> buttons = new HashMap<String, MTabButton>();

    @Getter
    @Setter
    private String selectedKey = "";

    @Getter
    private Color background2;

    public void setBackground2(Color background2) {
        this.background2 = background2;
        for (MPanel value : tabs.values()) {
            value.setBackgroundColor(background2);
        }
        for (MTabButton value : buttons.values()) {
            value.setBackgroundColor(background2.brighter());
        }
    }

    public void addTab(String tab, MPanel panel) {
        MPanel panel2 = new MPanel() ;
        panel2.add(panel);
        panel2.setBackgroundColor(background2);
        tabs.put(tab, panel2);
        panel2.setBounds(new Rectangle(0,15,getBounds().width, getBounds().height-15));

        MTabButton button = new MTabButton(this, tab);
        button.setBackgroundColor(background2.brighter());
        button.setBounds(new Rectangle(buttons.size()* 50, 0, 50, 15));
        buttons.put(tab, button);
        if (tabs.size() == 1)
            selectedKey = tab;
    }

    @Override
    public List<MPanel> getChildComponents() {
        ArrayList<MPanel> dynamic = new ArrayList<MPanel>(buttons.values());
        dynamic.add(tabs.get(selectedKey));
        return dynamic;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
        for (MPanel ma:tabs.values())
            ma.setBounds(new Rectangle(0,15,parentWidth, parentHeight-15));
    }

    @Override
    public void setBounds(Rectangle bounds) {
        if (bounds == null) return;
        this.bounds.x = bounds.x;
        this.bounds.y = bounds.y;
        this.bounds.width = bounds.width;
        this.bounds.height = bounds.height;
    }

    @Getter
    @Setter
    public static class MTabButton extends MPanel {
        private String text;

        private Color foreground = Color.white;
        private Color hover = new Color(236, 236, 236, 64);
        private Color clicked = new Color(30,30,30,0);
        private Color selected = new Color(0,0,0,255);
        private Color disabled = new Color(0,0,0);

        private boolean enabled = true;

        private MTabbedPane tabbedPane;

        public MTabButton(MTabbedPane tabbedPane, String key) {
            this.tabbedPane = tabbedPane;
            this.text = key;
        }

        @Override
        public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
            Dimension bounds = getSize();

            Color bg = null;
            if (!enabled) {
                bg = disabled;
            } else if (tabbedPane.getSelectedKey().equals(text)) {
                bg = selected;
            } else if (new Rectangle(new Point(0,0),bounds).contains(relMousex0, relMousey0)) {
                bg = hover;
            }
            if (bg != null)
                Gui.drawRect(0,0,getBounds().width, getBounds().height, bg.getRGB());

            FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
            int width = renderer.getStringWidth(text);
            int x = (getBounds().width - width) / 2;
            int y = (getBounds().height - renderer.FONT_HEIGHT) / 2;

            renderer.drawString(text, x,y, foreground.getRGB());
        }

        @Override
        public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
            if (lastAbsClip.contains(absMouseX, absMouseY)) {
                tabbedPane.setSelectedKey(text);
            }
        }
    }
}
