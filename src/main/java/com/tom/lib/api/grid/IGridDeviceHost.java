package com.tom.lib.api.grid;

public interface IGridDeviceHost {
	<G extends IGrid<?, G>> IGridDevice<G> getDevice(Class<G> gridClass, Object... objects);
}
