package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.LoaderMeta;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.branch.Update;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateBranch;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateRetrieverUtil;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.Notification;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.NotificationManager;
import kr.syeyoung.dungeonsguide.launcher.loader.JarLoader;
import kr.syeyoung.dungeonsguide.launcher.loader.LocalLoader;
import kr.syeyoung.dungeonsguide.launcher.loader.RemoteLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GuiChooseVersion extends SpecialGuiScreen {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Throwable cause;

    public GuiChooseVersion(Throwable cause) {
        this.cause = cause;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        cause.printStackTrace(printStream);
        this.stacktrace = byteArrayOutputStream.toString();

        try {
            fetchList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchList () throws IOException {
        loading++;
        executor.submit(() -> {
            try {
                branchList = UpdateRetrieverUtil.getUpdateBranches().stream()
                        .filter(updateBranch ->
                                Optional.ofNullable(updateBranch.getMetadata())
                                        .filter(a -> a.has("additionalMeta"))
                                        .map(a -> a.getJSONObject("additionalMeta"))
                                        .filter(a -> a.has("type"))
                                        .map(a -> a.getString("type")).orElse("").equals("mod"))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            smthUpdated = true;
            loading--;
        });
    }

    private void fetchUpdates(UpdateBranch branch) throws IOException {
        loading++;
        executor.submit(() -> {
            try {
                updates = UpdateRetrieverUtil.getLatestUpdates(branch.getId(), 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            smthUpdated = true;
            loading--;
        });
    }
    private final String stacktrace;

    private List<UpdateBranch> branchList = Collections.emptyList();
    private List<Update> updates = Collections.emptyList();
    private UpdateBranch current;
    private boolean smthUpdated = false;
    private int loading = 0;

    @Override
    public void initGui() {
        super.initGui();
        // Local version
        // Jar embedded version
        // Remote version
        // dg gui lib....?

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.buttonList.add(new GuiButton(2, 0,sr.getScaledHeight()-20 ,"Copy Error into Clipboard"));

        this.buttonList.add(new GuiButton(3, sr.getScaledWidth()/2-100,sr.getScaledHeight()-40 ,"Play Without DG"));
        GuiButton button;
        int tenth = sr.getScaledWidth()/10;
        this.buttonList.add(button = new GuiButton(0, tenth,90 ,"Try loading Locally (classes)"));
        button.enabled = this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/mod/DungeonsGuide.class") != null;
        this.buttonList.add(button = new GuiButton(1, tenth,110 ,"Try loading Local Jar (embedded jar)"));
        button.enabled = this.getClass().getResourceAsStream("/mod.jar") != null;
        this.buttonList.add(button = new GuiButton(4, tenth,sr.getScaledHeight()-100 ,"Refresh Options"));

        this.buttonList.add(new GuiCheckBox(5, sr.getScaledWidth() - tenth-200,sr.getScaledHeight()-100 ,"Save This Loader", false));

        int k = 0;
        for (UpdateBranch updateBranch : branchList) {
            this.buttonList.add(new GuiButton(10 + k++, tenth, 110 + 20 * k,"Remote Branch: "+ updateBranch.getName()));
        }

        k = 0;
        for (Update update : updates) {
            this.buttonList.add(button = new GuiButton(branchList.size() + 10 + k++, tenth+210, 70+ 20 * k, update.getName()));
            if (update.getMetadata().has("loaderVersion") && update.getMetadata().getInt("loaderVersion") > LoaderMeta.LOADER_VERSION) {
                button.enabled = false;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 2) {
            Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(stacktrace),
                                null
                        );

            NotificationManager.INSTANCE.updateNotification(UUID.randomUUID(), Notification.builder()
                        .title("Successfully Copied!")
                        .description("")
                        .titleColor(0xFF00FF00)
                        .build());
        } else if (button.id == 0) {
            dismiss();
            Main.getMain().tryReloadingWithSplash(new LocalLoader());
        } else if (button.id == 1) {
            dismiss();
            Main.getMain().tryReloadingWithSplash(new JarLoader());
        } else if (button.id == 3) {
            dismiss();
        } else if (button.id == 4) {
            branchList = Collections.emptyList();
            updates = Collections.emptyList();
            current = null;
            smthUpdated = true;
            fetchList();
        } else if (button.id == 5) {
            // do smt
        } else if (button.id < branchList.size() + 10) {
            int idx = button.id - 10;
            current = branchList.get(idx);
            fetchUpdates(branchList.get(idx));
        } else if (button.id < branchList.size() + 10 + updates.size()){
            int idx = button.id - branchList.size() - 10;
            Update update = updates.get(idx);

            dismiss();
            Main.getMain().tryReloadingWithSplash(new RemoteLoader(current.getName(),current.getId(),update.getId()));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (smthUpdated) {
            this.buttonList.clear();
            initGui();
            smthUpdated = false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(1);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString("Please choose a new version to load", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please choose a new version to load"))/2,(int) (40),0xFFFF0000);
        fontRenderer.drawString("Problem: "+cause.getMessage(), (sr.getScaledWidth()-fontRenderer.getStringWidth("Problem: "+cause.getMessage()))/2, (int) (40+fontRenderer.FONT_HEIGHT*1.5),0xFFFF0000);

        int tenth = sr.getScaledWidth() / 10;
        Gui.drawRect(tenth, 90,sr.getScaledWidth()-tenth, sr.getScaledHeight()-80, 0xFF5B5B5B);

        if (loading > 0) {
            fontRenderer.drawString("Loading", sr.getScaledWidth()/2, sr.getScaledHeight()/2, 0xFF000000);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
