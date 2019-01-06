package com.tom.lib.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

import net.minecraftforge.fluids.FluidTank;

import com.tom.lib.network.messages.MessageGuiSync;

public class GuiSyncHandler {
	private Map<Integer, Object> lastValues = new HashMap<>();
	private Map<Integer, DataSerializer<?>> serializerMap = new HashMap<>();
	private Map<Integer, Supplier<?>> suppliers = new HashMap<>();
	private Map<Integer, Consumer<?>> listeners = new HashMap<>();
	private Map<Integer, Consumer<?>> updaters = new HashMap<>();
	private Map<Integer, IntSupplier> intSuppliers = new HashMap<>();
	private Map<Integer, IntConsumer> intUpdaters = new HashMap<>();
	private Map<Integer, Integer> lastInts = new HashMap<>();
	private IPacketReceiver receiverTile, receiverGui;
	private List<IContainerListener> crafters;

	private final Container container;
	public GuiSyncHandler(Container container) {
		this.container = container;
	}
	public <T> void register(int id, Supplier<T> supplier, Consumer<T> updater, DataSerializer<T> serializer){
		suppliers.put(id, supplier);
		updaters.put(id, updater);
		serializerMap.put(id, serializer);
	}
	public void registerString(int id, Supplier<String> supplier, Consumer<String> updater){
		register(id, supplier, updater, DataSerializers.STRING);
	}
	public void registerTag(int id, Supplier<NBTTagCompound> supplier, Consumer<NBTTagCompound> updater){
		register(id, supplier, updater, DataSerializers.COMPOUND_TAG);
	}
	public void registerTag(int id, Consumer<NBTTagCompound> supplier, Consumer<NBTTagCompound> updater){
		registerTag(id, () -> {NBTTagCompound tag = new NBTTagCompound();supplier.accept(tag);return tag;}, updater);
	}
	public void registerShort(int id, IntSupplier supplier, IntConsumer updater){
		intSuppliers.put(id, supplier);
		intUpdaters.put(id, updater);
	}
	public <E extends Enum<E>> void registerEnum(int id, Supplier<E> supplier, Consumer<E> updater, E[] values){
		intSuppliers.put(id, () -> supplier.get().ordinal());
		intUpdaters.put(id, i -> updater.accept(values[Math.abs(i) % values.length]));
	}
	public void registerBoolean(int id, BooleanSupplier supplier, Consumer<Boolean> updater){
		intSuppliers.put(id, () -> supplier.getAsBoolean() ? 1 : 0);
		intUpdaters.put(id, i -> updater.accept(i > 0));
	}
	public void registerInt(int id, IntSupplier supplier, IntConsumer updater){
		register(id, supplier::getAsInt, updater::accept, DataSerializers.VARINT);
	}
	public void registerTank(int id, FluidTank tank){
		registerTag(id, () -> tank.writeToNBT(new NBTTagCompound()), tank::readFromNBT);
	}
	public void registerInventoryFieldShort(IInventory inv, int fieldID){
		registerShort(fieldID, () -> inv.getField(fieldID), data -> inv.setField(fieldID, data));
	}
	public void registerInventoryFieldInt(IInventory inv, int fieldID){
		register(fieldID, () -> inv.getField(fieldID), data -> inv.setField(fieldID, data), DataSerializers.VARINT);
	}
	public void detectAndSendChanges(List<IContainerListener> crafters){
		this.crafters = crafters;
		Map<Integer, Integer> toSendInt = new HashMap<>();
		Map<Integer, Object> toSend = new HashMap<>();
		for(Entry<Integer, IntSupplier> e : intSuppliers.entrySet()){
			int val = e.getValue().getAsInt();
			if(!lastInts.containsKey(e.getKey())){
				lastInts.put(e.getKey(), val);
				toSendInt.put(e.getKey(), val);
			}else{
				int lastVal = lastInts.get(e.getKey());
				if(lastVal != val){
					lastInts.put(e.getKey(), val);
					toSendInt.put(e.getKey(), val);
				}
			}
		}
		for(Entry<Integer, Supplier<?>> e : suppliers.entrySet()){
			Object val = e.getValue().get();
			if(!lastValues.containsKey(e.getKey())){
				lastValues.put(e.getKey(), val);
				toSend.put(e.getKey(), val);
			}else{
				Object lastVal = lastValues.get(e.getKey());
				if(!areEqual(lastVal, val)){
					lastValues.put(e.getKey(), val);
					toSend.put(e.getKey(), val);
				}
			}
		}
		if(toSendInt.isEmpty() && toSend.isEmpty())return;
		for (IContainerListener crafter : crafters) {
			for(Entry<Integer, Integer> e : toSendInt.entrySet()){
				crafter.sendWindowProperty(container, e.getKey(), e.getValue());
			}
			if(!toSend.isEmpty()){
				MessageGuiSync msg = new MessageGuiSync(crafter);
				for(Entry<Integer, Object> e : toSend.entrySet()){
					msg.add(e.getKey(), serializerMap.get(e.getKey()), e.getValue());
				}
				msg.send();
			}
		}
	}
	private static boolean areEqual(Object lastVal, Object val) {
		if(lastVal == val)return true;
		if(lastVal != null)return lastVal.equals(val);
		if(val != null)return val.equals(lastVal);
		return false;
	}
	public void updateProgressBar(int id, int data){
		IntConsumer cons = intUpdaters.get(id);
		if(cons != null){
			cons.accept(data);
		}else{
			System.err.println(container + ".updateProgressBar(" + id + ", " + data + "); updater == null");
		}
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void update(EntityPlayer pl, Map<Integer, Object> data, byte id){
		if(id == 1){
			NBTTagCompound tag = (NBTTagCompound) data.get(0);
			if(receiverGui != null)receiverGui.receiveNBTPacket(pl, tag);
		}else{
			for(Entry<Integer, Object> e : data.entrySet()){
				Consumer updater = updaters.get(e.getKey());
				if(updater != null){
					updater.accept(e.getValue());
					Consumer r = listeners.get(e.getKey());
					if(r != null)r.accept(e.getValue());
				}else{
					System.err.println(container + ".update(" + e.getKey() + ", " + e.getValue() + "); updater == null");
				}
			}
		}
	}
	public void setReceiver(IPacketReceiver receiver) {
		this.receiverTile = receiver;
	}
	public void setReceiverGui(Object receiver) {
		if(receiver instanceof IPacketReceiver)this.receiverGui = (IPacketReceiver) receiver;
	}
	public void sendNBTToServer(NBTTagCompound tag, boolean toTile){
		MessageGuiSync msg = new MessageGuiSync();
		msg.add(toTile ? 1 : 3, DataSerializers.COMPOUND_TAG, tag);
		msg.send();
	}
	public void sendNBTToGui(NBTTagCompound tag){
		for (IContainerListener crafter : crafters) {
			MessageGuiSync msg = new MessageGuiSync(crafter);
			msg.add(0, DataSerializers.COMPOUND_TAG, tag);
			msg.setID(1);
			msg.send();
		}
	}
	public void receiveFromClient(EntityPlayer player, Map<Integer, Object> data){
		if(data.containsKey(0)){
			int id = (int) data.get(0);
			int extra = (int) data.get(4);
			receiverTile.buttonPressed(player, id, extra);
		}else if(data.containsKey(2)){
			int id = (int) data.get(2);
			int extra = (int) data.get(4);
			if(container instanceof IPacketReceiver)((IPacketReceiver)container).buttonPressed(player, id, extra);
		}else if(data.containsKey(1)){
			NBTTagCompound tag = (NBTTagCompound) data.get(1);
			receiverTile.receiveNBTPacket(player, tag);
		}else if(data.containsKey(3)){
			NBTTagCompound tag = (NBTTagCompound) data.get(3);
			if(container instanceof IPacketReceiver)((IPacketReceiver)container).receiveNBTPacket(player, tag);
		}
	}
	public <T> void registerListener(int id, Consumer<T> r){
		listeners.put(id, r);
	}
	public void sendButtonToServer(int id, int extra, boolean toTile){
		MessageGuiSync msg = new MessageGuiSync();
		msg.add(toTile ? 0 : 2, DataSerializers.VARINT, id);
		msg.add(4, DataSerializers.VARINT, extra);
		msg.send();
	}
	public void forEach(Consumer<EntityPlayerMP> cons){
		crafters.stream().map(e -> (EntityPlayerMP) e).forEach(cons);
	}
	public static interface ISyncContainer {
		public GuiSyncHandler getSyncHandler();
	}
	public static interface IPacketReceiver {
		default void receiveNBTPacket(EntityPlayer from, NBTTagCompound message){}
		default void buttonPressed(EntityPlayer player, int id, int extra){}
	}
}
