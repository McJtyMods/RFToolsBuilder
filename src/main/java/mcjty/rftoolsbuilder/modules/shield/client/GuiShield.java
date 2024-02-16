package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.*;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity.*;


public class GuiShield extends GenericGuiContainer<ShieldProjectorTileEntity, GenericContainer> {
    public static final int SHIELD_WIDTH = 256;
    public static final int SHIELD_HEIGHT = 224;

    public static final String ACTION_PASS = "Pass";
    public static final String ACTION_SOLID = "Solid";
    public static final String ACTION_DAMAGE = "Damage";
    public static final String ACTION_SOLIDDAMAGE = "SolDmg";

    public static final String DAMAGETYPE_GENERIC = DamageTypeMode.DAMAGETYPE_GENERIC.getDescription();
    public static final String DAMAGETYPE_PLAYER = DamageTypeMode.DAMAGETYPE_PLAYER.getDescription();

    private EnergyBar energyBar;
    private ChoiceLabel shieldTextures;
    private ChoiceLabel visibilityOptions;
    private ChoiceLabel actionOptions;
    private ChoiceLabel typeOptions;
    private ChoiceLabel damageType;
    private WidgetList filterList;
    private TextField player;
    private Button addFilter;
    private Button delFilter;
    private Button upFilter;
    private Button downFilter;
    private ColorSelector colorSelector;

    // A copy of the filterList we're currently showing.
    private List<ShieldFilter> filters = null;
    private int listDirty = 0;

    private static List<ShieldFilter> fromServer_filters = new ArrayList<>();
    public static void storeFiltersForClient(List<ShieldFilter> filters) {
        fromServer_filters = new ArrayList<>(filters);
    }

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsBuilder.MODID, "textures/gui/shieldprojector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");

    public GuiShield(ShieldProjectorTileEntity shieldTileEntity, GenericContainer container, Inventory inventory) {
        super(shieldTileEntity, container, inventory, ShieldModule.SHIELD_BLOCK1.get().getManualEntry());

        imageWidth = SHIELD_WIDTH;
        imageHeight = SHIELD_HEIGHT;
    }

    public static void register() {
        register(ShieldModule.CONTAINER_SHIELD.get(), GuiShield::new);
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar().vertical().hint(12, 141, 10, 76).showText(false);

        initVisibilityMode();
        initShieldTextures();
        initActionOptions();
        initTypeOptions();
        ImageChoiceLabel redstoneMode = initRedstoneMode();
        initDamageType();

        filterList = new WidgetList().name("filters").desiredHeight(120).
                event(new DefaultSelectionEvent() {
                    @Override
                    public void select(int index) {
                        selectFilter();
                    }
                });
        Slider filterSlider = new Slider().vertical().scrollableName("filters").desiredWidth(11).desiredHeight(120);
        Panel filterPanel = horizontal(3, 1)
                .hint(12, 10, 154, 124).children(filterList, filterSlider)
                .filledBackground(0xff9e9e9e);

        colorSelector = new ColorSelector()
                .name("color")
                .tooltips("Color for the shield")
                .hint(25, 177, 30, 16);

        ToggleButton light = new ToggleButton().name("light").checkMarker(true).text("L").tooltips("If pressed, light is blocked", "by the shield")
                .hint(56, 177, 23, 16);

        player = textfield(170, 44, 80, 14).tooltips("Optional player name");

        addFilter = button(4, 6, 36, 14, "Add").channel("addfilter").tooltips("Add selected filter");
        delFilter = button(39, 6, 36, 14, "Del").channel("delfilter").tooltips("Delete selected filter");
        upFilter = button(4, 22, 36, 14, "Up").channel("upfilter").tooltips("Move filter up");
        downFilter = button(39, 22, 36, 14, "Down").channel("downfilter").tooltips("Move filter down");

        Panel controlPanel = positional().hint(170, 58, 80, 43)
                .children(addFilter, delFilter, upFilter, downFilter)
                .filledRectThickness(-2)
                .filledBackground(StyleConfig.colorListBackground);

        Label lootingBonus = label(160, 118, 60, 18, "Looting:").horizontalAlignment(HorizontalAlignment.ALIGN_RIGHT);
        lootingBonus.tooltips("Insert dimensional shards", "for looting bonus");

        Panel toplevel = positional().background(iconLocation).children(energyBar,
                visibilityOptions, shieldTextures, redstoneMode, filterPanel, actionOptions,
                typeOptions, player, controlPanel, damageType,
                colorSelector, lootingBonus, light);
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);

        window = new Window(this, toplevel);

        window.bind(RFToolsBuilderMessages.INSTANCE, "redstone", tileEntity, GenericTileEntity.VALUE_RSMODE.name());
        window.bind(RFToolsBuilderMessages.INSTANCE, "visibility", tileEntity, ShieldProjectorTileEntity.VALUE_SHIELDVISMODE.key().name());
        window.bind(RFToolsBuilderMessages.INSTANCE, "shieldtextures", tileEntity, ShieldProjectorTileEntity.VALUE_SHIELDTEXTURE.key().name());
        window.bind(RFToolsBuilderMessages.INSTANCE, "damage", tileEntity, ShieldProjectorTileEntity.VALUE_DAMAGEMODE.key().name());
        window.bind(RFToolsBuilderMessages.INSTANCE, "color", tileEntity, ShieldProjectorTileEntity.VALUE_COLOR.key().name());
        window.bind(RFToolsBuilderMessages.INSTANCE, "light", tileEntity, ShieldProjectorTileEntity.VALUE_LIGHT.key().name());
        window.event("addfilter", (source, params) -> addNewFilter());
        window.event("delfilter", (source, params) -> removeSelectedFilter());
        window.event("upfilter", (source, params) -> moveFilterUp());
        window.event("downfilter", (source, params) -> moveFilterDown());

        listDirty = 0;
        requestFilters();
    }

    private void selectFilter() {
        int selected = filterList.getSelected();
        if (selected != -1) {
            ShieldFilter shieldFilter = filters.get(selected);
            boolean solid = (shieldFilter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
            boolean damage = (shieldFilter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0;
            if (solid && damage) {
                actionOptions.choice(ACTION_SOLIDDAMAGE);
            } else if (solid) {
                actionOptions.choice(ACTION_SOLID);
            } else if (damage) {
                actionOptions.choice(ACTION_DAMAGE);
            } else {
                actionOptions.choice(ACTION_PASS);
            }
            String type = shieldFilter.getFilterName();
            if (DefaultFilter.DEFAULT.equals(type)) {
                typeOptions.choice("All");
            } else if (AnimalFilter.ANIMAL.equals(type)) {
                typeOptions.choice("Passive");
            } else if (HostileFilter.HOSTILE.equals(type)) {
                typeOptions.choice("Hostile");
            } else if (PlayerFilter.PLAYER.equals(type)) {
                typeOptions.choice("Player");
            } else if (ItemFilter.ITEM.equals(type)) {
                typeOptions.choice("Item");
            }
            if (shieldFilter instanceof PlayerFilter) {
                player.text(((PlayerFilter)shieldFilter).getName());
            } else {
                player.text("");
            }
        }
    }

    private void requestFilters() {
        RFToolsBuilderMessages.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), CMD_GETFILTERS.name()));
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestFilters();
            listDirty = 20;
        }
    }

    private void populateFilters() {
        List<ShieldFilter> newFilters = new ArrayList<>(fromServer_filters);
        if (newFilters.equals(filters)) {
            return;
        }

        filters = new ArrayList<>(newFilters);
        filterList.removeChildren();
        for (ShieldFilter filter : filters) {
            String n;
            if ("player".equals(filter.getFilterName())) {
                PlayerFilter playerFilter = (PlayerFilter) filter;
                if (playerFilter.getName() == null || playerFilter.getName().isEmpty()) {
                    n = "players";
                } else {
                    n = "player " + playerFilter.getName();
                }
            } else {
                n = filter.getFilterName();
            }
            Panel panel = horizontal();
            panel.children(label(n).color(StyleConfig.colorTextInListNormal).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(85));
            String actionName;
            boolean solid = (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
            boolean damage = (filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0;
            if (solid && damage) {
                actionName = ACTION_SOLIDDAMAGE;
            } else if (solid) {
                actionName = ACTION_SOLID;
            } else if (damage) {
                actionName = ACTION_DAMAGE;
            } else {
                actionName = ACTION_PASS;
            }
            panel.children(label(actionName).color(StyleConfig.colorTextInListNormal).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
            filterList.children(panel);
        }
    }

    private void moveFilterUp() {
        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, ShieldProjectorTileEntity.CMD_UPFILTER,
                TypedMap.builder()
                        .put(PARAM_SELECTED, filterList.getSelected())
                        .build());
        listDirty = 0;
    }

    private void moveFilterDown() {
        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, ShieldProjectorTileEntity.CMD_DOWNFILTER,
                TypedMap.builder()
                        .put(PARAM_SELECTED, filterList.getSelected())
                        .build());
        listDirty = 0;
    }

    private void addNewFilter() {
        String actionName = actionOptions.getCurrentChoice();
        int action;
        if (ACTION_PASS.equals(actionName)) {
            action = ShieldFilter.ACTION_PASS;
        } else if (ACTION_SOLID.equals(actionName)) {
            action = ShieldFilter.ACTION_SOLID;
        } else if (ACTION_SOLIDDAMAGE.equals(actionName)) {
            action = ShieldFilter.ACTION_DAMAGE + ShieldFilter.ACTION_SOLID;
        } else {
            action = ShieldFilter.ACTION_DAMAGE;
        }

        String filterName = typeOptions.getCurrentChoice();
        String type;
        if ("All".equals(filterName)) {
            type = DefaultFilter.DEFAULT;
        } else if ("Passive".equals(filterName)) {
            type = AnimalFilter.ANIMAL;
        } else if ("Hostile".equals(filterName)) {
            type = HostileFilter.HOSTILE;
        } else if ("Item".equals(filterName)) {
            type = ItemFilter.ITEM;
        } else {
            type = PlayerFilter.PLAYER;
        }

        String playerName = player.getText();
        int selected = filterList.getSelected();

        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, ShieldProjectorTileEntity.CMD_ADDFILTER,
                TypedMap.builder()
                        .put(PARAM_ACTION, action)
                        .put(PARAM_TYPE, type)
                        .put(PARAM_PLAYER, playerName)
                        .put(PARAM_SELECTED, filterList.getSelected())
                        .build());
        listDirty = 0;
    }

    private void removeSelectedFilter() {
        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, ShieldProjectorTileEntity.CMD_DELFILTER,
                TypedMap.builder()
                        .put(PARAM_SELECTED, filterList.getSelected())
                        .build());
        listDirty = 0;
    }

    private ImageChoiceLabel initRedstoneMode() {
        ImageChoiceLabel redstoneMode = new ImageChoiceLabel().
                name("redstone").
                choice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                choice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                choice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.hint(62, 200, 16, 16);
        redstoneMode.setCurrentChoice(tileEntity.getRSMode().ordinal());
        return redstoneMode;
    }

    private void initVisibilityMode() {
        visibilityOptions = new ChoiceLabel()
                .name("visibility")
                .hint(25, 161, 54, 14);
        for (ShieldRenderingMode m : ShieldRenderingMode.values()) {
            if ((!ShieldConfiguration.allowInvisibleShield.get()) && m == ShieldRenderingMode.INVISIBLE) {
                continue;
            }
            visibilityOptions.choices(m.getDescription());
        }
        if (ShieldConfiguration.allowInvisibleShield.get()) {
            visibilityOptions.choiceTooltip(ShieldRenderingMode.INVISIBLE.getDescription(), "Shield is completely invisible");
        }
        visibilityOptions.choiceTooltip(ShieldRenderingMode.SHIELD.getDescription(), "Default shield texture");
        visibilityOptions.choiceTooltip(ShieldRenderingMode.TRANSP.getDescription(), "Transparent shield texture");
        visibilityOptions.choiceTooltip(ShieldRenderingMode.SOLID.getDescription(), "Solid shield texture");
        visibilityOptions.choiceTooltip(ShieldRenderingMode.MIMIC.getDescription(), "Use the texture from the supplied block");
    }

    private void initShieldTextures() {
        shieldTextures = new ChoiceLabel()
                .name("shieldtextures")
                .hint(45, 143, 34, 14);
        for (ShieldTexture m : ShieldTexture.values()) {
            shieldTextures.choices(m.getDescription());
        }
    }

    private void initActionOptions() {
        actionOptions = new ChoiceLabel().hint(170, 12, 80, 14);
        actionOptions.choices(ACTION_PASS, ACTION_SOLID, ACTION_DAMAGE, ACTION_SOLIDDAMAGE);
        actionOptions.choiceTooltip(ACTION_PASS, "Entity that matches this filter", "can pass through");
        actionOptions.choiceTooltip(ACTION_SOLID, "Entity that matches this filter", "cannot pass");
        actionOptions.choiceTooltip(ACTION_DAMAGE, "Entity that matches this filter", "can pass but gets damage");
        actionOptions.choiceTooltip(ACTION_SOLIDDAMAGE, "Entity that matches this filter", "cannot pass and gets damage");
    }

    private void initTypeOptions() {
        typeOptions = new ChoiceLabel().hint(170, 28, 80, 14);
        typeOptions.choices("All", "Passive", "Hostile", "Item", "Player");
        typeOptions.choiceTooltip("All", "Matches everything");
        typeOptions.choiceTooltip("Passive", "Matches passive mobs");
        typeOptions.choiceTooltip("Hostile", "Matches hostile mobs");
        typeOptions.choiceTooltip("Item", "Matches items");
        typeOptions.choiceTooltip("Player", "Matches players", "(optionally named)");
    }

    private void initDamageType() {
        damageType = new ChoiceLabel()
                .name("damage")
                .hint(170, 102, 80, 14);
        damageType.choices(DAMAGETYPE_GENERIC, DAMAGETYPE_PLAYER);
        damageType.choiceTooltip(DAMAGETYPE_GENERIC, "Generic damage type");
        damageType.choiceTooltip(DAMAGETYPE_PLAYER, "Damage as done by a player");
    }

    private void enableButtons() {
        int sel = filterList.getSelected();
        int cnt = filterList.getMaximum();
        delFilter.enabled(sel != -1 && cnt > 0);
        upFilter.enabled(sel > 0 && cnt > 0);
        downFilter.enabled(sel < cnt-1 && sel != -1 && cnt > 0);
        if (sel == -1) {
            addFilter.text("Add");
        } else {
            addFilter.text("Ins");
        }
        player.enabled("Player".equals(typeOptions.getCurrentChoice()));
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float v, int i, int i2) {
        requestListsIfNeeded();
        populateFilters();
        enableButtons();
        drawWindow(graphics);
        colorSelector.currentColor(tileEntity.getShieldColor());
        updateEnergyBar(energyBar);
    }
}