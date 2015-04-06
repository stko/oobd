package se.krka.kahluax.compiler;

import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaTable;

import java.io.*;

public class LuaCompiler {

	public LuaClosure compile(InputStream input, LuaTable env, String name) throws LuaCompileException, IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		String line = "";
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return compile(sb.toString(), env, name);
	}

	public LuaClosure compile(String input, LuaTable env, String name) throws LuaCompileException, IOException {
		byte data[] = nativeCompile(input, name);
		verifyData(data);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return LuaPrototype.loadByteCode(bais, env);

	}

	private void verifyData(byte[] data) throws LuaCompileException {
		if (data.length == 0) {
			throw new LuaCompileException("Compilation Error: No data received");
		}
		if (data[0] != 27) {
			String s;
			try {
				s = new String(data, "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				s = new String(data);
			}
			throw new LuaCompileException("Compilation Error: " + s);
		}
	}


	private static String determineOS() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName != null) {
			if (osName.startsWith("mac os x")) {
				return "os_macosx";
			}
			if (osName.startsWith("windows")) {
				return "os_windows";
			}
			if (osName.startsWith("linux")) {
				return "os_linux";
			}
			if (osName.startsWith("sun")) {
				return "os_sun";
			}
			return "os_" + osName;
		}
		return "os_unknown";
	}


	private static String determineArch() {
		String osArch = System.getProperty("os.arch").toLowerCase();
		if (osArch != null) {
			if (osArch.equals("i386")) {
				return "arch_i386";
			}
			if (osArch.equals("x86")) {
				return "arch_i386";
			}
			if (osArch.startsWith("amd64") || osArch.startsWith("x86_64")) {
				return "arch_x86_64";
			}
			if (osArch.equals("ppc")) {
				return "arch_ppc";
			}
			if (osArch.startsWith("ppc")) {
				return "arch_ppc_64";
			}
			if (osArch.startsWith("sparc")) {
				return "arch_sparc";
			}
			return "arch_" + osArch;
		}
		return "arch_unknown";
	}

	static {
		String os = determineOS();
		String arch = determineArch();
		String type = os + "_" + arch;

		if (os.equals("os_linux")) {
			if (arch.equals("arch_i386")) {
				type = "linux32";
			} else if (arch.equals("arch_x86_64")) {
				type = "linux64";
			}
		} else if (os.equals("os_windows")) {
			type = "win32";
		} else if (os.equals("os_macosx")) {
			// implement later
		}
		System.loadLibrary("JavaLuac_" + type);
	}

	/**
	 * Performs the actual compilation using a JNI call to luac
	 * @param src the source code to compile
	 * @param name the name of the source code
	 * @return the ready to run lua byte code
	 * @throws LuaCompileException if compilation fails for some reason (e.g. syntax error)
	 */
	public native byte[] nativeCompile(String src, String name) throws LuaCompileException;
}
