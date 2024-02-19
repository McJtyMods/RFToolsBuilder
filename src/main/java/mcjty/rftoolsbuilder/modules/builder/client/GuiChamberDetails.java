package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.rftoolsbuilder.setup.CommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static mcjty.lib.gui.widgets.Widgets.*;

public class GuiChamberDetails extends GuiItemScreen {

    private static final int CHAMBER_XSIZE = 390;
    private static final int CHAMBER_YSIZE = 210;

    private static Map<BlockState, Integer> items = null;
    private static Map<BlockState, Integer> costs = null;
    private static Map<BlockState, ItemStack> stacks = null;
    private static Map<String, Integer> entities = null;
    private static Map<String, Integer> entityCosts = null;
    private static Map<String, Entity> realEntities = null;
    private static Map<String, String> playerNames = null;

    private WidgetList blockList;
    private Label infoLabel;
    private Label info2Label;

    public GuiChamberDetails() {
        super(CHAMBER_XSIZE, CHAMBER_YSIZE,  /* @todo 1.14 GuiProxy.GUI_MANUAL_SHAPE*/ ManualEntry.EMPTY);
        requestChamberInfoFromServer();
    }

    public static void setItemsWithCount(Map<BlockState, Integer> items, Map<BlockState, Integer> costs,
                                         Map<BlockState, ItemStack> stacks,
                                         Map<String, Integer> entities, Map<String, Integer> entityCosts,
                                         Map<String, Entity> realEntities,
                                         Map<String, String> playerNames) {
        GuiChamberDetails.items = new HashMap<>(items);
        GuiChamberDetails.costs = new HashMap<>(costs);
        GuiChamberDetails.stacks = new HashMap<>(stacks);
        GuiChamberDetails.entities = new HashMap<>(entities);
        GuiChamberDetails.entityCosts = new HashMap<>(entityCosts);
        GuiChamberDetails.realEntities = new HashMap<>(realEntities);
        GuiChamberDetails.playerNames = new HashMap<>(playerNames);
    }

    private void requestChamberInfoFromServer() {
        RFToolsBuilderMessages.sendToServer(CommandHandler.CMD_GET_CHAMBER_INFO);
    }

    @Override
    public void init() {
        super.init();

        blockList = new WidgetList().name("blocks");
        Slider listSlider = new Slider().desiredWidth(10).vertical().scrollableName("blocks");
        Panel listPanel = horizontal(3, 1).children(blockList, listSlider);

        infoLabel = new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        infoLabel.desiredWidth(380).desiredHeight(14);
        info2Label = new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        info2Label.desiredWidth(380).desiredHeight(14);

        Panel toplevel = vertical(3, 1).filledRectThickness(2).children(listPanel, infoLabel, info2Label);
        toplevel.bounds(guiLeft, guiTop, xSize, ySize);

        window = new Window(this, toplevel);
    }

    private void populateLists() {
        blockList.removeChildren();
        if (items == null) {
            return;
        }

        int totalCost = 0;
        for (Map.Entry<BlockState, Integer> entry : items.entrySet()) {
            BlockState bm = entry.getKey();
            int count = entry.getValue();
            int cost = costs.get(bm);
            Panel panel = horizontal().desiredHeight(16);
            ItemStack stack;
            if (stacks.containsKey(bm)) {
                stack = stacks.get(bm);
            } else {
                stack = bm.getBlock().getCloneItemStack(Minecraft.getInstance().level, BlockPos.ZERO, bm);
                if (stack.isEmpty()) {
                    stack = new ItemStack(bm.getBlock(), 0);
                }
            }
            BlockRender blockRender = new BlockRender().renderItem(stack).offsetX(-1).offsetY(-1);

            Label nameLabel = new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(StyleConfig.colorTextInListNormal);
            stack.getItem();
            nameLabel.text(stack.getHoverName().getString()).desiredWidth(160);   // @todo getFormattedText

            Label countLabel = label(String.valueOf(count)).color(StyleConfig.colorTextInListNormal);
            countLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(50);

            Label costLabel = new Label().color(StyleConfig.colorTextInListNormal);
            costLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);

            if (cost == -1) {
                costLabel.text("NOT MOVABLE!");
            } else {
                costLabel.text("Move Cost " + cost + " RF");
                totalCost += cost;
            }
            panel.children(blockRender, nameLabel, countLabel, costLabel);
            blockList.children(panel);
        }

        int totalCostEntities = 0;
        RenderHelper.rot += .5f;
        for (Map.Entry<String, Integer> entry : entities.entrySet()) {
            String className = entry.getKey();
            int count = entry.getValue();
            int cost = entityCosts.get(className);
            Panel panel = horizontal().desiredHeight(16);

            String entityName = "<?>";
            Entity entity = null;
            if (realEntities.containsKey(className)) {
                entity = realEntities.get(className);
                entityName = entity.getDisplayName().getString();
                if (entity instanceof ItemEntity entityItem) {
                    if (!entityItem.getItem().isEmpty()) {
                        String displayName = entityItem.getItem().getDisplayName().getString();
                        entityName += " (" + displayName + ")";
                    }
                }
            } else {
                try {
                    Class<?> aClass = Class.forName(className);
                    entity = (Entity) aClass.getConstructor(Level.class).newInstance(minecraft.level);
                    entityName = aClass.getSimpleName();
                } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                }
            }

            if (playerNames.containsKey(className)) {
                entityName = playerNames.get(className);
            }

            BlockRender blockRender = new BlockRender().renderItem(entity).offsetX(-1).offsetY(-1);

            Label nameLabel = label(entityName).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(160);
            Label countLabel = label(String.valueOf(count));
            countLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(50);

            Label costLabel = new Label();
            costLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);

            if (cost == -1) {
                costLabel.text("NOT MOVABLE!");
            } else {
                costLabel.text("Move Cost " + cost + " RF");
                totalCostEntities += cost;
            }
            panel.children(blockRender, nameLabel, countLabel, costLabel);
            blockList.children(panel);
        }


        infoLabel.text("Total cost blocks: " + totalCost + " RF");
        info2Label.text("Total cost entities: " + totalCostEntities + " RF");
    }

    @Override
    protected void renderInternal(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        populateLists();

        drawWindow(graphics);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiChamberDetails());
    }
}
