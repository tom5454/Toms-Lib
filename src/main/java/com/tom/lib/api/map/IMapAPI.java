package com.tom.lib.api.map;

public interface IMapAPI {
	void init();
	void createTexturedWayPoint(String group, int mx, int my, int mz, int dim, String markerName, String icon, int beamColor, RenderType beamRenderType, RenderType labelRenderType, boolean reloadable, String beamTexture);
	boolean deleteWayPoint(String group, String markerName);
	void updateMarkers();
	void addSyncedMarker(int id, Marker marker);
	void onPlayerDeath(int mx, int my, int mz, int dim);
}
