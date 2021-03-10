package kr.syeyoung.dungeonsguide.gui.elements;

import com.google.common.base.Function;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MNavigatingPane extends MPanel {

    private Map<String, MPanel> pages = new HashMap<String, MPanel>();
    private List<MTabButton> bookMarks = new ArrayList<MTabButton>();

    @Getter
    @Setter
    private Function<String, MPanel> pageGenerator;

    @Getter
    private String currentPage = "";

    public void setCurrentPage(String currentPage) {
        this.history.push(this.currentPage);
        this.currentPage = currentPage;
    }

    @Getter
    private Color background2;

    private Stack<String> history = new Stack<String>();

    private MButton back = new MButton();

    public MNavigatingPane() {
        back.setText("<");
        back.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                if (history.size() > 0)
                    currentPage = history.pop();
            }
        });
        back.setBackgroundColor(Color.darkGray);
        back.setBounds(new Rectangle(3,18,12,12));
        add(back);
    }

    public void setBackground2(Color background2) {
        this.background2 = background2;
        for (MPanel value : pages.values()) {
            value.setBackgroundColor(background2);
        }
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {

        RenderUtils.drawRectSafe(0, 15, getBounds().width, getBounds().height, 0xFF444444);
        RenderUtils.drawRectSafe(1, 16, getBounds().width-1, getBounds().height-1, background2 != null ? background2.getRGB() : 0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        fr.drawString(currentPage.replace(".", " > "), 20, 20, 0xFFFFFFFF);

    }

    public void addBookmark(String name, String addr) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        MTabButton button = new MTabButton(this, name, addr);
        int totalX = 0;
        for (MTabButton button1:bookMarks)
            totalX += button1.getBounds().width;
        button.setBounds(new Rectangle(totalX, 0, Math.max(25, fr.getStringWidth(name) + 6), 15));
        bookMarks.add(button);
        if (currentPage.isEmpty())
            currentPage = addr;
    }
    public void addBookmarkRunnable(String name, final Runnable toRun) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        MTabButton button = new MTabButton(this, name, null) {
            @Override
            public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
                if (lastAbsClip.contains(absMouseX, absMouseY)) {
                    toRun.run();
                }
            }
        };
        int totalX = 0;
        for (MTabButton button1:bookMarks)
            totalX += button1.getBounds().width;
        button.setBounds(new Rectangle(totalX, 0, Math.max(25, fr.getStringWidth(name) + 6), 15));
        bookMarks.add(button);
    }

    @Override
    public List<MPanel> getChildComponents() {
        ArrayList<MPanel> dynamic = new ArrayList<MPanel>(bookMarks);
        if (!pages.containsKey(currentPage)) {
            MPanel panel = pageGenerator.apply(currentPage);
            MPanel panel2 = new MPanel() ;
            if (panel != null)
                panel2.add(panel);
            panel2.setBackgroundColor(background2);
            pages.put(currentPage, panel2);
            panel2.setBounds(new Rectangle(1,30,getBounds().width-2, getBounds().height-31));
        }
        dynamic.add(pages.get(currentPage));
        dynamic.add(back);
        return dynamic;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
        for (MPanel ma:pages.values())
            ma.setBounds(new Rectangle(1,30,getBounds().width-2, getBounds().height-31));
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
        private String address;

        private Color foreground = Color.white;
        private Color hover = new Color(154, 154, 154, 255);
        private Color clicked = new Color(88, 88, 88,255);
        private Color selected = new Color(111, 111, 111,255);
        private Color disabled = new Color(0,0,0);

        private boolean enabled = true;

        private MNavigatingPane tabbedPane;

        public MTabButton(MNavigatingPane tabbedPane, String key, String address) {
            this.tabbedPane = tabbedPane;
            this.text = key;
            this.address = address;
        }

        @Override
        public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
            Dimension bounds = getSize();

            Color bg = null;
            if (!enabled) {
                bg = disabled;
            } else if (tabbedPane.getCurrentPage().equals(address)) {
                bg = selected;
            } else if (new Rectangle(new Point(0,0),bounds).contains(relMousex0, relMousey0)) {
                bg = hover;
            }
            RenderUtils.drawRectSafe(0, tabbedPane.getCurrentPage().equals(address) ? 0 : 2, getBounds().width, getBounds().height, 0xFF444444);
            if (bg != null)
                RenderUtils.drawRectSafe(1,tabbedPane.getCurrentPage().equals(address) ? 1 : 3,getBounds().width - 1, getBounds().height, bg.getRGB());

            FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
            int width = renderer.getStringWidth(text);
            int x = (getBounds().width - width) / 2;
            int y = (getBounds().height - 3 - renderer.FONT_HEIGHT) / 2 + 3;

            renderer.drawString(text, x,y, foreground.getRGB());
        }

        @Override
        public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
            if (lastAbsClip.contains(absMouseX, absMouseY)) {
                tabbedPane.setCurrentPage(address);
            }
        }
    }
}
