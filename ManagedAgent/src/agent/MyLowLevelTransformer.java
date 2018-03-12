package agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

@SuppressWarnings("unchecked")
public class MyLowLevelTransformer implements ClassFileTransformer {

	private final String classname;
	private final String methodname;

	public MyLowLevelTransformer(String classname, String methodname) {
		this.classname = classname;
		this.methodname = methodname;
	}

	@Override
	public byte[] transform(ClassLoader loader, String name, Class<?> ___, ProtectionDomain __, byte[] classfile) throws IllegalClassFormatException {
		if (!name.equals(classname)) {
			return null;
		}
		
		try {
			ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(classfile)));
			
			List<MethodInfo> methods = file.getMethods();
			for(MethodInfo method : methods) {
				if(method.getName().equals(methodname)) {
					transform(method);
				}
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			file.write(new DataOutputStream(out));
			byte[] data = out.toByteArray();			
			try (FileOutputStream o = new FileOutputStream(classname.replace('/', '.') + ".class")) {
				o.write(data);
			}
			return data;
		} catch (IOException | RuntimeException | BadBytecode e) {
			e.printStackTrace();
			return null;
		}
	}

	private void transform(MethodInfo method) throws BadBytecode {
		CodeAttribute code = method.getCodeAttribute();
		CodeIterator iterator = code.iterator();
		
		iterator.insert(getCodeBefore(method));
		while(iterator.hasNext()) {
			int bci = iterator.next();
			int opcode = iterator.byteAt(bci);
			if (opcode == Opcode.RETURN || opcode == Opcode.ATHROW || opcode == Opcode.IRETURN || opcode == Opcode.LRETURN || opcode == Opcode.FRETURN || opcode == Opcode.DRETURN) {
				iterator.insert(getCodeAfter(method));
			}
		}

		code.computeMaxStack();
		method.rebuildStackMap(ClassPool.getDefault());
	}

	private byte[] getCodeAfter(MethodInfo method) {
		List<String> arguments = getArgumentTypes(method.getDescriptor());
		Bytecode code = new Bytecode(method.getConstPool(), 0, 0);
		// [
		if (method.getDescriptor().endsWith("V")) {
			code.add(Opcode.ACONST_NULL);
		} else {
			code.add(Opcode.DUP);
			wrap(code, method.getDescriptor().substring(method.getDescriptor().indexOf(')') + 1));
		}
		// [ return-value
		code.addIconst(arguments.size());
		// [ return-value, argument count
		code.addAnewarray("java.lang.Object");
		// [ return-value, array
		int displacement = 0;
		for(int i = 0; i < arguments.size(); i++) {
			// [ return-value, array
			code.add(Opcode.DUP);
			// [ return-value, array, array
			code.addIconst(i);
			// [ return-value, array, array, i
			displacement += loadLocal(code, arguments, i, displacement);
			// [ return-value, array, array, i, i-th argument
			code.add(Opcode.AASTORE);
			// [ return-value, array
		}
		// [ return-value, array
		code.addLdc(classname);
		// [ return-value, array, classname
		code.addLdc(methodname);
		// [ return-value, array, classname, methodname
		code.addLconst(0); //TODO
		// [ return-value, array, classname, methodname, time
		code.addInvokestatic(LoggerIntrospection.class.getName(), "logExit",
				"(Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;J)V");
		// [
		return code.get();
	}

	private byte[] getCodeBefore(MethodInfo method) {
		List<String> arguments = getArgumentTypes(method.getDescriptor());
		Bytecode code = new Bytecode(method.getConstPool(), 0, 0);
		// [
		code.addIconst(arguments.size());
		// [ argument count
		code.addAnewarray("java.lang.Object");
		// [ array
		int displacement = 0;
		for(int i = 0; i < arguments.size(); i++) {
			// [ array
			code.add(Opcode.DUP);
			// [ array, array
			code.addIconst(i);
			// [ array, array, i
			displacement += loadLocal(code, arguments, i, displacement);
			// [ array, array, i, i-th argument
			code.add(Opcode.AASTORE);
			// [ array
		}
		// [ array
		code.addLdc(classname);
		// [ array, classname
		code.addLdc(methodname);
		// [ array, classname, methodname
		code.addInvokestatic(LoggerIntrospection.class.getName(), "logEnter",
				"([Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
		// [
		return code.get();
	}

	private int loadLocal(Bytecode code, List<String> arguments, int i, int displacement) {
		switch(arguments.get(i)) {
			case "B":
				code.addIload(i + displacement);
				code.addInvokestatic("java.lang.Byte", "valueOf", "(B)Ljava/lang/Byte;");
				return 0;
			case "S":
				code.addIload(i + displacement);
				code.addInvokestatic("java.lang.Short", "valueOf", "(S)Ljava/lang/Short;");
				return 0;
			case "I":
				code.addIload(i + displacement);
				code.addInvokestatic("java.lang.Integer", "valueOf", "(I)Ljava/lang/Integer;");
				return 0;
			case "J":
				code.addLload(i + displacement);
				code.addInvokestatic("java.lang.Long", "valueOf", "(J)Ljava/lang/Long;");
				return 1;
			case "F":
				code.addFload(i + displacement);
				code.addInvokestatic("java.lang.Float", "valueOf", "(F)Ljava/lang/Float;");
				return 0;
			case "D":
				code.addDload(i + displacement);
				code.addInvokestatic("java.lang.Double", "valueOf", "(D)Ljava/lang/Double;");
				return 1;
			case "Z":
				code.addIload(i + displacement);
				code.addInvokestatic("java.lang.Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
				return 0;
			case "C":
				code.addIload(i + displacement);
				code.addInvokestatic("java.lang.Character", "valueOf", "(C)Ljava/lang/Character;");
				return 0;
			default: // L, [
				code.addAload(i + displacement);
				return 0;
		}
	}

	private void wrap(Bytecode code, String type) {
		switch(type) {
			case "B":
				code.addInvokestatic("java.lang.Byte", "valueOf", "(B)Ljava/lang/Byte;");
				break;
			case "S":
				code.addInvokestatic("java.lang.Short", "valueOf", "(S)Ljava/lang/Short;");
				break;
			case "I":
				code.addInvokestatic("java.lang.Integer", "valueOf", "(I)Ljava/lang/Integer;");
				break;
			case "J":
				code.addInvokestatic("java.lang.Long", "valueOf", "(J)Ljava/lang/Long;");
				break;
			case "F":
				code.addInvokestatic("java.lang.Float", "valueOf", "(F)Ljava/lang/Float;");
				break;
			case "D":
				code.addInvokestatic("java.lang.Double", "valueOf", "(D)Ljava/lang/Double;");
				break;
			case "Z":
				code.addInvokestatic("java.lang.Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
				break;
			case "C":
				code.addInvokestatic("java.lang.Character", "valueOf", "(C)Ljava/lang/Character;");
				break;
			default: // L, [
				break;
		}
	}

	//"([Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;IJ)V"
	private List<String> getArgumentTypes(String descriptor) {
		List<String> result = new ArrayList<>();
		for(int i = 1; descriptor.charAt(i) != ')'; i++) {
			char c = descriptor.charAt(i);
			String type = "";
			while(c == '[') {
				type += "[";
				i++;
				c = descriptor.charAt(i);
			}
			if (c == 'L') {
				int semicolon = descriptor.indexOf(";", i + 1);
				type += descriptor.substring(i + 1, semicolon);
				i = semicolon;
			} else {
				type += c;
			}
			result.add(type);
		}
		return result;
	}

}
















