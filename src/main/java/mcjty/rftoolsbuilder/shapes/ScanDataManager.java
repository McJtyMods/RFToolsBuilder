package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScanDataManager extends AbstractWorldData<ScanDataManager> {

    private static final String SCANDATA_NETWORK_NAME = "RFToolsScanData";

    private int lastId = 0;

    private final Map<Integer, Scan> scans = new HashMap<>();

    // This data is not persisted
    private final Map<Integer, ScanExtraData> scanData = new HashMap<>();

    public ScanDataManager() {
    }

    public ScanDataManager(CompoundTag tag) {
        scans.clear();
        ListTag lst = tag.getList("scans", Tag.TAG_COMPOUND);
        for (int i = 0; i < lst.size(); i++) {
            CompoundTag tc = lst.getCompound(i);
            int id = tc.getInt("scan");
            Scan scan = new Scan();
            scan.readFromNBT(tc);
            scans.put(id, scan);
        }
        lastId = tag.getInt("lastId");
    }

    public void save(Level w, int scanId) {
        Level world = LevelTools.getOverworld(w);
        File dataDir = null; // @todo 1.16 new File(((ServerWorld)world).getSaveHandler().getWorldDirectory(), "rftoolsscans");
        dataDir.mkdirs();
        File file = new File(dataDir, "scan" + scanId);
        Scan scan = getOrCreateScan(scanId);
        CompoundTag tc = new CompoundTag();
        scan.writeToNBTExternal(tc);
        try(DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file))) {
            NbtIo.writeCompressed(tc, dataoutputstream);
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing to file 'scan" + scan + "'!", e);
        }
        save();
    }

    public ScanExtraData getExtraData(int id) {
        ScanExtraData data = scanData.get(id);
        if (data == null) {
            data = new ScanExtraData();
            scanData.put(id, data);
        } else {
            // Longer to accomodate for delay on locator
            if (data.getBirthTime() + (ScannerConfiguration.ticksPerLocatorScan.get()*100) < System.currentTimeMillis()) {
                data = new ScanExtraData();
                scanData.put(id, data);
            }
        }
        return data;
    }

    public static ScanDataManager get(Level world) {
        return getData(world, ScanDataManager::new, ScanDataManager::new, SCANDATA_NETWORK_NAME);
    }

    @Nonnull
    public Scan getOrCreateScan(int id) {
        Scan scan = scans.get(id);
        if (scan == null) {
            scan = new Scan();
            scans.put(id, scan);
        }
        return scan;
    }

    @Nonnull
    public Scan loadScan(Level w, int id) {
        Level world = LevelTools.getOverworld(w);
        Scan scan = scans.get(id);
        if (scan == null || scan.getDataInt() == null) {
            if (scan == null) {
                scan = new Scan();
            }
            File dataDir = null; // @todo 1.16 new File(((ServerWorld)world).getSaveHandler().getWorldDirectory(), "rftoolsscans");
            dataDir.mkdirs();
            File file = new File(dataDir, "scan" + id);
            if (file.exists()) {
                try(DataInputStream datainputstream = new DataInputStream(new FileInputStream(file))) {
                    CompoundTag tag = NbtIo.readCompressed(datainputstream);
                    scan.readFromNBTExternal(tag);
                } catch (IOException e) {
                    Logging.log("Error reading scan file for id: " + id);
                }
            }
        }
        return scan;
    }

    public static void listScans(Player sender) {
        ScanDataManager scans = get(sender.getCommandSenderWorld());
        for (Map.Entry<Integer, Scan> entry : scans.scans.entrySet()) {
            Integer scanid = entry.getKey();
            scans.loadScan(sender.getCommandSenderWorld(), scanid);
            Scan scan = entry.getValue();
            BlockPos dim = scan.getDataDim();
            if (dim == null) {
                sender.sendMessage(new TextComponent(
                        ChatFormatting.YELLOW + "Scan: " + ChatFormatting.WHITE + scanid +
                                ChatFormatting.RED + "   Invalid"), Util.NIL_UUID);
            } else {
                sender.sendMessage(new TextComponent(
                        ChatFormatting.YELLOW + "Scan: " + ChatFormatting.WHITE + scanid +
                                ChatFormatting.YELLOW + "   Dim: " + ChatFormatting.WHITE + dim.getX() + "," + dim.getY() + "," + dim.getZ() +
                                ChatFormatting.YELLOW + "   Size: " + ChatFormatting.WHITE + scan.getRledata().length + " bytes"), Util.NIL_UUID);
            }
        }
    }


    public int newScan(Level world) {
        lastId++;
        save();
        return lastId;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public CompoundTag save(CompoundTag tagCompound) {
        ListTag lst = new ListTag();
        for (Map.Entry<Integer, Scan> entry : scans.entrySet()) {
            CompoundTag tc = new CompoundTag();
            tc.putInt("scan", entry.getKey());
            entry.getValue().writeToNBT(tc);
            lst.add(tc);
        }
        tagCompound.put("scans", lst);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

}
