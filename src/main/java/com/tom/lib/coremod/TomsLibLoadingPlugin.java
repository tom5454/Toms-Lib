package com.tom.lib.coremod;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class TomsLibLoadingPlugin implements IFMLLoadingPlugin {
	static {
		CoreModUtils.removeNext(null, 0);
		new MethodDescriptor(null, null);
	}
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"com.tom.lib.coremod.InstantDimSwitchPatch"};
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

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
