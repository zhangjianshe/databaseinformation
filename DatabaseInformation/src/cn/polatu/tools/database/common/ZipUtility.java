/**
 * 2010-4-12
 */
package cn.polatu.tools.database.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ZIP压缩工具
 * 
 * @author 梁栋
 * @since 1.0
 */
public class ZipUtility {

	public static final String EXT = ".zip";
	private static final String BASE_DIR = "";
	private static final String PATH = File.separator;
	private static final int BUFFER = 1024;

	public static String getClassFile(Class clazz) {
		String url;
		try {
			url = getClassPath(clazz);
			if (url != null) {
				int pos = url.lastIndexOf("!");
				if (pos > 0) {
					return url.substring(0, pos);
				}
				return null;
			}
			return null;
		} catch (Exception e) {
			return null;
		}

	}
	
	/**
	 * 获取类所在JAR文件的所在路径
	 * @param clazz
	 * @return
	 */
	public static String getJarPath(Class clazz) {
		String url;
		try {
			url = getClassFile(clazz);
			if (url != null) {
				if(File.separator.equals("/"))
				{
					//linux
					int pos = url.lastIndexOf(File.separator);
					if (pos > 0) {
						return url.substring(0, pos);
					}
					return null;
				}
				else
				{
					int pos = url.lastIndexOf("/");
					if (pos > 0) {
						String t= url.substring(1, pos);
						t=t.replace('/', '\\');
						return t;
					}
					return null;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 得到类的路径，例如E:/workspace/JavaGUI/bin/com/util
	 * 
	 * @return
	 * @throws java.lang.Exception
	 */
	public static String getClassPath(Class clazz) throws Exception {
		try {
			String strClassName = clazz.getName();
			String strPackageName = "";
			if (clazz.getPackage() != null) {
				strPackageName = clazz.getPackage().getName();
			}
			String strClassFileName = "";
			if (!"".equals(strPackageName)) {
				strClassFileName = strClassName.substring(
						strPackageName.length() + 1, strClassName.length());
			} else {
				strClassFileName = strClassName;
			}

			URL url = null;
			url = clazz.getResource(strClassFileName + ".class");
			String strURL = url.toString();
			strURL = strURL.substring(strURL.indexOf('/') ,
					strURL.lastIndexOf('/'));
			// 返回当前类的路径，并且处理路径中的空格，因为在路径中出现的空格如果不处理的话，
			// //在访问时就会从空格处断开，那么也就取不到完整的信息了，这个问题在web开发中尤其要注意 return
			// strURL.replaceAll("%, " ");

			return strURL;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 文件 解压缩
	 * 
	 * @param srcPath
	 *            源文件路径
	 * 
	 * @throws Exception
	 */
	public static void decompress(String srcPath, String sourcepath,
			String replacepath) throws Exception {
		File srcFile = new File(srcPath);

		decompress(srcFile, sourcepath, replacepath);
	}

	/**
	 * 解压缩
	 * 
	 * @param srcFile
	 * @throws Exception
	 */
	public static void decompress(File srcFile, String sourcepath,
			String replacepath) throws Exception {
		String basePath = srcFile.getParent();
		decompress(srcFile, basePath, sourcepath, replacepath);
	}

	/**
	 * 解压缩
	 * 
	 * @param srcFile
	 * @param destFile
	 * @throws Exception
	 */
	public static void decompress(File srcFile, File destFile,
			String sourcepath, String replacepath) throws Exception {

		CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
				srcFile), new CRC32());

		ZipInputStream zis = new ZipInputStream(cis);

		decompress(destFile, zis, sourcepath, replacepath);

		zis.close();

	}

	/**
	 * 解压缩
	 * 
	 * @param srcFile
	 * @param destPath
	 * @throws Exception
	 */
	public static void decompress(File srcFile, String destPath,
			String sourcepath, String replacepath) throws Exception {
		decompress(srcFile, new File(destPath), sourcepath, replacepath);

	}

	/**
	 * 文件 解压缩
	 * 
	 * @param srcPath
	 *            源文件路径
	 * @param destPath
	 *            目标文件路径
	 * @throws Exception
	 */
	public static void decompress(String srcPath, String destPath,
			String sourcePath, String replacepath) throws Exception {

		File srcFile = new File(srcPath);
		decompress(srcFile, destPath, sourcePath, replacepath);
	}

	/**
	 * 文件 解压缩
	 * 
	 * @param destFile
	 *            目标文件
	 * @param zis
	 *            ZipInputStream
	 * @throws Exception
	 */
	private static void decompress(File destFile, ZipInputStream zis,
			String sourcepath, String replacepath) throws Exception {

		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {

			if (sourcepath != null && sourcepath.length() > 0) {
				// 需要判断是否要解压
				if (entry.getName().startsWith(sourcepath)) {

					String dir = destFile.getPath() + File.separator
							+ entry.getName();

					if (replacepath != null) {
						dir = destFile.getPath()
								+ File.separator
								+ replacepath
								+ File.separator
								+ entry.getName()
										.substring(sourcepath.length());
					}

					System.out.println("****Extacte " + entry.getName()
							+ "  to " + dir);

					File dirFile = new File(dir);

					// 文件检查
					fileProber(dirFile);

					if (entry.isDirectory()) {
						dirFile.mkdirs();
					} else {
						decompressFile(dirFile, zis);
					}
				}

			} else {
				String dir = destFile.getPath() + File.separator
						+ entry.getName();

				File dirFile = new File(dir);

				// 文件检查
				fileProber(dirFile);

				if (entry.isDirectory()) {
					dirFile.mkdirs();
				} else {
					decompressFile(dirFile, zis);
				}

			}
			zis.closeEntry();
		}
	}

	/**
	 * 文件探针
	 * 
	 * 
	 * 当父目录不存在时，创建目录！
	 * 
	 * 
	 * @param dirFile
	 */
	private static void fileProber(File dirFile) {

		File parentFile = dirFile.getParentFile();
		if (!parentFile.exists()) {

			// 递归寻找上级目录
			fileProber(parentFile);

			parentFile.mkdir();
		}

	}

	/**
	 * 文件解压缩
	 * 
	 * @param destFile
	 *            目标文件
	 * @param zis
	 *            ZipInputStream
	 * @throws Exception
	 */
	private static void decompressFile(File destFile, ZipInputStream zis)
			throws Exception {

		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(destFile));

		int count;
		byte data[] = new byte[BUFFER];
		while ((count = zis.read(data, 0, BUFFER)) != -1) {
			bos.write(data, 0, count);
		}

		bos.close();
	}

}