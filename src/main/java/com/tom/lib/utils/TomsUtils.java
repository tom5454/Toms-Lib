package com.tom.lib.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.oredict.OreDictionary;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.mojang.authlib.GameProfile;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.lib.LibInit;
import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.thirdparty.storagedrawers.DrawerGroupWrapper;
import com.tom.lib.thirdparty.storagedrawers.DrawerWrapper;

import io.netty.buffer.ByteBuf;

public class TomsUtils {
	public static final UUID NULL_ID = new UUID(0, 0);
	private static final int DIVISION_BASE = 1000;
	private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();
	private static final Format format;
	private static final Joiner comma = Joiner.on(", ").skipNulls();
	protected static Random rng = new Random();
	public static final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	@SideOnly(Side.CLIENT)
	private static final int DELETION_ID = 2525277;
	@SideOnly(Side.CLIENT)
	private static int lastAdded;
	protected static InventoryCrafting craftingInv = new InventoryCrafting(new Container() {
		@Override public boolean canInteractWith(EntityPlayer playerIn) { return true; }
		@Override public void onCraftMatrixChanged(IInventory inventoryIn) {};
	}, 3, 3);
	private static final GameProfile profile;
	private static final UUID fakePlayerUUID;
	private static final String fakePlayerName = "[TomsLib]";
	public static final DataSerializer<Long> LONG_SERIALIZER = new DataSerializer<Long>() {
		@Override public void write(PacketBuffer buf, Long value){buf.writeLong(value.longValue());}
		@Override public Long read(PacketBuffer buf) throws IOException {return buf.readLong();}
		@Override public DataParameter<Long> createKey(int id) {return new DataParameter<>(id, this);}
		@Override public Long copyValue(Long value){return value;}
	};
	public static final Logger log = LogManager.getLogger("Tom's Lib Utils");
	static {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat format_ = new DecimalFormat(".#;0.#");
		format_.setDecimalFormatSymbols(symbols);
		format_.setRoundingMode(RoundingMode.DOWN);
		format = format_;
		fakePlayerUUID = UUID.nameUUIDFromBytes(fakePlayerName.getBytes());
		profile = new GameProfile(fakePlayerUUID, fakePlayerName);
	}

	public static void printFakePlayerInfo(){
		log.info("Tom's Lib Fake Player: " + fakePlayerName + ", UUID: " + fakePlayerUUID);
	}

	public static int find(String[] array, String obj) {
		for (int i = 0;i < array.length;i++) {
			String string = array[i];
			if(obj.equals(string))return i;
		}
		return -1;
	}
	public static <T> Optional<T> get(WeakReference<T> ref){
		return Optional.<T>of(ref.get());
	}

	public static void writeInventory(String name, NBTTagCompound compound, IInventory inv) {
		compound.setTag(name, saveAllItems(inv));
	}

	public static boolean isItemListEmpty(NonNullList<ItemStack> list) {
		return list.stream().allMatch(ItemStack::isEmpty);
	}

	public static NBTTagCompound writeBlock(Block block) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("b", block.delegate.name().toString());
		return tag;
	}

	public static Block readBlock(NBTTagCompound tag) {
		return Block.REGISTRY.getObject(new ResourceLocation(tag.getString("b")));
	}
	public static NBTTagList saveAllItems(List<ItemStack> list) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0;i < list.size();++i) {
			ItemStack itemstack = list.get(i);

			if (!itemstack.isEmpty()) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				itemstack.writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		return nbttaglist;
	}

	public static NBTTagList saveAllItems(IInventory inv) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0;i < inv.getSizeInventory();++i) {
			ItemStack itemstack = inv.getStackInSlot(i);

			if (!itemstack.isEmpty()) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				itemstack.writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		return nbttaglist;
	}

	public static void loadAllItems(NBTTagList nbttaglist, List<ItemStack> list) {
		for (int i = 0;i < nbttaglist.tagCount();++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < list.size()) {
				list.set(j, new ItemStack(nbttagcompound));
			}
		}
	}

	public static void loadAllItems(NBTTagList nbttaglist, IInventory inv) {
		inv.clear();
		int invSize = inv.getSizeInventory();
		for (int i = 0;i < nbttaglist.tagCount();++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < invSize) {
				inv.setInventorySlotContents(j, new ItemStack(nbttagcompound));
			}
		}
	}

	public static void loadAllItems(NBTTagCompound tag, String name, IInventory inv) {
		loadAllItems(tag.getTagList(name, 10), inv);
	}
	public static String formatNumber(long number) {
		int width = 4;
		assert number >= 0;
		String numberString = Long.toString(number);
		int numberSize = numberString.length();
		if (numberSize <= width) { return numberString; }

		long base = number;
		double last = base * 1000;
		int exponent = -1;
		String postFix = "";

		while (numberSize > width) {
			last = base;
			base /= DIVISION_BASE;

			exponent++;

			numberSize = Long.toString(base).length() + 1;
			postFix = String.valueOf(ENCODED_POSTFIXES[exponent]);
		}

		String withPrecision = format.format(last / DIVISION_BASE) + postFix;
		String withoutPrecision = Long.toString(base) + postFix;

		String slimResult = (withPrecision.length() <= width) ? withPrecision : withoutPrecision;
		assert slimResult.length() <= width;
		return slimResult;
	}
	public static String join(int[] in) {
		return comma.join(Arrays.stream(in).iterator());
	}
	public static IItemHandler getItemHandler(World world, BlockPos pos, EnumFacing side, boolean includeEntities) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IDrawer) {
			return new DrawerWrapper((IDrawer) te);
		} else if (te instanceof IDrawerGroup) {
			return new DrawerGroupWrapper((IDrawerGroup) te);
		} else if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
			IInventory inv = includeEntities ? TileEntityHopper.getInventoryAtPosition(world, pos.getX(), pos.getY(), pos.getZ()) : (te instanceof IInventory ? (IInventory) te : null);
			if (inv != null) {
				if (inv instanceof ISidedInventory) {
					return new SidedInvWrapper((ISidedInventory) inv, side);
				} else {
					return new InvWrapper(inv);
				}
			} else
				return null;
		} else {
			return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		}
	}
	public static boolean isUseable(int xCoord, int yCoord, int zCoord, EntityPlayer player, World worldObj, TileEntity thisT) {
		return isUsable(new BlockPos(xCoord, yCoord, zCoord), player, worldObj, thisT);
	}

	public static boolean isUsable(BlockPos pos, EntityPlayer player, World worldObj, TileEntity thisT) {
		return worldObj.getTileEntity(pos) != thisT ? false : player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	public static boolean isUseable(EntityPlayer player, TileEntity thisT) {
		return thisT.getWorld().getTileEntity(thisT.getPos()) != thisT ? false : player.getDistanceSq(thisT.getPos().getX() + 0.5D, thisT.getPos().getY() + 0.5D, thisT.getPos().getZ() + 0.5D) <= 64.0D;
	}

	public static EnumFacing getDirectionFacing(EntityLivingBase entity, boolean includeUpAndDown) {
		double yaw = entity.rotationYaw;
		while (yaw < 0)
			yaw += 360;
		yaw = yaw % 360;
		if (includeUpAndDown) {
			if (entity.rotationPitch > 45)
				return EnumFacing.DOWN;
			else if (entity.rotationPitch < -45)
				return EnumFacing.UP;
		}
		if (yaw < 45)
			return EnumFacing.SOUTH;
		else if (yaw < 135)
			return EnumFacing.WEST;
		else if (yaw < 225)
			return EnumFacing.NORTH;
		else if (yaw < 315)
			return EnumFacing.EAST;
		else
			return EnumFacing.SOUTH;
	}

	public static int getBurnTime(ItemStack is) {
		return TileEntityFurnace.getItemBurnTime(is);
	}

	public static void setBlockState(World worldIn, BlockPos pos, IBlockState state, int flags) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		worldIn.setBlockState(pos, state, flags);
		if (tileentity != null) {
			tileentity.validate();
			worldIn.setTileEntity(pos, tileentity);
		}
	}

	public static void setBlockState(World worldIn, BlockPos pos, IBlockState state) {
		setBlockState(worldIn, pos, state, 2);
	}

	public static void setBlockState(World worldIn, int x, int y, int z, IBlockState state) {
		setBlockState(worldIn, getBlockPos(x, y, z), state);
	}

	public static void setBlockState(World worldIn, int x, int y, int z, IBlockState state, int flags) {
		setBlockState(worldIn, getBlockPos(x, y, z), state, flags);
	}

	public static BlockPos getBlockPos(int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	public static <T extends Comparable<T>> void setBlockStateWithCondition(World worldObj, BlockPos pos, IBlockState state, IProperty<T> p, T valueE) {
		try {
			if (state.getValue(p) != valueE)
				setBlockState(worldObj, pos, state.withProperty(p, valueE), 2);
		} catch (Exception e) {
			LibInit.log.catching(e);
		}
	}

	public static <T extends Comparable<T>> void setBlockStateWithCondition(World worldObj, BlockPos pos, IProperty<T> p, T valueE) {
		setBlockStateWithCondition(worldObj, pos, worldObj.getBlockState(pos), p, valueE);
	}

	@SideOnly(Side.CLIENT)
	private static void sendNoSpamMessages(ITextComponent[] messages) {
		GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
		for (int i = DELETION_ID + messages.length - 1;i <= lastAdded;i++) {
			chat.deleteChatLine(i);
		}
		for (int i = 0;i < messages.length;i++) {
			chat.printChatMessageWithOptionalDeletion(messages[i], DELETION_ID + i);
		}
		lastAdded = DELETION_ID + messages.length - 1;
	}

	/**
	 * Sends a chat message to the client, deleting past messages also sent via
	 * this method.
	 *
	 * Credit to RWTema for the idea
	 *
	 * @param player
	 *            The player to send the chat message to
	 * @param lines
	 *            The chat lines to send.
	 */
	public static void sendNoSpam(EntityPlayer player, ITextComponent... lines) {
		if (lines.length > 0)
			LibNetworkHandler.sendTo(new PacketNoSpamChat(lines), (EntityPlayerMP) player);
	}

	/**
	 * @author tterrag1098
	 *
	 *         Ripped from EnderCore (and slightly altered)
	 */
	public static class PacketNoSpamChat implements IMessage {

		private ITextComponent[] chatLines;

		public PacketNoSpamChat() {
			chatLines = new ITextComponent[0];
		}

		private PacketNoSpamChat(ITextComponent... lines) {
			// this is guaranteed to be >1 length by accessing methods
			this.chatLines = lines;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(chatLines.length);
			for (ITextComponent c : chatLines) {
				ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(c));
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			chatLines = new ITextComponent[buf.readInt()];
			for (int i = 0;i < chatLines.length;i++) {
				chatLines[i] = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
			}
		}

		public static class Handler implements IMessageHandler<PacketNoSpamChat, IMessage> {

			@Override
			public IMessage onMessage(final PacketNoSpamChat message, MessageContext ctx) {
				Minecraft.getMinecraft().addScheduledTask(new Runnable() {

					@Override
					public void run() {
						sendNoSpamMessages(message.chatLines);
					}
				});
				return null;
			}
		}
	}

	public static ITextComponent getChatMessageFromString(String in, Object... args) {
		return new TextComponentTranslation(in, args);
	}

	public static void sendNoSpamTranslate(EntityPlayer player, String key, Object... args) {
		if (!player.world.isRemote)
			sendNoSpam(player, new TextComponentTranslation(key, args));
	}

	public static void sendNoSpamTranslate(EntityPlayer player, Style style, String key, Object... args) {
		if (!player.world.isRemote)
			sendNoSpam(player, new TextComponentTranslation(key, args).setStyle(style));
	}

	public static void sendChatMessages(EntityPlayer player, ITextComponent... lines) {
		for (ITextComponent c : lines) {
			player.sendMessage(c);
		}
	}

	public static void sendChatTranslate(EntityPlayer player, String key, Object... args) {
		player.sendMessage(new TextComponentTranslation(key, args));
	}

	public static void sendChatTranslate(EntityPlayer player, Style style, String key, Object... args) {
		player.sendMessage(new TextComponentTranslation(key, args).setStyle(style));
	}

	public static void sendNoSpamTranslate(EntityPlayer player, TextFormatting color, String key, Object... args) {
		sendNoSpamTranslate(player, new Style().setColor(color), key, args);
	}

	public static void sendChatTranslate(EntityPlayer player, TextFormatting color, String key, Object... args) {
		sendChatTranslate(player, new Style().setColor(color), key, args);
	}
	public static File getSavedFile() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		File file = null;
		if (server != null) {
			if (server.isSinglePlayer()) {
				String s1 = server.getFile("saves").getAbsolutePath() + File.separator + server.getFolderName() + File.separator + "tm";
				file = new File(s1);
			} else {
				String f = server.getFile("a").getAbsolutePath();
				String s1 = f.substring(0, f.length() - 1) + File.separator + server.getFolderName() + File.separator + "tm";
				file = new File(s1);
			}
		}
		return file;
	}
	public static void writeVec3d(ByteBuf buf, Vec3d pos) {
		boolean hasPos = pos != null;
		buf.writeBoolean(hasPos);
		if (hasPos) {
			buf.writeDouble(pos.x);
			buf.writeDouble(pos.y);
			buf.writeDouble(pos.z);
		}
	}
	public static Vec3d readVec3d(ByteBuf buf) {
		if (buf.readBoolean()) { return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()); }
		return null;
	}

	@SideOnly(Side.CLIENT)
	public static double rotateMatrixByMetadata(int metadata) {
		if (metadata == 0)
			metadata = 1;
		else if (metadata == 1)
			metadata = 0;
		EnumFacing facing = EnumFacing.VALUES[metadata];
		double metaRotation;
		switch (facing) {
		case UP:
			metaRotation = 0;
			GL11.glRotated(90, 1, 0, 0);
			GL11.glTranslated(0, -1, -1);
			break;
		case DOWN:
			metaRotation = 0;
			GL11.glRotated(-90, 1, 0, 0);
			GL11.glTranslated(0, -1, 1);
			break;
		case NORTH:
			metaRotation = 180;
			break;
		case EAST:
			metaRotation = 270;
			break;
		case SOUTH:
			metaRotation = 0;
			break;
		default:
			metaRotation = 90;
			break;
		}
		GL11.glRotated(metaRotation, 0, 1, 0);
		return metaRotation;
	}

	public static TileEntity getTileEntity(World world, int[] coords) {
		return world.getTileEntity(new BlockPos(coords[0], coords[1], coords[2]));
	}
	public static void writeBlockPosToPacket(ByteBuf buf, BlockPos pos) {
		boolean hasPos = pos != null;
		buf.writeBoolean(hasPos);
		if (hasPos) {
			buf.writeInt(pos.getX());
			buf.writeInt(pos.getY());
			buf.writeInt(pos.getZ());
		}
	}
	public static BlockPos readBlockPosFromPacket(ByteBuf buf) {
		if (buf.readBoolean()) { return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()); }
		return null;
	}
	public static MinecraftServer getServer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}
	public static TileEntity getTileEntity(World worldIn, BlockPos pos, int dim) {
		if (worldIn.isRemote) {
			log.error("world is remote");
			return null;
		}
		World world = worldIn;
		if (world.provider.getDimension() != dim) {
			if (getServer() == null) {
				log.error("MinecraftServer.getServer() == null");
				FMLLog.bigWarning("MinecraftServer.getServer() == null");
				return null;
			}
			world = getServer().getWorld(dim);
		}
		if (world == null) {
			log.error("world == null");
			return null;
		}
		return world.getTileEntity(pos);
	}

	public static IBlockState getBlockState(World worldIn, BlockPos pos, int dim) {
		if (worldIn.isRemote) {
			log.error("world is remote");
			return null;
		}
		World world = worldIn;
		if (world.provider.getDimension() != dim) {
			if (getServer() == null) {
				log.error("MinecraftServer.getServer() == null");
				FMLLog.bigWarning("MinecraftServer.getServer() == null");
				return null;
			}
			world = getServer().getWorld(dim);
		}
		if (world == null) {
			log.error("world == null");
			return null;
		}
		return world.getBlockState(pos);
	}

	public static World getWorld(int dim) {
		if (getServer() == null) {
			log.error("MinecraftServer.getServer() == null");
			FMLLog.bigWarning("MinecraftServer.getServer() == null");
			return null;
		}
		return getServer().getWorld(dim);
	}
	public static FakePlayer getFakePlayer(World world) {
		if (world instanceof WorldServer) {
			return FakePlayerFactory.get((WorldServer) world, profile);
		} else
			return null;
	}
	public static ItemStack pushStackToNeighbours(ItemStack stack, World world, BlockPos pos, EnumFacing[] sides) {
		if (sides == null)
			sides = EnumFacing.VALUES;
		for (EnumFacing f : sides) {
			BlockPos p = pos.offset(f);
			IItemHandler inv = getItemHandler(world, p, f.getOpposite(), true);
			if (inv != null) {
				stack = putStackInInventoryAllSlots(inv, stack);
			}
			if (stack.isEmpty())
				break;
		}
		return stack;
	}
	public static ItemStack putStackInInventoryAllSlots(IItemHandler inventory, ItemStack sIn) {
		for (int i = 0;i < inventory.getSlots() && !sIn.isEmpty();i++) {
			sIn = inventory.insertItem(i, sIn, false);
		}
		return sIn;
	}
	@SideOnly(Side.CLIENT)
	public static class GuiRunnableLabel extends GuiLabel {
		GuiRenderRunnable field;

		public GuiRunnableLabel(GuiRenderRunnable field) {
			super(null, 0, 0, 0, 0, 0, 0);
			this.field = field;
		}

		@Override
		public void drawLabel(Minecraft mc, int mouseX, int mouseY) {
			field.run(mouseX, mouseY);
		}
	}

	@SideOnly(Side.CLIENT)
	public static interface GuiRenderRunnable {
		void run(int mouseX, int mouseY);
	}

	@SideOnly(Side.CLIENT)
	public static List<GuiLabel> addRunnableToLabelList(GuiRenderRunnable field, List<GuiLabel> labelList) {
		if (field != null)
			labelList.add(new GuiRunnableLabel(field));
		return labelList;
	}

	public static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	public static boolean areItemStacksEqualOreDict(ItemStack stack, ItemStack matchTo, boolean checkMeta, boolean checkNBT, boolean checkMod, boolean checkOreDict) {
		if (stack.isEmpty() && matchTo.isEmpty())
			return true;
		if (!stack.isEmpty() && !matchTo.isEmpty()) {
			if (areItemStacksEqual(stack, matchTo, checkMeta, checkNBT, checkMod)) {
				return true;
			} else if (checkOreDict) {
				int[] matchIds = OreDictionary.getOreIDs(matchTo);
				int[] ids = OreDictionary.getOreIDs(stack);
				if (matchIds.length < 1 && ids.length < 1) { return areItemStacksEqual(stack, matchTo, checkMeta, checkNBT, checkMod); }
				boolean equals = false;
				for (int i = 0;i < matchIds.length;i++) {
					for (int j = 0;j < ids.length;j++) {
						if (matchIds[i] == ids[j]) {
							equals = true;
							break;
						}
					}
				}
				if (checkNBT) {
					equals = equals && ItemStack.areItemStackTagsEqual(stack, matchTo);
				}
				return equals;
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static String getTranslatedName(ItemStack stack) {
		return stack.getDisplayName();
	}

	public static List<ItemStack> craft(ItemStack[] input, EntityPlayer player, World world) {
		if (player == null)
			player = getFakePlayer(world);
		for (int i = 0;i < input.length && i < 9;i++) {
			craftingInv.setInventorySlotContents(i, input[i] != null && !input[i].isEmpty() ? input[i].copy() : ItemStack.EMPTY);
		}
		NonNullList<ItemStack> ret = NonNullList.<ItemStack>create();
		IRecipe matchingRecipe = CraftingManager.findMatchingRecipe(craftingInv, player.world);
		if (matchingRecipe != null) {
			ItemStack result = matchingRecipe.getCraftingResult(craftingInv);
			if(result != null){
				FMLCommonHandler.instance().firePlayerCraftingEvent(player, result, craftingInv);
				ForgeHooks.setCraftingPlayer(player);
				NonNullList<ItemStack> aitemstack = CraftingManager.getRemainingItems(craftingInv, player.world);
				ForgeHooks.setCraftingPlayer(null);
				ret.add(result);
				for (int i = 0;i < aitemstack.size();++i) {
					ItemStack itemstack = craftingInv.getStackInSlot(i);
					ItemStack itemstack1 = aitemstack.get(i);

					if (!itemstack.isEmpty()) {
						craftingInv.decrStackSize(i, 1);
						itemstack = craftingInv.getStackInSlot(i);
					}

					if (!itemstack1.isEmpty()) {
						if (itemstack.isEmpty()) {
							craftingInv.setInventorySlotContents(i, itemstack1);
						} else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1)) {
							itemstack1.grow(itemstack.getCount());
							craftingInv.setInventorySlotContents(i, itemstack1);
						} else
							ret.add(itemstack1);
					}
				}
			}
			for (int i = 0;i < 9;i++) {
				ItemStack stack = craftingInv.removeStackFromSlot(i);
				if (!stack.isEmpty()) {
					ret.add(stack);
				}
			}
			return !result.isEmpty() ? ret : null;
		}
		return null;
	}

	public static ItemStack[] getStackArrayFromInventory(IInventory inv) {
		ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
		for (int i = 0;i < inv.getSizeInventory();i++) {
			ret[i] = inv.getStackInSlot(i);
		}
		return ret;
	}

	public static ItemStack getMathchingRecipe(ItemStack[] input, World world) {
		for (int i = 0;i < input.length && i < 9;i++) {
			craftingInv.setInventorySlotContents(i, input[i]);
		}
		IRecipe recipe = CraftingManager.findMatchingRecipe(craftingInv, world);
		if(recipe != null){
			ItemStack result = recipe.getCraftingResult(craftingInv);
			for (int i = 0;i < 9;i++) {
				craftingInv.setInventorySlotContents(i, ItemStack.EMPTY);
			}
			return result;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getMathchingRecipe(IInventory input, World world) {
		return getMathchingRecipe(getStackArrayFromInventory(input), world);
	}
	public static boolean areItemStacksEqual(ItemStack stack, ItemStack matchTo, boolean checkMeta, boolean checkNBT, boolean checkMod) {
		if (stack.isEmpty() && matchTo.isEmpty())
			return false;
		if (!stack.isEmpty() && !matchTo.isEmpty()) {
			if (checkMod) {
				String modname = stack.getItem().delegate.name().getResourceDomain();
				return modname != null && modname.equals(matchTo.getItem().delegate.name().getResourcePath());
			} else {
				if (stack.getItem() == matchTo.getItem()) {
					boolean equals = true;
					if (checkMeta) {
						equals = equals && (stack.getItemDamage() == matchTo.getItemDamage() || stack.getMetadata() == OreDictionary.WILDCARD_VALUE || matchTo.getMetadata() == OreDictionary.WILDCARD_VALUE);
					}
					if (checkNBT) {
						equals = equals && ItemStack.areItemStackTagsEqual(stack, matchTo);
					}
					return equals;
				}
			}
		}
		return false;
	}
	public static int[] createIntsFromBlockPos(BlockPos pos) {
		long h = pos.toLong();
		int a = (int) (h >> 32);
		int b = (int) h;
		return new int[]{a, b};
	}

	public static BlockPos createBlockPos(int a, int b) {
		return BlockPos.fromLong((long) a << 32 | b & 0xFFFFFFFFL);
	}

	public static int getColorFrom(TextFormatting f) {
		switch (f) {
		case AQUA:
			return 0x55FFFF;
		case BLACK:
			return 0;
		case BLUE:
			return 0x5555FF;
		case BOLD:
			return 0;
		case DARK_AQUA:
			return 0x00AAAA;
		case DARK_BLUE:
			return 0x0000AA;
		case DARK_GRAY:
			return 0x555555;
		case DARK_GREEN:
			return 0x00AA00;
		case DARK_PURPLE:
			return 0xAA00AA;
		case DARK_RED:
			return 0xAA0000;
		case GOLD:
			return 0xFFAA00;
		case GRAY:
			return 0xAAAAAA;
		case GREEN:
			return 0x55FF55;
		case ITALIC:
			return 0;
		case LIGHT_PURPLE:
			return 0xFF55FF;
		case OBFUSCATED:
			return 0;
		case RED:
			return 0xFF5555;
		case RESET:
			return 0;
		case STRIKETHROUGH:
			return 0;
		case UNDERLINE:
			return 0;
		case WHITE:
			return 0xFFFFFF;
		case YELLOW:
			return 0xFFFF55;
		default:
			return 0;
		}
	}

	private static class ContainerCrafting extends Container {
		private InventoryCrafting craftingInv;
		private EntityPlayer player;
		private InventoryCraftResult result;
		public ContainerCrafting(IInventory craftingInvIn, EntityPlayer player, InventoryCraftResult result) {
			this.craftingInv = new WrappedInventoryCrafting(this, 3, 3, craftingInvIn);
			this.player = player;
			this.result = result;
		}
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		@Override
		public void onCraftMatrixChanged(IInventory inventoryIn) {
			this.slotChangedCraftingGrid(player.world, player, craftingInv, result);
		}
		public InventoryCrafting getCraftingInv() {
			return craftingInv;
		}
		private static class WrappedInventoryCrafting extends InventoryCrafting {

			public WrappedInventoryCrafting(Container eventHandlerIn, int width, int height, IInventory inv) {
				super(eventHandlerIn, width, height);
				this.inv = inv;
			}

			private IInventory inv;

			@Override
			public boolean isEmpty() {
				return inv.isEmpty();
			}

			@Override
			public ItemStack getStackInSlot(int index) {
				return inv.getStackInSlot(index);
			}

			@Override
			public ItemStack decrStackSize(int index, int count) {
				return inv.decrStackSize(index, count);
			}

			@Override
			public ItemStack removeStackFromSlot(int index) {
				return inv.removeStackFromSlot(index);
			}

			@Override
			public void setInventorySlotContents(int index, ItemStack stack) {
				inv.setInventorySlotContents(index, stack);
			}

			@Override
			public void markDirty() {
				inv.markDirty();
			}

			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack) {
				return inv.isItemValidForSlot(index, stack);
			}

			@Override
			public void clear() {
				inv.clear();
			}
		}
	}
	public static InventoryCrafting wrapCraftingInv(IInventory craftingInvIn, EntityPlayer player, InventoryCraftResult result) {
		return new ContainerCrafting(craftingInvIn, player, result).getCraftingInv();
	}
	public static List<String> getStringList(String... in) {
		List<String> list = new ArrayList<>();
		if (in != null) {
			for (String a : in) {
				list.add(a);
			}
		}
		return list;
	}
	public static void writeBlockPosToNBT(NBTTagCompound tag, BlockPos pos) {
		if (pos != null) {
			tag.setInteger("posX", pos.getX());
			tag.setInteger("posY", pos.getY());
			tag.setInteger("posZ", pos.getZ());
		} else {
			tag.setBoolean("null", true);
		}
	}

	public static NBTTagCompound writeBlockPosToNewNBT(BlockPos pos) {
		NBTTagCompound tag = new NBTTagCompound();
		writeBlockPosToNBT(tag, pos);
		return tag;
	}

	public static BlockPos readBlockPosFromNBT(NBTTagCompound tag) {
		if (tag.hasKey("null") || !tag.hasKey("posX") || !tag.hasKey("posY") || !tag.hasKey("posZ"))
			return null;
		return new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
	}

	public static <T extends Annotation> List<Entry<Class<?>, T>> getInstances(ASMDataTable asmDataTable, Class<T> annotationClass) {
		String annotationClassName = annotationClass.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		List<Entry<Class<?>, T>> instances = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : asmDatas) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				T t = asmClass.getAnnotation(annotationClass);
				instances.add(new EmptyEntry<>(asmClass, t));
			} catch (ClassNotFoundException | LinkageError e) {
				log.error("Failed to load: {}\n", asmData.getClassName(), e);
			}
		}
		return instances;
	}
	public static <K, U> Collector<Pair<K, U>, ?, Map<K,U>> toMapCollector() {
		return Collectors.toMap(Pair::getKey, Pair::getValue);
	}
	public static <K, V, R> Function<Entry<K, V>, Pair<K, R>> valueMapper(Function<V, R> func){
		return e -> Pair.of(e.getKey(), func.apply(e.getValue()));
	}
}
