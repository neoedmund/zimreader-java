package neoe.zim;

import java.util.List;

public class NeoeZimTest {

	public static void main(String[] args) throws Exception {
		Zim zim = new Zim("D:\\10h\\wikipedia_en_all_2016-12.zim");
		System.out.println("cnt=" + zim.articleCount);
		for (int i = 0; i < 10; i++) {
			zim.getEntry(i);
		}
		System.out.println("-----");
		if (false) {
			int i = 0;
			while (true) {
				Entry e = zim.getEntry(zim.articleCount - 1 - (i++));
				if (i % 100 == 0) {
					System.out.println(e.toString());
				}
				if (e.title.length() > 0) {
					break;
				}
			}
			System.out.println("i=" + i);
			System.out.println("-----");
		}
		String key = "Text Editor";
		int pos = zim.findTitlePos(key);
		System.out.println("found pos=" + pos);
		List<String> res = zim.findTitle(pos, 10);
		System.out.println(res);
	}

}
