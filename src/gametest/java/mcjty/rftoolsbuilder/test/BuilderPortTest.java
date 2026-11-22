package mcjty.rftoolsbuilder.test;

import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiComposer;
import mcjty.lib.varia.FluidStack;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import mcjty.rftoolsbuilder.modules.scanner.client.DummyBlockGetter;
import mcjty.rftoolsbuilder.shapes.ScanModels;
import mcjty.xnet.modules.cables.CableModule;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.util.RandomSource;
import mcjty.lib.network.PayloadContext;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.network.PacketOpenBuilderGui;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiScanner;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.MenuTools;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;

/** Exercise registration, save data, native transfers, menu payloads and scanner synchronization in Minecraft. */
public class BuilderPortTest implements FabricClientGameTest {
    private static final BlockPos BUILDER = new BlockPos(0, -59, 0);
    private static final BlockPos SCANNER = new BlockPos(2, -59, 0);
    private static final BlockPos SHIELD = new BlockPos(4, -59, 0);
    private static final BlockPos TANK = BUILDER.above();

    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.level != null && client.player != null);
            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                int index = 0;
                for (Block block : BuiltInRegistries.BLOCK) {
                    if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("rftoolsbuilder")) continue;
                    var pos = new BlockPos((index++ % 8) * 2, -59, 8 + (index / 8) * 2);
                    level.setBlockAndUpdate(pos, block.defaultBlockState());
                    var entity = level.getBlockEntity(pos);
                    if (entity != null) {
                        var restored = BlockEntity.loadStatic(pos, entity.getBlockState(), entity.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
                        check(restored != null && restored.getType() == entity.getType(), "Block entity saves and loads: " + block);
                    }
                }
                check(index == 27, "All 27 Builder blocks are registered");
                level.setBlockAndUpdate(BUILDER, BuilderModule.BUILDER.block().get().defaultBlockState());
                level.setBlockAndUpdate(SCANNER, ScannerModule.SCANNER.block().get().defaultBlockState());
                level.setBlockAndUpdate(SHIELD, ShieldModule.SHIELDING_SOLID.get().defaultBlockState());
                var shield = (ShieldingTileEntity) level.getBlockEntity(SHIELD);
                shield.setMimic(Blocks.GOLD_BLOCK.defaultBlockState());
                var restoredShield = (ShieldingTileEntity) BlockEntity.loadStatic(SHIELD, shield.getBlockState(), shield.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
                check(restoredShield.getMimic().is(Blocks.GOLD_BLOCK), "Shield camouflage survives save/load");
                var builder = (BuilderTileEntity) level.getBlockEntity(BUILDER);
                builder.setData(BuilderModule.BUILDER_DATA, BuilderData.DEFAULT.withSilent(true));
                var restored = (BuilderTileEntity) BlockEntity.loadStatic(BUILDER, builder.getBlockState(), builder.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
                check(restored.getData(BuilderModule.BUILDER_DATA).flags().silent(), "Builder attachment survives save/load");
                testFluids(builder, level);
                var player = server.getPlayerList().getPlayers().getFirst();
                player.teleportTo(0.5, -58, 3.5);
            });
            context.waitFor(client -> client.level.getBlockEntity(BUILDER) instanceof BuilderTileEntity
                    && client.level.getBlockEntity(SCANNER) != null);
            world.getServer().runOnServer(server -> new PacketOpenBuilderGui(BUILDER).handle(
                    new PayloadContext(server.getPlayerList().getPlayers().getFirst(), Runnable::run)));
            context.runOnClient(client -> {
                var state = CableModule.CONNECTOR.get().defaultBlockState();
                var scanned = new DummyBlockGetter(client.level.registryAccess(), Map.of(BlockPos.ZERO, state));
                var parts = new ArrayList<BlockStateModelPart>();
                ScanModels.collectParts(scanned, BlockPos.ZERO, state, RandomSource.create(42), parts);
                check(!parts.isEmpty(), "Scanned XNet connectors render without synthetic block entities");
                parts.clear();
                ScanModels.collectParts(client.level, BUILDER, client.level.getBlockState(BUILDER), RandomSource.create(42), parts);
                check(!parts.isEmpty(), "Native Fabric models provide preview geometry");
            });
            context.waitForScreen(GuiBuilder.class);
            context.waitTicks(3);
            context.setScreen(() -> null);
            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                var player = server.getPlayerList().getPlayers().getFirst();
                var provider = CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY.find(level, SCANNER, null);
                MenuTools.open(player, provider, data -> {
                    data.writeBlockPos(SCANNER);
                    ((DefaultContainerProvider<?>) provider).writeExtraData(data, level.getBlockEntity(SCANNER));
                });
            });
            context.waitForScreen(GuiScanner.class);
            context.waitTicks(3);
            context.setScreen(() -> null);
            testComposerCardEditor(context, world.getServer());
            testProjectorSynchronization(context, world.getServer());
            world.getServer().runOnServer(server -> {
                server.overworld().destroyBlock(SCANNER, false);
                server.overworld().destroyBlock(BUILDER, false);
            });
            context.waitFor(client -> client.level.getBlockState(SCANNER).isAir() && client.level.getBlockState(BUILDER).isAir());
        }
    }

    private static void testComposerCardEditor(ClientGameTestContext context, TestServerContext server) {
        BlockPos pos = new BlockPos(2, -59, 2);
        server.runOnServer(instance -> {
            var level = instance.overworld();
            level.setBlockAndUpdate(pos, ScannerModule.COMPOSER.block().get().defaultBlockState());
            var composer = (ComposerTileEntity) level.getBlockEntity(pos);
            composer.getItems().setStackInSlot(ComposerTileEntity.SLOT_TABS, new ItemStack(BuilderModule.SHAPE_CARD_DEF.get()));
            composer.getItems().setStackInSlot(ComposerTileEntity.SLOT_OUT, new ItemStack(BuilderModule.SHAPE_CARD_QUARRY.get()));
            composer.tick();
        });
        context.waitFor(client -> client.level.getBlockEntity(pos) instanceof ComposerTileEntity);
        server.runOnServer(instance -> new PacketOpenBuilderGui(pos).handle(
                new PayloadContext(instance.getPlayerList().getPlayers().getFirst(), Runnable::run)));
        context.waitForScreen(GuiComposer.class);
        context.waitFor(client -> !((GuiComposer) client.gui.screen()).getMenu()
                .getSlotByInventoryAndIndex(ContainerFactory.CONTAINER_CONTAINER, ComposerTileEntity.SLOT_TABS).getItem().isEmpty());

        context.runOnClient(client -> {
            var gui = (GuiComposer) client.gui.screen();
            clickComposerEditor(gui, 14);
        });
        context.waitForScreen(GuiShapeCard.class);
        context.runOnClient(client -> {
            var editor = (GuiShapeCard) client.gui.screen();
            TextField dimX = editorWidget(editor, "dimX", TextField.class);
            dimX.selectAll();
            check(dimX.charTyped('9'), "The Composer input card dimension is editable");
        });
        context.setScreen(() -> null);
        context.waitForScreen(GuiComposer.class);
        context.waitFor(client -> {
            var composer = (GuiComposer) client.gui.screen();
            ItemStack output = composer.getMenu().getSlot(ComposerTileEntity.SLOT_OUT).getItem();
            var children = ShapeCardItem.getChildren(output);
            return children.size() == 1 && ShapeCardItem.getDimension(children.getFirst().stack()).getX() == 9;
        });

        context.runOnClient(client -> {
            var gui = (GuiComposer) client.gui.screen();
            clickComposerEditor(gui, 208);
        });
        context.waitForScreen(GuiShapeCard.class);
        context.runOnClient(client -> {
            var editor = (GuiShapeCard) client.gui.screen();
            check(!editorWidget(editor, "shapeLabel", ChoiceLabel.class).isEnabled(), "The output shape stays a composition");
            editorWidget(editor, "solidLabel", ChoiceLabel.class).mouseClick(0, 0, 0);
            editorWidget(editor, "tagMatching", ToggleButton.class).mouseClick(0, 0, 0);
        });
        context.setScreen(() -> null);
        context.waitForScreen(GuiComposer.class);
        context.waitFor(client -> {
            var gui = (GuiComposer) client.gui.screen();
            ItemStack output = gui.getMenu().getSlot(ComposerTileEntity.SLOT_OUT).getItem();
            return ShapeCardItem.isTagMatching(output) && !ShapeCardItem.isSolid(output);
        });
        server.runOnServer(instance -> {
            var level = instance.overworld();
            var composer = (ComposerTileEntity) level.getBlockEntity(pos);
            var saved = composer.saveWithFullMetadata(level.registryAccess());
            var restored = (ComposerTileEntity) BlockEntity.loadStatic(pos, composer.getBlockState(), saved, level.registryAccess());
            ItemStack input = restored.getItems().getStackInSlot(ComposerTileEntity.SLOT_TABS);
            ItemStack output = restored.getItems().getStackInSlot(ComposerTileEntity.SLOT_OUT);
            check(ShapeCardItem.getDimension(input).getX() == 9, "The Composer input card edit survives save/load");
            check(ShapeCardItem.isTagMatching(output) && !ShapeCardItem.isSolid(output), "The Composer output edits survive save/load");
            var children = ShapeCardItem.getChildren(output);
            check(ShapeCardItem.getShape(output).isComposition() && children.size() == 1
                    && ShapeCardItem.getDimension(children.getFirst().stack()).getX() == 9, "The edited composition survives save/load");
        });
        context.setScreen(() -> null);
    }

    private static void clickComposerEditor(GuiComposer gui, int buttonY) {
        int x = gui.getPreviewLeft() + 9;
        int y = gui.getPreviewTop() + buttonY;
        var window = gui.getWindow();
        check(window.getWidgetAtPosition(x, y) instanceof Button, "Found the Composer edit button");
        window.mouseClicked(x, y, 0);
        window.mouseReleased(x, y, 0);
    }

    private static <T> T editorWidget(GuiShapeCard editor, String fieldName, Class<T> type) {
        try {
            var field = GuiShapeCard.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(editor));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void testProjectorSynchronization(ClientGameTestContext context, TestServerContext server) {
        BlockPos pos = new BlockPos(6, -59, 0);
        server.runOnServer(instance -> {
            var level = instance.overworld();
            level.setBlockAndUpdate(pos, ScannerModule.PROJECTOR.block().get().defaultBlockState());
            var projector = (ProjectorTileEntity) level.getBlockEntity(pos);
            projector.getItems().setStackInSlot(ProjectorTileEntity.SLOT_CARD, new ItemStack(BuilderModule.SHAPE_CARD_DEF.get()));
            CompoundTag settings = new CompoundTag();
            projector.saveClientDataToNBT(settings, level.registryAccess());
            settings.putBoolean("active", true);
            settings.putBoolean("rot", true);
            settings.putFloat("scale", 0.25f);
            settings.putFloat("angle", 30);
            settings.putFloat("offs", 2);
            projector.loadClientDataFromNBT(settings, level.registryAccess());
            projector.markDirtyClient();
        });
        context.waitFor(client -> client.level.getBlockEntity(pos) instanceof ProjectorTileEntity projector
                && projector.isAutoRotate() && projector.getScale() == 0.25f && projector.getVerticalOffset() == 2);
        context.waitFor(client -> {
            var projector = (ProjectorTileEntity) client.level.getBlockEntity(pos);
            return projector.isProjecting() && !projector.getRenderStack().isEmpty()
                    && !projector.getShapeRenderer().capture(projector.getRenderStack(), projector.isScanline(),
                            projector.isRenderBlockModels()).meshes().isEmpty();
        });
        float[] angle = new float[1];
        context.runOnClient(client -> angle[0] = ((ProjectorTileEntity) client.level.getBlockEntity(pos)).getAngle());
        context.waitTicks(5);
        context.runOnClient(client -> {
            var projector = (ProjectorTileEntity) client.level.getBlockEntity(pos);
            check(projector.getAngle() != angle[0], "Projector rotates between block updates");
        });
        server.runOnServer(instance -> {
            var level = instance.overworld();
            var projector = (ProjectorTileEntity) level.getBlockEntity(pos);
            CompoundTag settings = new CompoundTag();
            projector.saveClientDataToNBT(settings, level.registryAccess());
            settings.putFloat("scale", 0.5f);
            projector.loadClientDataFromNBT(settings, level.registryAccess());
            projector.markDirtyClient();
        });
        context.waitFor(client -> ((ProjectorTileEntity) client.level.getBlockEntity(pos)).getScale() == 0.5f);
        context.waitTicks(5);
        context.runOnClient(client -> check(((ProjectorTileEntity) client.level.getBlockEntity(pos)).isAutoRotate(),
                "Later projector packets preserve continuous rotation"));
    }

    private static void testFluids(BuilderTileEntity builder, Level level) {
        var water = FluidVariant.of(Fluids.WATER);
        var tank = new SingleVariantStorage<FluidVariant>() {

            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return FluidConstants.BUCKET;
            }

            @Override
            public long extract(FluidVariant resource, long amount, TransactionContext transaction) {
                return super.extract(resource, Math.min(amount, FluidConstants.BUCKET / 2), transaction);
            }
        };
        FluidStorage.SIDED.registerFallback((world, pos, state, entity, side) -> world == level && pos.equals(TANK) ? tank : null);
        try {
            Method insert = BuilderTileEntity.class.getDeclaredMethod("checkFluidTank", FluidStack.class, BlockPos.class, Direction.class);
            insert.setAccessible(true);
            tank.variant = water;
            tank.amount = FluidConstants.BUCKET / 2;
            check(!(boolean) insert.invoke(builder, new FluidStack(water, 1000), TANK, Direction.DOWN), "A partial bucket does not fit");
            check(tank.amount == FluidConstants.BUCKET / 2, "Rejected insertion rolls back");
            tank.amount = 0;
            check((boolean) insert.invoke(builder, new FluidStack(water, 1000), TANK, Direction.DOWN), "A full bucket fits");
            check(tank.amount == FluidConstants.BUCKET, "Insertion uses Fabric bucket units");
            Method extract = BuilderTileEntity.class.getDeclaredMethod("findAndConsumeLiquid", Storage.class, Level.class, BlockPos.class);
            extract.setAccessible(true);
            var result = (FluidStack) extract.invoke(builder, tank, level, BUILDER);
            check(result.isEmpty() && tank.amount == FluidConstants.BUCKET, "Partial extraction rolls back without creating a fluid block");
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
