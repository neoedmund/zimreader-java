package neoe.zim;

public class U {

	public static byte[] download(String url, int len) throws Exception {
		Downloader down = new Downloader("agent");
		down.download(url, 0, len, true);
		return down.ba;
	}

}
