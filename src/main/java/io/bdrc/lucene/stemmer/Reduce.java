/*
                    Egothor Software License version 2.00
                    Copyright (C) 1997-2004 Leo Galambos.
                 Copyright (C) 2002-2004 "Egothor developers"
                      on behalf of the Egothor Project.
                             All rights reserved.

   This  software  is  copyrighted  by  the "Egothor developers". If this
   license applies to a single file or document, the "Egothor developers"
   are the people or entities mentioned as copyright holders in that file
   or  document.  If  this  license  applies  to the Egothor project as a
   whole,  the  copyright holders are the people or entities mentioned in
   the  file CREDITS. This file can be found in the same location as this
   license in the distribution.

   Redistribution  and  use  in  source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    1. Redistributions  of  source  code  must retain the above copyright
       notice, the list of contributors, this list of conditions, and the
       following disclaimer.
    2. Redistributions  in binary form must reproduce the above copyright
       notice, the list of contributors, this list of conditions, and the
       disclaimer  that  follows  these  conditions  in the documentation
       and/or other materials provided with the distribution.
    3. The name "Egothor" must not be used to endorse or promote products
       derived  from  this software without prior written permission. For
       written permission, please contact leo.galambos@egothor.org
    4. Products  derived  from this software may not be called "Egothor",
       nor  may  "Egothor"  appear  in  their name, without prior written
       permission from leo.galambos@egothor.org.

   In addition, we request that you include in the end-user documentation
   provided  with  the  redistribution  and/or  in the software itself an
   acknowledgement equivalent to the following:
   "This product includes software developed by the Egothor Project.
    http://www.egothor.org/"

   THIS  SOFTWARE  IS  PROVIDED  ``AS  IS''  AND ANY EXPRESSED OR IMPLIED
   WARRANTIES,  INCLUDING,  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY  AND  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
   IN  NO  EVENT  SHALL THE EGOTHOR PROJECT OR ITS CONTRIBUTORS BE LIABLE
   FOR   ANY   DIRECT,   INDIRECT,  INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR
   CONSEQUENTIAL  DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE  GOODS  OR  SERVICES;  LOSS  OF  USE,  DATA, OR PROFITS; OR
   BUSINESS  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER  IN  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
   OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
   IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This  software  consists  of  voluntary  contributions  made  by  many
   individuals  on  behalf  of  the  Egothor  Project  and was originally
   created by Leo Galambos (leo.galambos@egothor.org).

 */

package io.bdrc.lucene.stemmer;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 *  The Reduce object is used to remove gaps in a Trie which stores a
 *  dictionary.
 *  
 *  This product includes software developed by the Egothor Project. http://www.egothor.org/
 *
 * @author    Leo Galambos
 */
public class Reduce {

	public MessageDigest md;
	
    /**
     *  Constructor for the Reduce object.
     */
    public Reduce() {
    	 try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    }

    public void printDebug(Trie t) {
    	System.out.println(t.toString());
    	for (int rowId = 0 ; rowId < t.rows.size() ; rowId++) {
    		final Row r = t.getRow(rowId);
    		System.out.println("row "+rowId+":");
    		System.out.println(r);
    	}
    }
    
    // this optimization is safe in the sense that you can still walk the trie and
    // find the exact same results for all lookups (valid or not)
    
    public Trie optimize(Trie orig) {
    	final Map<Integer,Integer> rowIdMappings = new HashMap<>();
    	Map<BigInteger,Integer> rowMdMappings = new HashMap<>();
    	final int origRowsSize = orig.rows.size();
        
    	//printDebug(orig);
    	
    	int nbChanges = fillMappings(orig, orig.root, rowIdMappings, rowMdMappings);
    	
    	if (nbChanges == 0) {
    		System.out.println("no changes");
    		return orig;
    	}
    	
    	System.out.println("merged "+nbChanges+" of the "+origRowsSize+" rows");
    	
    	rowMdMappings = null; // gc
    	
    	// we now have a mapping but that contains holes, for instance:
    	// if we have mappings 1 -> 3 and 2 -> 3, we have nothing in the rows at index 1 and 2
    	// se we build a second mapping mapping these new indexes to a compact index
    	int curCompactId = 0;
    	final Map<Integer,Integer> compactMapping = new HashMap<>();
    	for (int origRowId = 0 ; origRowId < origRowsSize ; origRowId ++) {
    		if (rowIdMappings.containsKey(origRowId))
    			continue;
    		compactMapping.put(origRowId, curCompactId);
    		curCompactId += 1;
    	}
    	final Vector<Row> newRows = new Vector<Row>(curCompactId);
    	for (int origRowId = 0 ; origRowId < origRowsSize ; origRowId++) {
    		if (rowIdMappings.containsKey(origRowId))
    			continue;
    		final Row newRow = new Remap(orig.getRow(origRowId), compactMapping);
    		newRows.add(newRow);
    	}
    	
        final Trie res = new Trie(orig.forward, compactMapping.get(orig.root), orig.cmds, newRows);
        //printDebug(res);
        return res;
    }

    BigInteger updateRowAndGetMd(final Row r, final Map<Integer,Integer> rowIdMappings) {
    	final String rowStrWithMappings = r.updateAndGetString(rowIdMappings);
    	final byte[] hashBytes = md.digest(rowStrWithMappings.getBytes());
        return new BigInteger(1,hashBytes);
    }
    
    int fillMappings(final Trie orig, final int rowId, final Map<Integer,Integer> rowIdMappings, final Map<BigInteger, Integer> rowMDMappings) {
    	int nbChanges = 0;
    	final Row r = orig.getRow(rowId);
    	final Iterator<Cell> i = r.cells.values().iterator();
        for (; i.hasNext(); ) {
            final Cell c = i.next();
            if (c.ref >= 0 && !rowIdMappings.containsKey(c.ref)) {
                nbChanges += fillMappings(orig, c.ref, rowIdMappings, rowMDMappings);
            }
        }
        final BigInteger rowMd = updateRowAndGetMd(r, rowIdMappings);
        if (!rowMDMappings.containsKey(rowMd)) {
        	rowMDMappings.put(rowMd, rowId);
        } else {
        	rowIdMappings.put(rowId, rowMDMappings.get(rowMd));
        	nbChanges += 1;
        }
        return nbChanges;
    }

    /**
     *  This class is part of the Egothor Project
     *
     * @author    Leo Galambos
     */
    class Remap extends Row {

        public Remap(Row old, final Map<Integer,Integer> compactMappings) {
            super();
            final Iterator<Character> i = old.cells.keySet().iterator();
            for (; i.hasNext(); ) {
                final Character ch = i.next();
                final Cell c = old.at(ch);
                final Cell nc;
                if (c.ref >= 0) {
                    nc = new Cell(c);
                    nc.ref = compactMappings.get(nc.ref);
                } else {
                    nc = new Cell(c);
                }
                cells.put(ch, nc);
            }
        }
    }
}
