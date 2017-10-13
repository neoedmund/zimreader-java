package neoe.zim;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Zim {

	public static boolean debugEntry;
	public int articleCount;
	public String fn;
	public int magicNumber;
	public int version;
	public byte[] uuid = new byte[16];
	public int clusterCount;
	public long urlPtrPos;
	public long titlePtrPos;
	public long clusterPtrPos;
	public long mimeListPos;
	public int mainPage;
	public int layoutPage;
	public ArrayList<String> mMIMETypeList;
	public long checksumPos;
	public long geoIndexPos;
	private Map<Integer, Entry> titleCache = new HashMap<Integer, Entry>();
	private Map<Integer, Entry> urlCache = new HashMap<Integer, Entry>();
	private String url;
	RandomAccessFile f;
	LittleEndianDataInput leu;

	public Zim(String fn) throws Exception {
		initFile(fn);
		readHeader(f);
	}

	private void initFile(String fn) throws FileNotFoundException {
		this.fn = fn;
		f = new RandomAccessFile(fn, "r");
		leu = new LittleEndianDataInput(f);
	}

	public void closeFile() throws IOException {
		if (f != null)
			f.close();
	}

	public Zim(String url, boolean network) throws Exception {
		if (!network) {
			initFile(fn);
			readHeader(f);
		} else {
			this.url = url;
			// currently, only support readHeader for ZIM file on web
			readHeader(new DataInputStream(new ByteArrayInputStream(U.download(url, 88))));
		}

	}

	static final long I4 = (1L << 32) - 1;

	private void readHeader(DataInput in0) throws Exception {

		//
		LittleEndianDataInput in = new LittleEndianDataInput(in0);

		// Read the contents of the header
		magicNumber = in.readInt();
		System.out.println("magic:" + Integer.toHexString(magicNumber));

		version = in.readInt();
		System.out.println("ver:" + Integer.toHexString(version));

		in.readFully(uuid);
		// System.out.println(uuid); read(buffer, 0, 4);

		articleCount = in.readInt();
		System.out.println("count:" + articleCount);

		clusterCount = in.readInt();
		System.out.println(clusterCount);

		urlPtrPos = in.readLong();
		// System.out.println(urlPtrPos);

		titlePtrPos = in.readLong();
		// System.out.println(titlePtrPos);

		clusterPtrPos = in.readLong();
		// System.out.println(clusterPtrPos);

		mimeListPos = in.readLong();
		System.out.println(mimeListPos);

		mainPage = in.readInt();
		System.out.println(mainPage);

		layoutPage = in.readInt();
		System.out.println(layoutPage);
		checksumPos = in.readLong();
		if (mimeListPos >= 88) {
			geoIndexPos = in.readLong();
		}
		// Initialise the MIMETypeList
		if (url != null) {
			return;
		}
		f.seek(mimeListPos);
		mMIMETypeList = new ArrayList<String>();
		while (true) {
			String s = U.toStr(U.readString(in));
			if (s.isEmpty()) {
				break;
			}
			mMIMETypeList.add(s);
		}
		System.out.println("MIME=" + mMIMETypeList);

	}

	public List<String> getTitlesNear(int pos, int cnt) {

		List<String> ret = new ArrayList<>();
		for (int i = pos; i < pos + cnt; i++) {
			if (i >= articleCount || i < 0)
				break;
			ret.add(U.toStr(getEntryByTitleIndex(i).title));
		}
		return ret;
	}

	public int findTitlePos(String s) {
		if (s == null || s.length() == 0)
			return 0;
		return searchNearestTitle(U.toBs(s), 0, articleCount - 1);
	}

	public int findUrlPos(String s) {
		if (s == null || s.length() == 0)
			return 0;
		return searchUrl(U.toBs(s), 0, articleCount - 1);
	}

	private int searchUrl(byte[] s, int i, int j) {
		if (i > j)
			return -1;
		if (i == j) {
			return urlEq(i, s) ? i : -1;
		}
		if (urlEq(i, s))
			return i;
		if (urlEq(j, s))
			return j;

		if (j - i == 1) {
			return -1;
		}
		int k = (i + j) / 2;
		if (k == i)
			k++;
		if (k > j)
			return -1;

		{
			Entry a = getEntryByUrlIndex(k);
			int v = U.bsCompareTo(s, a.url);
			if (v == 0)
				return k;
			if (v < 0) {
				return searchUrl(s, i + 1, k - 1);
			}
			if (v > 0) {
				return searchUrl(s, k + 1, j - 1);
			}
		}
		return -1;
	}

	private boolean urlEq(int i, byte[] bs) {
		Entry a = getEntryByUrlIndex(i);
		return Arrays.equals(a.url, bs);
	}

	private int searchNearestTitle(byte[] s, int i, int j) {

		if (i > j)
			return i;
		if (i == j) {
			return i;
		}
		if (j - i == 1) {
			return i;
		}
		if (isLargeEq(i, s, true))
			return i;
		if (isSmallEq(j, s, true))
			return j;
		int k = (i + j) / 2;

		if (isLargeEq(k, s, false)) {
			return searchNearestTitle(s, i, k);
		} else {
			return searchNearestTitle(s, k, j);
		}
	}

	private boolean isSmallEq(int pos, byte[] s, boolean canEmpty) {
		Entry e = getEntryByTitleIndex(pos);
		if (e.title.length == 0) {
			if (!canEmpty) {
				throw new RuntimeException("not here");
			} else {
				return false;
			}
		}
		int v = U.bsCompareTo(e.title, s);
		return v <= 0;
	}

	private boolean isLargeEq(int pos, byte[] s, boolean canEmpty) {
		Entry e = getEntryByTitleIndex(pos);
		if (e.title.length == 0) {
			if (!canEmpty) {
				// throw new RuntimeException("pos="+pos);
				return true;
			} else {
				return false;
			}
		}

		int v = U.bsCompareTo(e.title, s);
		return v >= 0;
	}

	public Entry getEntryByTitleIndex(int titleIndex) {
		Entry e = titleCache.get(titleIndex);
		if (e == null) {
			try {
				f.seek(titlePtrPos + titleIndex * 4);
				e = getEntryByUrlIndex(leu.readInt());
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
			titleCache.put(titleIndex, e);
		}
		return e;
	}

	public Entry getEntryByUrlIndex(int urlIndex) {
		Entry e = urlCache.get(urlIndex);
		if (e == null) {
			try {
				f.seek(urlPtrPos + urlIndex * 8);
				f.seek(leu.readLong());
				e = new Entry(urlIndex, this);
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
			urlCache.put(urlIndex, e);
		}
		return e;
	}

	public byte[] getContent(int urlIndex) {
		if (urlIndex < 0)
			return null;
		// TODO
		return null;
	}

}
