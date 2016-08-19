package com.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import com.nothome.delta.GDiffPatcher;

public class DiffTool {

	private final static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[((b[i] & 0xF0) >>> 4)]);
			sb.append(hexChar[(b[i] & 0xF)]);
		}
		return sb.toString();
	}

	public static String getMD5(File file) {
		InputStream fis = null;
		String str = null;
		try {
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}

			str = toHexString(md5.digest());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {

				}
			}
		}

		return str;
	}

	private static File mergeFile(final String source, final String patch,
			String target) throws Exception {
		GDiffPatcher patcher = new GDiffPatcher();
		File deffFile = new File(patch);
		File updatedFile = new File(target);
		patcher.patch(new File(source), deffFile, updatedFile);
		return updatedFile;
	}

	public static File mergeApk(final String source, final String patch,
			final String target, String newApkMd5) throws Exception {
		File updateFile = mergeFile(source, patch, target);
		String ufpMd5 = getMD5(updateFile);
		System.out
				.println("服务端下发的md5:" + newApkMd5 + ",新合并后的apk MD5:" + ufpMd5);
		if (ufpMd5 == null || !newApkMd5.equalsIgnoreCase(ufpMd5)) {
			if (updateFile.exists()) {
				updateFile.delete();
			}
			throw new Exception("MD5错误,不能成功合并!");
		}

		return updateFile;
	}

	public static void main(String args[]) throws Exception {

		try {
			System.out.println("old Md5:"
					+ DiffTool.getMD5(new File("d:/diff/appstore2024.apk")));
			File sourceFile = new File("d:/diff/appstore2017.apk");
			File patchFile = new File("d:/diff/appstore.patch");
			File outputFile = new File("d:/diff/appstore2025.apk");

			if (sourceFile.length() > Integer.MAX_VALUE
					|| patchFile.length() > Integer.MAX_VALUE) {
				System.err
						.println("source or patch is too large, max length is "
								+ Integer.MAX_VALUE);
				System.err.println("aborting..");
				return;
			}
			GDiffPatcher patcher = new GDiffPatcher();
			patcher.patch(sourceFile, patchFile, outputFile);

			System.out.println("finished patching file");

		} catch (Exception ioe) { // gls031504a
			System.err.println("error while patching: " + ioe);
		}

		System.out.println("new Md5:"
				+ DiffTool.getMD5(new File("d:/diff/appstore2025.apk")));

	}

}
