/*
 * Copyright (C) 2011 Arunesh Mathur
 * 
 * This file is a part of zimreader-java.
 *
 * zimreader-java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3.0 as 
 * published by the Free Software Foundation.
 *
 * zimreader-java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with zimreader-java.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzim.ZIMTypes;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.openzim.util.RandomAcessFileZIMInputStream;
import org.openzim.util.Utilities;
import org.tukaani.xz.SingleXZInputStream;

/**
 * @author Arunesh Mathur
 * 
 *         A ZIMReader that reads data from the ZIMFile
 * 
 */
public class ZIMReader {

	private ZIMFile mFile;
	private RandomAcessFileZIMInputStream mReader;

	public ZIMReader(ZIMFile file) {
		this.mFile = file;
		try {
			mReader = new RandomAcessFileZIMInputStream(new RandomAccessFile(
					mFile, "r"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<String> getURLListByURL() throws IOException {

		int i = 0, pos, mimeType;

		byte[] buffer = new byte[8];

		// The list that will eventually return the list of URL's
		ArrayList<String> returnList = new ArrayList<String>();

		// Move to the spot where URL's are listed
		mReader.seek(mFile.getUrlPtrPos());

		for (i = 0; i < mFile.getArticleCount(); i++) {

			// The position of URL i
			pos = mReader.readEightLittleEndianBytesValue(buffer);

			// Mark the current position that we need to return to
			mReader.mark();

			// Move to the position of URL i
			mReader.seek(pos);

			// Article or Redirect entry?
			mimeType = mReader.readTwoLittleEndianBytesValue(buffer);

			if (mimeType == 65535) {
				mReader.seek(pos + 12);
				returnList.add(mReader.readString());
			} else {
				mReader.seek(pos + 16);
				returnList.add(mReader.readString());
			}

			mReader.reset();
		}

		return returnList;
	}

	public List<String> getURLListByTitle() throws IOException {

		int i = 0, pos, mimeType, articleNumber, urlPtrPos;

		byte[] buffer = new byte[8];

		// The list that will eventually return the list of URL's
		ArrayList<String> returnList = new ArrayList<String>();

		// Get the UrlPtrPos or one time storage
		urlPtrPos = mFile.getUrlPtrPos();

		// Move to the spot where URL's are listed
		mReader.seek(mFile.getTitlePtrPos());

		for (i = 0; i < mFile.getArticleCount(); i++) {

			// The articleNumber of the position of URL i
			articleNumber = mReader.readFourLittleEndianBytesValue(buffer);

			// Mark the current position that we need to return to
			mReader.mark();

			mReader.seek(urlPtrPos + (8 * (articleNumber)));

			// The position of URL i
			pos = mReader.readEightLittleEndianBytesValue(buffer);
			mReader.seek(pos);

			// Article or Redirect entry?
			mimeType = mReader.readTwoLittleEndianBytesValue(buffer);

			if (mimeType == 65535) {
				mReader.seek(pos + 12);
				String url = mReader.readString();
				returnList.add(url);
			} else {
				mReader.seek(pos + 16);
				String url = mReader.readString();
				returnList.add(url);
			}

			// Return to the marked position
			mReader.reset();
		}

		return returnList;
	}

	// Gives the minimum required information needed for the given articleName
	public DirectoryEntry getDirectoryInfo(String articleName, char namespace)
			throws IOException {

		DirectoryEntry entry;
		String cmpStr;
		int numberOfArticles = mFile.getArticleCount();
		int beg = mFile.getTitlePtrPos(), end = beg + (numberOfArticles * 4), mid;

		articleName = namespace + "/" + articleName;

		while (beg <= end) {
			mid = beg + 4 * (((end - beg) / 4) / 2);
			entry = getDirectoryInfoAtTitlePosition(mid);
			if (entry == null) {
				return null;
			}
			cmpStr = entry.getNamespace() + "/" + entry.getUrl();
			if (articleName.compareTo(cmpStr) < 0) {
				end = mid - 4;

			} else if (articleName.compareTo(cmpStr) > 0) {
				beg = mid + 4;

			} else {
				return entry;
			}
		}

		return null;

	}

	public ByteArrayOutputStream getArticleData(String articleName, char namespace) throws IOException {

		// search in the cache first, if not found, then call getDirectoryInfo(articleName)

		byte[] buffer = new byte[8];

		DirectoryEntry mainEntry = getDirectoryInfo(articleName, namespace);

		if (mainEntry != null) {

			// Check what kind of an entry was mainEnrty
			if (mainEntry.getClass() == ArticleEntry.class) {

				// Cast to ArticleEntry
				ArticleEntry article = (ArticleEntry) mainEntry;

				// Get the cluster and blob numbers from the article
				int clusterNumber = article.getClusterNumber();
				int blobNumber = article.getBlobnumber();

				// Move to the cluster entry in the clusterPtrPos
				mReader.seek(mFile.getClusterPtrPos() + clusterNumber * 8);

				// Read the location of the cluster
				int clusterPos = mReader
						.readEightLittleEndianBytesValue(buffer);

				// Move to the cluster
				mReader.seek(clusterPos);

				// Read the first byte, for compression information
				int compressionType = mReader.read();

				// Reference declaration
				SingleXZInputStream xzReader = null;
				int firstOffset, numberOfBlobs, offset1,
				offset2,
				location,
				differenceOffset;
				
				ByteArrayOutputStream baos;

				// Check the compression type that was read
				switch (compressionType) {

				// TODO: Read uncompressed data directly
				case 0:
				case 1:
					
					// Read the first 4 bytes to find out the number of artciles
					buffer = new byte[4];

					// Create a dictionary with size 40MiB, the zimlib uses this
					// size while creating

					// Read the first offset
					mReader.read(buffer);

					// The first four bytes are the offset of the zeroth blob
					firstOffset = Utilities
							.toFourLittleEndianInteger(buffer);

					// The number of blobs
					numberOfBlobs = firstOffset / 4;

					// The blobNumber has to be lesser than the numberOfBlobs
					assert blobNumber < numberOfBlobs;


					if (blobNumber == 0) {
						// The first offset is what we read earlier
						offset1 = firstOffset;
					} else {

						location = (blobNumber - 1) * 4;
						Utilities.skipFully(mReader, location);
						mReader.read(buffer);
						offset1 = Utilities.toFourLittleEndianInteger(buffer);
					}

					mReader.read(buffer);
					offset2 = Utilities.toFourLittleEndianInteger(buffer);

					differenceOffset = offset2 - offset1;
					buffer = new byte[differenceOffset];

					Utilities.skipFully(mReader,
							(offset1 - 4 * (blobNumber + 2)));

					mReader.read(buffer, 0, differenceOffset);

					baos = new ByteArrayOutputStream();
					baos.write(buffer, 0, differenceOffset);

					return baos;

				// LZMA2 compressed data
				case 4:

					// Read the first 4 bytes to find out the number of artciles
					buffer = new byte[4];

					// Create a dictionary with size 40MiB, the zimlib uses this
					// size while creating
					xzReader = new SingleXZInputStream(mReader, 4194304);

					// Read the first offset
					xzReader.read(buffer);

					// The first four bytes are the offset of the zeroth blob
					firstOffset = Utilities
							.toFourLittleEndianInteger(buffer);

					// The number of blobs
					numberOfBlobs = firstOffset / 4;

					// The blobNumber has to be lesser than the numberOfBlobs
					assert blobNumber < numberOfBlobs;

					if(blobNumber == 0) {
						// The first offset is what we read earlier
						offset1 = firstOffset;
					} else {

						location = (blobNumber - 1) * 4;
						Utilities.skipFully(xzReader, location);
						xzReader.read(buffer);
						offset1 = Utilities.toFourLittleEndianInteger(buffer);
					}

					xzReader.read(buffer);
					offset2 = Utilities.toFourLittleEndianInteger(buffer);

					differenceOffset = offset2 - offset1;
					buffer = new byte[differenceOffset];

					Utilities.skipFully(xzReader,
							(offset1 - 4 * (blobNumber + 2)));

					xzReader.read(buffer, 0, differenceOffset);

					baos = new ByteArrayOutputStream();
					baos.write(buffer, 0, differenceOffset);

					return baos;

				}
			}
		}

		return null;

	}

	public DirectoryEntry getDirectoryInfoAtTitlePosition(int position)
			throws IOException {

		// Helpers
		int pos;
		byte[] buffer = new byte[8];

		// At the appropriate position in the titlePtrPos
		mReader.seek(position);

		// Get value of article at index
		pos = mReader.readFourLittleEndianBytesValue(buffer);

		// Move to the position in urlPtrPos
		mReader.seek(mFile.getUrlPtrPos() + 8 * pos);

		// Get value of article in urlPtrPos
		pos = mReader.readEightLittleEndianBytesValue(buffer);

		// Go to the location of the directory entry
		mReader.seek(pos);

		int type = mReader.readTwoLittleEndianBytesValue(buffer);

		// Ignore the parameter length
		mReader.read();

		char namespace = (char) mReader.read();
		// System.out.println("Namepsace: " + namespace);

		int revision = mReader.readFourLittleEndianBytesValue(buffer);
		// System.out.println("Revision: " + revision);

		// TODO: Remove redundant if condition code
		// Article or Redirect entry
		if (type == 65535) {

			// System.out.println("MIMEType: " + type);

			int redirectIndex = mReader.readFourLittleEndianBytesValue(buffer);
			// System.out.println("RedirectIndex: " + redirectIndex);

			String url = mReader.readString();
			// System.out.println("URL: " + url);

			String title = mReader.readString();
			title = title.equals("") ? url : title;
			// System.out.println("Title: " + title);

			return new RedirectEntry(type, namespace, revision, redirectIndex,
					url, title, (position - mFile.getUrlPtrPos()) / 8);

		} else {

			// System.out.println("MIMEType: " + mFile.getMIMEType(type));

			int clusterNumber = mReader.readFourLittleEndianBytesValue(buffer);
			// System.out.println("Cluster Number: " + clusterNumber);

			int blobNumber = mReader.readFourLittleEndianBytesValue(buffer);
			// System.out.println("Blob Number: " + blobNumber);

			String url = mReader.readString();
			// System.out.println("URL: " + url);

			String title = mReader.readString();
			title = title.equals("") ? url : title;
			// System.out.println("Title: " + title);

			// Parameter data ignored

			return new ArticleEntry(type, namespace, revision, clusterNumber,
					blobNumber, url, title,
					(position - mFile.getUrlPtrPos()) / 8);
		}

	}

	public ZIMFile getZIMFile() {
		return mFile;
	}
}
