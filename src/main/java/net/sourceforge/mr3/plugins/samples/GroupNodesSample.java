/*
 * @(#) 2003/12/23
 * 
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package net.sourceforge.mr3.plugins.samples;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.mr3.plugin.MR3Plugin;

/**
 * @author Takeshi Morita
 */
public class GroupNodesSample extends MR3Plugin {

	public void exec() {
		String ns = "http://mr3.sourceforge.net#";
		Set selectionCells = new HashSet();
		selectionCells.add(ns + "a");
		selectionCells.add(ns + "b");
		selectionCells.add(ns + "c");
		groupRDFNodes(selectionCells);
	}

}
