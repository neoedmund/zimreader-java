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

import org.openzim.ZIMTypes.ZIMFile;
import org.openzim.ZIMTypes.ZIMReader;

public class ZIMTest2 {
	public static void main(String[] args) {
	 
	
		// args[0] is the Zim File's location
		ZIMFile file = new ZIMFile("D:\\10h\\wikipedia_en_all_2016-12.zim");
		System.out.println("cnt="+file.getArticleCount());

		// Associate the Zim File with a Reader
		ZIMReader zReader = new ZIMReader(file);

	 
//			System.out.println(zReader.getArticleData(args[1],'A').toString("utf-8"));
		 
	}
}
