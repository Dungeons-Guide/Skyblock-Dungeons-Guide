package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
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

    private final Map<String, MPanel> tabs = new HashMap<String, MPanel>();
    private final Map<String, MTabButton> buttons = new HashMap<String, MTabButton>();

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

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Gui.drawRect(0, 15, getBounds().width, getBounds().height, 0xFF444444);
    }

    public void addTab(String tab, MPanel panel) {
        MPanel panel2 = new MPanel() ;
        panel2.add(panel);
        panel2.setBackgroundColor(background2);
        tabs.put(tab, panel2);
        panel2.setBounds(new Rectangle(1,16,getBounds().width-2, getBounds().height-17));

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        MTabButton button = new MTabButton(this, tab);
        int totalX = 0;
        for (MTabButton button1:buttons.values())
            totalX += button1.getBounds().width;
        button.setBounds(new Rectangle(totalX, 0, Math.max(25, fr.getStringWidth(tab) + 6), 15));
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
            ma.setBounds(new Rectangle(1,16,getBounds().width-2, getBounds().height-17));
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
        private Color hover = new Color(154, 154, 154, 255);
        private Color clicked = new Color(88, 88, 88,255);
        private Color selected = new Color(111, 111, 111,255);
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
            Gui.drawRect(0, tabbedPane.getSelectedKey().equals(text) ? 0 : 2, getBounds().width, getBounds().height, 0xFF444444);
            if (bg != null)
                Gui.drawRect(1,tabbedPane.getSelectedKey().equals(text) ? 1 : 3,getBounds().width - 1, getBounds().height, bg.getRGB());

            FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
            int width = renderer.getStringWidth(text);
            int x = (getBounds().width - width) / 2;
            int y = (getBounds().height - 3 - renderer.FONT_HEIGHT) / 2 + 3;

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
