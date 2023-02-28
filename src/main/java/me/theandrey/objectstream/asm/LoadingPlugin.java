package me.theandrey.objectstream.asm;

import java.io.File;
import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import me.theandrey.objectstream.Config;

@IFMLLoadingPlugin.SortingIndex(2000)
@IFMLLoadingPlugin.TransformerExclusions("me.theandrey.objectstream.asm.")
public class LoadingPlugin implements IFMLLoadingPlugin {

    public LoadingPlugin() {
        Config.load(new File("config/ObjectStreamBlocker.cfg"));
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"me.theandrey.objectstream.asm.ObjectInputStreamTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
