package neoe.zim;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Entry {

	public String title, url;
	public short mimetype;
	public int type;
	public byte namespace;
	public int revision;
	public int redirectIndex;
	public int cluster;
	public int blob;
	private int pos;
	private Zim zim;

	private void read(DataInput f) throws IOException {
		mimetype = f.readShort();
		if (mimetype == (short) 0xffff) {
			type = 1;

			f.readByte();
			namespace = f.readByte();
			revision = f.readInt();
			redirectIndex = f.readInt();
			url = Zim.readString(f);
			title = Zim.readString(f);
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
			url = Zim.readString(f);
			title = Zim.readString(f);

		}
		if (Zim.debugEntry) {
			System.out.println(toString());
		}

	}

	public String toString() {
		if (type == 1) {
			String redirect;
			try {
				RandomAccessFile f = new RandomAccessFile(zim.fn, "r");
				LittleEndianDataInput leu = new LittleEndianDataInput(f);
				zim.seekURL(f, leu, redirectIndex);
				redirect = new Entry(pos, leu, zim).toString();
			} catch (IOException e) {
				e.printStackTrace();
				redirect = e.toString();
			}
			return String.format("(%s)`%s`[%s]->%s", pos, title, url, redirect);
		} else {
			return String.format("(%s)`%s`[%s]", pos, title, url);
		}
	}

	public Entry(int pos, LittleEndianDataInput leu, Zim zim) throws IOException {
		this.zim = zim;
		this.pos = pos;
		read(leu);
	}

}
