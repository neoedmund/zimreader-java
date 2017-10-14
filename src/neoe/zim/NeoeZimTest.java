package neoe.zim;

import java.util.List;

public class NeoeZimTest {

	public static void main(String[] args) throws Exception {
		{
			Zim zim = new Zim("D:\\10h\\wikipedia_en_all_2016-12.zim");
			System.out.println("cnt=" + zim.articleCount);
			System.out.println("--- first titles --");
			for (int i = 0; i < 100; i++) {
				System.out.println(zim.getEntryByTitleIndex(i));
			}
			System.out.println("--- last titles ---");
			// PrintWriter out = new PrintWriter(new OutputStreamWriter(new
			// FileOutputStream("out1"), "utf8"));
			for (int i = 0; i < 100; i++) {
				System.out.println(zim.getEntryByTitleIndex(zim.articleCount - 1 - i));
			}

			String key = "Java";
			System.out.println("---search by title  `" + key + "` ---");
//			Zim.debugEntry = true;

			int pos = zim.findTitlePos(key);
			System.out.println("found pos=" + pos);
			List<String> res = zim.getTitlesNear(pos, 10);
			System.out.println(res);

			{
				String[] urls = { "A/Java_(programming_language).html", "I/m/1913-world-series-polo-grounds.jpg",
						"I/m/鹽埔鄉新圍社區德協路旁被稱為大仁哥樹的苦楝樹.jpg" };
				for (String url : urls) {
					System.out.println("--- search url " + url + " ---");
					int urlIndex = zim.findUrlPos(url);
					System.out.println("result urlIndex=" + zim.getEntryByUrlIndex(urlIndex));
					byte[] bs = zim.getContent(urlIndex);
					if (bs != null) {
						U.saveFile(url, bs);
					}
				}
			}
		}

		// {
		// Zim zim = new Zim(
		// "http://download.kiwix.org/zim/stack_exchange/blender.stackexchange.com_en_all_2017-07.zim",
		// true);
		// System.out.println("cnt=" + zim.articleCount);
		// }

	}

}
