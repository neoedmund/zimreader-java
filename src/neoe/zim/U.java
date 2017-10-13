package neoe.zim;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class U {

	public static byte[] download(String url, int len) throws Exception {
		Downloader down = new Downloader("agent");
		down.download(url, 0, len, true);
		return down.ba;
	}

	public static void saveFile(String url, byte[] bs) throws IOException {
		File f = new File("dump/" + url);
		f.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(f);
		out.write(bs);
		out.close();
	}

	public static byte[] readString(DataInput in) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		while (true) {
			byte c = in.readByte();
			if (c == 0) {
				break;
			}
			ba.write(c);
		}
		// String s = ba.toString("UTF8");
		return ba.toByteArray();
	}

	public static String toStr(byte[] bs) {
		try {
			return new String(bs, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("not here");
		}
	}

	public static byte[] toBs(String s) {
		try {
			return s.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("not here");
		}
	}

	public static int bsCompareTo(byte[] a, byte[] b) {
		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; i++) {
			int x = a[i] & 0xff;
			int y = b[i] & 0xff;
			if (x < y)
				return -1;
			if (x > y)
				return 1;
		}
		if (a.length > b.length)
			return 1;
		if (a.length < b.length)
			return -1;
		return 0;
	}

}
