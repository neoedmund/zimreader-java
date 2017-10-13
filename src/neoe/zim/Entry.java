package neoe.zim;

import java.io.DataInput;
import java.io.IOException;

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

	}

	public String toString() {
		return String.format("(%s),t%s,`%s`[%s]", pos, type, title, url);
	}

	public Entry(int pos, LittleEndianDataInput leu) throws IOException {
		this.pos = pos;
		read(leu);
	}

}
