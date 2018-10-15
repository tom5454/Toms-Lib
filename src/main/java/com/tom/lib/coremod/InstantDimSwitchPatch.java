package com.tom.lib.coremod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class InstantDimSwitchPatch implements IClassTransformer {
	private static final boolean DUMP = false;
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(basicClass == null)return null;
		if(DUMP)CoreModUtils.dump(transformedName, basicClass);
		/*if(name.equals("net.minecraft.client.Minecraft")){
			System.out.println("Found MC class");
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			main:
				for (MethodNode m : classNode.methods) {
					if(m.name.equals("loadWorld")){
						String desc = m.desc;
						MethodDescriptor d = new MethodDescriptor(desc);
						if(d.ret.equals("void") && d.args.length == 2){
							String a1 = d.args[0];
							String a2 = d.args[1];
							if(a1.equals("net.minecraft.client.multiplayer.WorldClient") && a2.equals("java.lang.String")){
								System.out.println("Method found");
								for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
									AbstractInsnNode insnNode = it.next();
									//System.out.println(insnNode.getType());
									if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
										MethodInsnNode node = (MethodInsnNode) insnNode;
										//System.out.println(node.owner + "." + node.name);
										//System.out.println("Opcode: " + node.getOpcode());
										if(node.owner.equals("java/lang/System") && node.name.equals("gc")){
											System.out.println("Opcode gc found");
											it.remove();
											MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, "com/tom/lib/coremod/InstantDimSwitchPatch", "gc", "()V", false);
											it.add(replace);
											break main;
										}
									}

								}
							}
						}
					}
				}
			try {
				new File(".", "asm").mkdirs();
				PrintWriter w = new PrintWriter(new File("./asm", "Minecraft.txt"));
				CoreModUtils.dump(w, classNode);
				w.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}else*/ if(name.equals("net.minecraft.client.network.NetHandlerPlayClient")){
			System.out.println("Found NetHandlerPlayClient class");
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			//boolean foundLine = false, fieldReplaced = false;
			main:
				for (MethodNode m : classNode.methods) {
					if(m.name.equals("handleRespawn")){
						String desc = m.desc;
						MethodDescriptor d = new MethodDescriptor(desc);
						if(d.ret.equals("void") && d.args.length == 1){
							String a1 = d.args[0];
							if(a1.equals("net.minecraft.network.play.server.SPacketRespawn")){
								System.out.println("Method found");
								for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
									AbstractInsnNode insnNode = it.next();
									/*if(fieldReplaced){
										if(insnNode.getType() == AbstractInsnNode.METHOD_INSN){
											MethodInsnNode mn = (MethodInsnNode) insnNode;
											if(mn.owner.equals("net/minecraft/client/Minecraft") && mn.desc.equals("(Lnet/minecraft/client/multiplayer/WorldClient;)V")){
												System.out.println("Method found");
												it.remove();
												MethodInsnNode replaced = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/tom/lib/coremod/PatchedWorldLoader", "loadWorld", "(Lnet/minecraft/client/multiplayer/WorldClient;)V", false);
												it.add(replaced);
												break main;
											}
										}
									}else if(foundLine){
										if(insnNode.getType() == AbstractInsnNode.FIELD_INSN){
											FieldInsnNode fn = (FieldInsnNode) insnNode;
											if(fn.owner.equals("net/minecraft/client/network/NetHandlerPlayClient") && fn.desc.equals("Lnet/minecraft/client/Minecraft;")){
												it.remove();
												FieldInsnNode replaced = new FieldInsnNode(Opcodes.GETSTATIC, "com/tom/lib/proxy/ClientProxy", "patchedWorldLoader", "");
												it.add(replaced);
												System.out.println("Field getter redirected");
												fieldReplaced = true;
											}
										}
									}else */if(insnNode.getType() == AbstractInsnNode.LINE){
										LineNumberNode ln = (LineNumberNode) insnNode;
										if(ln.line == 1161){
											System.out.println("Line found");
											//foundLine = true;
											CoreModUtils.removeNext(it, 5);
											VarInsnNode r0 = new VarInsnNode(Opcodes.ALOAD, 0);
											it.add(r0);
											VarInsnNode r1 = new VarInsnNode(Opcodes.ALOAD, 0);
											it.add(r1);
											FieldInsnNode r2 = new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/network/NetHandlerPlayClient", "world", "Lnet/minecraft/client/multiplayer/WorldClient;");
											it.add(r2);
											MethodInsnNode r3 = new MethodInsnNode(Opcodes.INVOKESTATIC, "com/tom/lib/proxy/ClientProxy", "patchedLoadWorld", "(Lnet/minecraft/client/multiplayer/WorldClient;)Lnet/minecraft/client/multiplayer/WorldClient;", false);
											it.add(r3);
											FieldInsnNode r4 = new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/network/NetHandlerPlayClient", "world", "Lnet/minecraft/client/multiplayer/WorldClient;");
											it.add(r4);
											validate(classNode);
											System.out.println("Method patched");
											break main;
										}
									}
								}
							}
						}
					}
				}
			try {
				new File(".", "asm").mkdirs();
				PrintWriter w = new PrintWriter(new File("./asm", "NetHandlerPlayClient.txt"));
				CoreModUtils.dump(w, classNode);
				w.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			return writer.toByteArray();
		}else if(name.equals("com.tom.lib.coremod.Dump")){
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			try {
				new File(".", "asm").mkdirs();
				PrintWriter w = new PrintWriter(new File("./asm", "dump.txt"));
				CoreModUtils.dump(w, classNode);
				w.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return basicClass;
	}
	//Patched method
	public static void gc(){
		//System.gc();
	}
	private static void validate(ClassNode classNode){
		try {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			byte[] d =  writer.toByteArray();
			ClassReader classReader = new ClassReader(d);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classReader.accept(classWriter, ClassReader.SKIP_FRAMES);
		} catch (Throwable e){
			e.printStackTrace();
		}
	}
}
