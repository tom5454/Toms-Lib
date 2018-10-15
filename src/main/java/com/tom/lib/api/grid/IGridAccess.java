package com.tom.lib.api.grid;

public interface IGridAccess<G extends IGrid<?, G>> {
	G getGrid();
}