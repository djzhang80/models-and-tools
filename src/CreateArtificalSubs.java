import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.IOUtils;

import sun.nio.cs.ext.IBM1025;

public class CreateArtificalSubs {
	public static String hurNoInfo = "               2    | HRUTOT : Total number of HRUs modeled in subbasin";
	public static String hruNoInfo_prefix = "            ";
	public static String hruNoInfo_subfix = "    | HRUTOT : Total number of HRUs modeled in subbasin";

	public static File[] listAllSubs(String filePath) {

		File dir = new File(filePath);

		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.getName().startsWith("output"))
						&& file.getName().endsWith(".sub");
			}
		};
		File[] files = dir.listFiles(fileFilter);

		return files;
	}

	public static File[] listAllHrus(String filePath) {

		File dir = new File(filePath);

		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.getName().startsWith("output"))
						&& (file.getName().endsWith(".hru")
								|| file.getName().endsWith(".gw")
								|| file.getName().endsWith(".sol")
								|| file.getName().endsWith(".chm")
								|| file.getName().endsWith(".ops")
								|| file.getName().endsWith(".sep") || file
								.getName().endsWith(".mgt"));
			}
		};
		File[] files = dir.listFiles(fileFilter);

		return files;
	}

	public static void main(String[] args) {
		// parameter one: the path to find models
		// parameter two: the number of HRUs in each subbasin
		// parameter three: how many models need to be created
		int hru_count = Integer.parseInt(args[1]);

		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}


			String model_path = args[0];
			File[] files = listAllSubs(model_path);
			String hruno = String.format("% 4d", hru_count);

			for (int i = 0; i < files.length; i++) {
				editSubFile(files[i], hruno, model_path);
			}
		

		// delete other hru files

		File[] hruFiles = listAllHrus(args[0]);

		for (int i = 0; i < hruFiles.length; i++) {
			int thruno = Integer
					.parseInt(hruFiles[i].getName().substring(5, 9));
			if (thruno > hru_count) {
				hruFiles[i].delete();
			}
		}

	}

	public static void editSubFile(File file1, String hruno, String base) {

		FileReader filereader = null;
		OutputStream outputStream = null;
		int ihruno = Integer.parseInt(hruno.trim());
		int pos = 0;
		String subno = file1.getName().substring(0, 5);
		try {
			filereader = new FileReader(file1);

			List<String> lines = IOUtils.readLines(filereader);

			for (int i = 0; i < lines.size(); i++) {

				if (lines.get(i).contains(
						"HRUTOT : Total number of HRUs modeled in subbasin")) {
					System.out.println(lines.get(i));
					String token = lines.get(i).split("\\|")[0];
					System.out.println(token);
					lines.set(i, hruNoInfo_prefix + hruno + hruNoInfo_subfix);
				}
				if (lines.get(i).contains("HRU: General")) {
					pos = i + 1;
				}
			}

			System.out.println(lines.size());
			for (int i = lines.size() - 1; i >= pos; i--) {
				lines.remove(i);
			}
			int zz = 1;
			for (int k = pos; k < pos + ihruno; k++) {
				String element = "xxxxxxxxx.hruxxxxxxxxx.mgtxxxxxxxxx.solxxxxxxxxx.chm xxxxxxxxx.gwxxxxxxxxx.opsxxxxxxxxx.sep";
				element = element.replaceAll("xxxxxxxxx",
						subno + String.format("%04d", zz));
				lines.add(element);
				zz++;
			}

			try {
				filereader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			outputStream = new FileOutputStream(file1);

			IOUtils.writeLines(lines, IOUtils.LINE_SEPARATOR, outputStream);

			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int k = 1; k <= ihruno; k++) {
				String src = "000010001";
				String des = subno + String.format("%04d", k);
				// "xxxxxxxxx.hruxxxxxxxxx.mgtxxxxxxxxx.solxxxxxxxxx.chm xxxxxxxxx.gwxxxxxxxxx.opsxxxxxxxxx.sep"
				copyfile(base, src + ".hru", des + ".hru");
				copyfile(base, src + ".mgt", des + ".mgt");
				copyfile(base, src + ".sol", des + ".sol");
				copyfile(base, src + ".chm", des + ".chm");
				copyfile(base, src + ".gw", des + ".gw");
				copyfile(base, src + ".ops", des + ".ops");
				copyfile(base, src + ".sep", des + ".sep");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

	}

	public static void copyfile(String directory, String src, String des) {
		Path path = Paths.get(directory);
		Path p1 = path.resolve(src);
		Path p2 = path.resolve(des);
		try {
			Files.copy(p1, p2);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			// e.printStackTrace();
		}
	}
}
