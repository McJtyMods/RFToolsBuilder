package mcjty.rftoolsbuilder.modules.builder;

import com.google.gson.JsonObject;
import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.lib.datagen.model.BlockModelBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;

public class DataGenHelper {
    public static void generateSupportModel(BaseBlockStateProvider provider) {
        var ok = supportModel(provider, "supportblock_status0", "block/supportblock");
        var warn = supportModel(provider, "supportblock_status1", "block/supportyellowblock");
        var error = supportModel(provider, "supportblock_status2", "block/supportredblock");
        provider.variantBlock(BuilderModule.SUPPORT.get(), state -> switch (state.getValue(SupportBlock.STATUS)) {
            case STATUS_OK -> ok;
            case STATUS_WARN -> warn;
            case STATUS_ERROR -> error;
        }, state -> 0, state -> 0);
    }

    private static BlockModelBuilder supportModel(BaseBlockStateProvider provider, String name, String texture) {
        var model = provider.models().getBuilder(name)
                .parent(provider.models().getExistingFile(provider.modLoc("block/support_block")));
        JsonObject material = new JsonObject();
        material.addProperty("sprite", provider.modLoc(texture).toString());
        material.addProperty("force_translucent", true);
        JsonObject textures = new JsonObject();
        textures.add("all", material);
        model.toJson().add("textures", textures);
        return model;
    }
}
