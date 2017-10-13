package neoe.zim;

import java.io.DataInput;
import java.io.IOException;

public class LittleEndianDataInput implements DataInput {

	public LittleEndianDataInput(DataInput in) {
		this.in = in;
		w = new byte[8];
	}

	public final short readShort() throws IOException {
		in.readFully(w, 0, 2);
		return (short) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	/**
	 * like DataInputStream.readInt except little endian.
	 */
	public final int readInt() throws IOException {
		in.readFully(w, 0, 4);
		return (w[3]) << 24 | (w[2] & 0xff) << 16 | (w[1] & 0xff) << 8 | (w[0] & 0xff);
	}

	/**
	 * like DataInputStream.readLong except little endian.
	 */
	public final long readLong() throws IOException {
		in.readFully(w, 0, 8);
		return (long) (w[7]) << 56 | (long) (w[6] & 0xff) << 48 | (long) (w[5] & 0xff) << 40
				| (long) (w[4] & 0xff) << 32 | (long) (w[3] & 0xff) << 24 | (long) (w[2] & 0xff) << 16
				| (long) (w[1] & 0xff) << 8 | (long) (w[0] & 0xff);
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public final void readFully(byte b[]) throws IOException {
		in.readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		in.readFully(b, off, len);
	}

	public final int skipBytes(int n) throws IOException {
		return in.skipBytes(n);
	}

	public final boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	public final byte readByte() throws IOException {
		return in.readByte();
	}

	// DataInputStream
	private DataInput in; // to get at the low-level read methods of
	// InputStream
	private byte w[]; // work array for buffering input

	public int readUnsignedShort() throws IOException {
		return readShort();
	}

	@Override
	public char readChar() throws IOException {
		return (char) in.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return readByte();
	}

	@Override
	public String readLine() throws IOException {
		return in.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}
}
