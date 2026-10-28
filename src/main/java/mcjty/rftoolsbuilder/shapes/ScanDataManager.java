package mcjty.rftoolsbuilder.shapes;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsbase.tools.LegacyWorldData;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

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
        ListTag lst = tag.getListOrEmpty("scans");
        for (int i = 0; i < lst.size(); i++) {
            CompoundTag tc = lst.getCompoundOrEmpty(i);
            int id = tc.getIntOr("scan", 0);
            Scan scan = new Scan();
            scan.readFromNBT(tc);
            scans.put(id, scan);
        }
        lastId = tag.getIntOr("lastId", 0);
    }

    public void save(Level w, int scanId) {
        Level world = LevelTools.getOverworld(w);
        File dataDir = world.getServer().getWorldPath(LevelResource.ROOT).resolve("rftoolsscans").toFile();
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
        return LegacyWorldData.get(world, ScanDataManager::new, ScanDataManager::new,
                "rftoolsbuilder:scans", SCANDATA_NETWORK_NAME);
    }

    @NotNull
    public Scan getOrCreateScan(int id) {
        Scan scan = scans.get(id);
        if (scan == null) {
            scan = new Scan();
            scans.put(id, scan);
        }
        return scan;
    }

    @NotNull
    public Scan loadScan(Level w, int id) {
        Level world = LevelTools.getOverworld(w);
        Scan scan = scans.get(id);
        if (scan == null || scan.getDataInt() == null) {
            if (scan == null) {
                scan = new Scan();
            }
            File dataDir = world.getServer().getWorldPath(LevelResource.ROOT).resolve("rftoolsscans").toFile();
            dataDir.mkdirs();
            File file = new File(dataDir, "scan" + id);
            if (file.exists()) {
                try(DataInputStream datainputstream = new DataInputStream(new FileInputStream(file))) {
                    CompoundTag tag = NbtIo.readCompressed(datainputstream, NbtAccounter.unlimitedHeap());
                    scan.readFromNBTExternal(tag, world.registryAccess());
                } catch (IOException e) {
                    Logging.log("Error reading scan file for id: " + id);
                }
            }
        }
        return scan;
    }

    public static void listScans(Player sender) {
        ScanDataManager scans = get(sender.level());
        for (Map.Entry<Integer, Scan> entry : scans.scans.entrySet()) {
            Integer scanid = entry.getKey();
            scans.loadScan(sender.level(), scanid);
            Scan scan = entry.getValue();
            BlockPos dim = scan.getDataDim();
            if (dim == null) {
                sender.sendSystemMessage(Component.literal(
                        ChatFormatting.YELLOW + "Scan: " + ChatFormatting.WHITE + scanid +
                                ChatFormatting.RED + "   Invalid"));
            } else {
                sender.sendSystemMessage(Component.literal(
                        ChatFormatting.YELLOW + "Scan: " + ChatFormatting.WHITE + scanid +
                                ChatFormatting.YELLOW + "   Dim: " + ChatFormatting.WHITE + dim.getX() + "," + dim.getY() + "," + dim.getZ() +
                                ChatFormatting.YELLOW + "   Size: " + ChatFormatting.WHITE + scan.getRledata().length + " bytes"));
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
    public CompoundTag save(CompoundTag tagCompound, HolderLookup.Provider provider) {
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
