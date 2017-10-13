package neoe.zim;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/** */
public class Downloader {

	public static final byte[] emptyBA = new byte[0];

	private static final int MAX_RETRY = 5;

	public byte[] ba;

	String enc = "UTF8";

	private long len;

	private String name;

	Proxy proxy;
	public boolean readContent = true;
	Map reqHeader;
	public Map respHeader;
	int retry;
	private long start;
	String url;
	private boolean usePart;
	boolean useProxy;

	public Downloader(String name) {
		this.name = name;
	}

	public void download(String url, long start, long len, boolean readContent) throws Exception {
		this.url = url;
		reqHeader = new HashMap<>();
		reqHeader.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:47.0) Gecko/20100101 Firefox/57.0");
		setPart(start, len);
		this.readContent = readContent;
		run();
	}

	int redirect = 0;

	public void run() throws Exception {
		ba = emptyBA;
		retry = 0;
		while (true) {
			retry += 1;
			// safeguard
			if (retry > MAX_RETRY)
				return;

			URL u = new URL(url);
			URLConnection conn;
			if (useProxy) {
				// Log.log(String.format("[D]connect via proxy", url, proxy));
				conn = u.openConnection(proxy);
			} else {
				// Log.log(String.format("[D]connect"));
				conn = u.openConnection();
			}
			conn.setConnectTimeout(9000);
			// set headers
			for (Object o : reqHeader.keySet()) {
				conn.setRequestProperty((String) o, reqHeader.get(o).toString());
			}
			boolean error = false;
			Exception ex1 = null;
			try {
				respHeader = conn.getHeaderFields();
				{
					String loc = getStr((List) respHeader.get("Location"));
					if (loc != null && !loc.isEmpty()) {
						redirect++;
						if (redirect > 10) {
							throw new RuntimeException("too many redirect");
						}
						System.out.println(String.format("source %s redirect(%s) to %s", name, redirect, loc));
						url = loc;
						run();
						return;
					}
				}
				// Log.log(String.format("[DD %s|%s]respHeader=%s", name,
				// reqHeader.get("Range"), respHeader));
				if (readContent) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					copy(conn.getInputStream(), baos);
					ba = baos.toByteArray();
					int len1 = ba.length;
					{// gzip enc
						Object encoding = respHeader.get("Content-Encoding"); // java.util.Collections$UnmodifiableRandomAccessList
						Object te = respHeader.get("Transfer-Encoding");
						// Log.log("encoding="+encoding);
						if (encoding != null && encoding.toString().toLowerCase().indexOf("gzip") >= 0) {
							// gzipped
							GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(ba));
							ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
							copy(gzip, baos2);
							ba = baos2.toByteArray();
							int len2 = ba.length;
							System.out.println(String.format("[D]extract gzip %d bytes -> %d bytes", len1, len2));
						}

					}

					// Log.log(String.format("[D]downloaded %s(%d bytes)", url,
					// ba.length));
				}
			} catch (Exception ex) {
				error = true;
				ex1 = ex;
			}
			if (error) {
				throw new RuntimeException("download fail via proxy:" + proxy, ex1);
			} else {
				return;
			}
		}
	}

	// private void say(String s) {
	// Log.log(name + ":" + s);
	// }

	public static void copy(InputStream in, OutputStream outstream) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outstream);
		byte[] buf = new byte[1024 * 16];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	private static String getStr(List list) {
		if (list == null || list.size() <= 0)
			return null;
		return (String) list.get(0);
	}

	private void setPart(long start, long len) {
		this.start = start;
		this.len = len;
		this.usePart = true;
		reqHeader.put("Range", String.format("bytes=%d-%d", start, start + len - 1));
	}
}
