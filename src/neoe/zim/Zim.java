package neoe.zim;

import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Zim {

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
	private Map<Integer, Entry> entryCache = new HashMap<Integer, Entry>();

	public Zim(String fn) throws Exception {
		this.fn = fn;
		readHeader();
	}

	static final long I4 = (1L << 32) - 1;

	private void readHeader() throws Exception {

		//
		LittleEndianDataInput in = new LittleEndianDataInput(new RandomAccessFile(fn, "r"));

		// Read the contents of the header
		magicNumber = in.readInt();
		System.out.println("magic:" + Integer.toHexString(magicNumber));

		version = in.readInt();
		System.out.println("ver:" + Integer.toHexString(version));

		in.readFully(uuid);
		// System.out.println(uuid); read(buffer, 0, 4);

		articleCount = in.readInt();
		System.out.println(articleCount);

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
		geoIndexPos = in.readLong();
		// Initialise the MIMETypeList
		mMIMETypeList = new ArrayList<String>();
		while (true) {
			String s = readString(in);
			if (s.isEmpty()) {
				break;
			}
			mMIMETypeList.add(s);
		}
		System.out.println("MIME=" + mMIMETypeList);

	}

	public static String readString(DataInput in) throws IOException {
		StringBuffer sb = new StringBuffer();
		while (true) {
			char c = in.readChar();
			if (c == 0) {
				break;
			}
			sb.append(c);
		}
		String s = sb.toString();
		return s;
	}

	public List<String> findTitle(int pos, int cnt) {

		List<String> ret = new ArrayList();
		for (int i = pos; i < pos + cnt; i++) {
			if (i >= articleCount || i < 0)
				break;
			ret.add(getEntry(i).title);
		}
		return ret;
	}

	public int findTitlePos(String s) {
		if (s == null || s.length() == 0)
			return 0;
		return binarySearchForNearIndex(s, 0, articleCount - 1);
	}

	private int binarySearchForNearIndex(String s, int i, int j) {

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
			return binarySearchForNearIndex(s, i, k);
		} else {
			return binarySearchForNearIndex(s, k, j);
		}
	}

	private boolean isSmallEq(int pos, String s, boolean canEmpty) {
		Entry e = getEntry(pos);
		if (e.title.length() == 0) {
			if (!canEmpty) {
				throw new RuntimeException("not here");
			} else {
				return false;
			}
		}
		int v = e.title.compareTo(s);
		return v <= 0;
	}

	private boolean isLargeEq(int pos, String s, boolean canEmpty) {
		Entry e = getEntry(pos);
		if (e.title.length() == 0) {
			if (!canEmpty) {
				// throw new RuntimeException("pos="+pos);
				return true;
			} else {
				return false;
			}
		}

		int v = e.title.compareTo(s);
		return v >= 0;
	}

	public Entry getEntry(int pos) {
		Entry e = entryCache.get(pos);
		if (e == null) {
			e = _getEntry(pos);
			entryCache.put(pos, e);
		}
		return e;
	}

	private Entry _getEntry(int pos) {
		try {
			RandomAccessFile f = new RandomAccessFile(fn, "r");
			LittleEndianDataInput leu = new LittleEndianDataInput(f);
			long tp = titlePtrPos + pos * 4;
			f.seek(tp);
			int tpv = leu.readInt();
			long up = urlPtrPos + tpv * 8;
			f.seek(up);
			long upv = leu.readLong();
			f.seek(upv);
			Entry e = new Entry(pos, leu);
			f.close();
			return e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
