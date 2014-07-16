package com.github.sixro.vodafoneitalymavenplugin.util;

import java.io.*;
import java.security.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Checksum {

	private Checksum() {
	}

	public static class Cksum {

		public static String valueOf(File file) {
			try {
				Process process = Cksum.exec("cksum " + file.getPath());
				return Cksum.firstPart(Cksum.firstLineOutput(process));
			} catch (InterruptedException e) {
				throw new RuntimeException("unable to calculate cksum of file " + file, e);
			} catch (IOException e) {
				throw new RuntimeException("unable to calculate cksum of file " + file, e);
			}
		}

		private static Process exec(String string) throws IOException, InterruptedException {
			Process process = Runtime.getRuntime().exec(string);
			process.waitFor();
			return process;
		}

		private static String firstPart(String line) {
			return StringUtils.split(line, ' ')[0];
		}

		private static String firstLineOutput(Process process) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = reader.readLine();
			reader.close();
			return line;
		}

	}

	public static class MD5 {

		public static String valueOf(File file) {
			FileInputStream in = null;
			
			try {
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");

				in = new FileInputStream(file);
				byte[] dataBytes = new byte[1024];
				int nread = 0;
				while ((nread = in.read(dataBytes)) != -1)
					messageDigest.update(dataBytes, 0, nread);
				byte[] mdbytes = messageDigest.digest();

				return toHex(mdbytes);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("unable to find MD5 algorithm", e);
			} catch (IOException e) {
				throw new RuntimeException("unable to calculate MD5 of file " + file, e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		private static String toHex(byte[] mdbytes) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++)
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			return sb.toString();
		}
	}

}
