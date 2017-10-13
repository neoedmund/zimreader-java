package neoe.zim;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

public class NeoeZimTest {

	public static void main(String[] args) throws Exception {
		Zim zim = new Zim("D:\\10h\\wikipedia_en_all_2016-12.zim");
		System.out.println("cnt=" + zim.articleCount);
		for (int i = 0; i < 10; i++) {
			zim.getEntry(i);
		}
		System.out.println("-----");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("out1"),"utf8"));
		for (int i = 0; i < 5000; i++) {
			Entry e = zim.getEntry(zim.articleCount - 1 - (i++));
//			if (i % 100 == 0) {
				out.println(e.toString());
//			}
			if (e.title.length() > 0) {
				out.println("i=" + i);
				break;
			}
		}
		out.close();
		System.out.println("-----");

		String key = "Text Editor";
		int pos = zim.findTitlePos(key);
		System.out.println("found pos=" + pos);
		List<String> res = zim.findTitle(pos, 10);
		System.out.println(res);
	}

}
