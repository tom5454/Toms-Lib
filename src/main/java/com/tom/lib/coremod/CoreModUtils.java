package com.tom.lib.coremod;

import java.io.File;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CoreModUtils {
	public static void dump(PrintWriter w, ClassNode classNode){
		for (MethodNode m: classNode.methods) {
			w.println(m.name);
			for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
				AbstractInsnNode insnNode = it.next();
				w.println("\tINSN type: " + insnNode.getType());
				switch(insnNode.getType()){
				case AbstractInsnNode.INSN:{
					w.println("\tINSN");
				}break;
				case AbstractInsnNode.INT_INSN:{
					IntInsnNode node = (IntInsnNode) insnNode;
					w.println("\tINSN");
					w.println("\t\tOperand: " + node.operand);
				}break;
				case AbstractInsnNode.VAR_INSN:{
					VarInsnNode node = (VarInsnNode) insnNode;
					w.println("\tVariable INSN");
					w.println("\t\tVar: " + node.var);
				}break;
				case AbstractInsnNode.TYPE_INSN:{
					TypeInsnNode node = (TypeInsnNode) insnNode;
					w.println("\tType INSN");
					w.println("\t\tDescriptor: '" + node.desc + "'");
				}break;
				case AbstractInsnNode.FIELD_INSN:{
					FieldInsnNode node = (FieldInsnNode) insnNode;
					w.println("\tField INSN");
					w.println("\t\t" + node.owner + "." + node.name + " " + node.desc);
				}break;
				case AbstractInsnNode.METHOD_INSN:{
					w.println("\tMethod INSN");
					MethodInsnNode node = (MethodInsnNode) insnNode;
					w.println("\t\t" + node.owner + "." + node.name  + " " +  node.desc);
					w.println("\t\tITF: " + node.itf);
				}break;
				case AbstractInsnNode.INVOKE_DYNAMIC_INSN:{
					w.println("\tInvoke Dynamic INSN");
					InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) insnNode;
					w.println("\t\t" + node.name + node.desc);
				}break;
				case AbstractInsnNode.JUMP_INSN:{
					w.println("\tJump INSN");
					JumpInsnNode node = (JumpInsnNode) insnNode;
					w.print("\t\tJump To: ");
					dumpLabel(w, node.label.getLabel());
					w.println();
				}break;
				case AbstractInsnNode.LABEL:{
					w.println("\tLabel");
					LabelNode node = (LabelNode) insnNode;
					w.print("\t\t");
					dumpLabel(w, node.getLabel());
					w.println();
				}break;
				case AbstractInsnNode.LDC_INSN:{
					w.println("\tLDC INSN");
					LdcInsnNode node = (LdcInsnNode) insnNode;
					w.println("\t\tValue: '" + node.cst + "'");
				}break;
				case AbstractInsnNode.IINC_INSN: {
					w.println("\tIncrement INSN");
					IincInsnNode node = (IincInsnNode) insnNode;
					w.println("\t\tVar: " + node.var + " Incr: " + node.incr);
				}break;
				case AbstractInsnNode.TABLESWITCH_INSN: {
					w.println("\tTable Switch INSN");
					TableSwitchInsnNode node = (TableSwitchInsnNode) insnNode;
					w.println("\t\tMin: " + node.min + " Max: " + node.max);
					w.print("\t\tdflt: ");
					dumpLabel(w, node.dflt.getLabel());
					w.println();
					w.println("\t\tLabels: " + node.labels.size() + " [");
					int i = 0;
					for (LabelNode n : node.labels) {
						w.print("\t\t\t" + (i++) + ": ");
						dumpLabel(w, n.getLabel());
						w.println(',');
					}
					w.println("\t\t]");
				}break;
				case AbstractInsnNode.LOOKUPSWITCH_INSN: {
					w.println("\tLookup Switch INSN");
					LookupSwitchInsnNode node = (LookupSwitchInsnNode) insnNode;
					w.print("\t\tdflt: ");
					dumpLabel(w, node.dflt.getLabel());
					w.println();
					w.println("\t\tKeys: ");
					w.print("\t\t\t");
					w.println(node.keys);
					w.println("\t\tLabels: " + node.labels.size() + " [");
					int i = 0;
					for (LabelNode n : node.labels) {
						w.print("\t\t\t" + (i++) + ": ");
						dumpLabel(w, n.getLabel());
						w.println(',');
					}
					w.println("\t\t]");
				}break;
				case AbstractInsnNode.MULTIANEWARRAY_INSN: {
					w.println("\tMULTIANEWARRAY INSN");
					MultiANewArrayInsnNode node = (MultiANewArrayInsnNode) insnNode;
					w.println("\t\tDims: " + node.dims);
					w.println("\t\tDesc: " + node.desc);
				}break;
				case AbstractInsnNode.FRAME: {
					w.println("\tFrame");
					FrameNode node = (FrameNode) insnNode;
					w.println("\t\tType: " + node.type);
					w.println("\t\tLocal list:");
					w.print("\t\t\t");
					w.println(node.local);
					w.println("\t\tStack list:");
					w.print("\t\t\t");
					w.println(node.stack);
				}break;
				case AbstractInsnNode.LINE: {
					w.println("\tLine");
					LineNumberNode node = (LineNumberNode) insnNode;
					w.println("\t\tLine: " + node.line);
					w.print("\t\tStart Label: ");
					dumpLabel(w, node.start.getLabel());
					w.println();
				}break;
				default:break;
				}
				if(insnNode.getOpcode() != -1)w.println("\t\tOpcode: " + insnNode.getOpcode());
			}
		}
	}
	private static void dumpLabel(PrintWriter w, Label lbl){
		w.print(lbl.info);
	}
	public static void removeNext(ListIterator<AbstractInsnNode> it, int j) {
		for(int i = 0;i<j;i++){
			it.next();
			it.remove();
		}
	}
	public static void dump(String name, byte[] basicClass){
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		try {
			String fn = name.replace('.', '/');
			new File(".", "asm/" + fn).getParentFile().mkdirs();
			PrintWriter w = new PrintWriter(new File("./asm",  fn + ".txt"));
			CoreModUtils.dump(w, classNode);
			w.close();
		} catch (Throwable e) {
		}
	}
}
