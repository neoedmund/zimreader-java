package neoe.zim;

import java.io.DataInput;
import java.io.IOException;

/** ZIM directory entry */
public class Entry {

	public byte[] title, url;
	public short mimetype;
	public int type;
	public byte namespace;
	public int revision;
	public int redirectIndex;
	public int cluster;
	public int blob;
	private int urlIndex;
	private Zim zim;

	private void readEntry(DataInput f) throws IOException {
		mimetype = f.readShort();
		if (mimetype == (short) 0xffff) {// redirect
			type = 1;

			f.readByte();
			namespace = f.readByte();
			revision = f.readInt();
			redirectIndex = f.readInt();
			byte[] url1;
			url = U.toBs(String.format("%s/%s", (char) namespace, U.toStr(url1 = U.readString(f))));
			title = U.readString(f);
			if (title.length == 0) {
				//title = url;
			}
		} else if (mimetype == (short) 0xfffe) {
			type = 2;
			throw new RuntimeException("found Linktarget or deleted Entry");
		} else {
			type = 0;

			f.readByte();
			namespace = f.readByte();
			revision = f.readInt();
			cluster = f.readInt();
			blob = f.readInt();
			byte[] url1;
			url = U.toBs(String.format("%s/%s", (char) namespace, U.toStr(url1 = U.readString(f))));
			title = U.readString(f);
			if (title.length == 0) {
				//title = url;
			}
		}
		if (Zim.debugEntry) {
			System.out.println(toString());
		}

	}

	public String toString() {
		if (type == 1) {
			return String.format("(%s)`%s`[%s]->%s", urlIndex, U.toStr(title), U.toStr(url),
					zim.getEntryByUrlIndex(redirectIndex));
		} else {
			return String.format("(%s)`%s`[%s]", urlIndex, U.toStr(title), U.toStr(url));
		}
	}

	public Entry(int urlIndex, Zim zim) throws IOException {
		this.zim = zim;
		this.urlIndex = urlIndex;
		readEntry(zim.leu);
	}

}
