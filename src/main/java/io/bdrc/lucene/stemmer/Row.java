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

import java.util.*;
import java.io.*;

/**
 *  The Row class represents a row in a matrix representation of a trie.
 *  
 *  This product includes software developed by the Egothor Project. http://www.egothor.org/
 *
 * @author    Leo Galambos
 */
public class Row {
    TreeMap<Character, Cell> cells = new TreeMap<Character, Cell>();
    int uniformCnt = 0;
    int uniformSkip = 0;


    /**
     *  Construct a Row object from input carried in via the given input
     *  stream.
     *
     * @param  is               the input stream
     * @exception  IOException  if an I/O error occurs
     */
    public Row(DataInput is) throws IOException {
        for (int i = is.readInt(); i > 0; i--) {
            final Character ch = new Character(is.readChar());
            final Cell c = new Cell();
            c.cmd = is.readInt();
            c.ref = is.readInt();
            cells.put(ch, c);
        }
    }


    /**
     *  The default constructor for the Row object.
     */
    public Row() { }


    /**
     *  Construct a Row using the cells of the given Row.
     *
     * @param  old  the Row to copy
     */
    public Row(Row old) {
        cells = old.cells;
    }


    /**
     *  Set the command in the Cell of the given Character to the given
     *  integer.
     *
     * @param  way  the Character defining the Cell
     * @param  cmd  the new command
     */
    public void setCmd(Character way, int cmd) {
        Cell c = at(way);
        if (c == null) {
            c = new Cell();
            c.cmd = cmd;
            cells.put(way, c);
        } else {
            c.cmd = cmd;
        }
    }


    /**
     *  Set the reference to the next row in the Cell of the given
     *  Character to the given integer.
     *
     * @param  way  the Character defining the Cell
     * @param  ref  The new ref value
     */
    public void setRef(Character way, int ref) {
        Cell c = at(way);
        if (c == null) {
            c = new Cell();
            c.ref = ref;
            cells.put(way, c);
        } else {
            c.ref = ref;
        }
    }


    /**
     *  Return the number of cells in use.
     *
     * @return    the number of cells in use
     */
    public int getCells() {
        final Iterator<Character> i = cells.keySet().iterator();
        int size = 0;
        for (; i.hasNext(); ) {
            final Character c = i.next();
            final Cell e = at(c);
            if (e.cmd >= 0 || e.ref >= 0) {
                size++;
            }
        }
        return size;
    }


    /**
     *  Return the number of references (how many transitions) to other
     *  rows.
     *
     * @return    the number of references
     */
    public int getCellsPnt() {
        final Iterator<Character> i = cells.keySet().iterator();
        int size = 0;
        for (; i.hasNext(); ) {
            final Character c = i.next();
            final Cell e = at(c);
            if (e.ref >= 0) {
                size++;
            }
        }
        return size;
    }

    /**
     *  Return the command in the Cell associated with the given Character.
     *
     * @param  way  the Character associated with the Cell holding the
     *      desired command
     * @return      the command
     */
    public int getCmd(final Character way) {
        final Cell c = at(way);
        return (c == null) ? -1 : c.cmd;
    }


    /**
     *  Return the reference to the next Row in the Cell associated with
     *  the given Character.
     *
     * @param  way  the Character associated with the desired Cell
     * @return      the reference, or -1 if the Cell is <tt>null,</tt>
     */
    public int getRef(final Character way) {
        final Cell c = at(way);
        return (c == null) ? -1 : c.ref;
    }


    /**
     *  Write the contents of this Row to the given output stream.
     *
     * @param  os               the output stream
     * @exception  IOException  if an I/O error occurs
     */
    public void store(DataOutput os) throws IOException {
        os.writeInt(cells.size());
        Iterator<Character> i = cells.keySet().iterator();
        for (; i.hasNext(); ) {
            Character c = (Character) i.next();
            Cell e = at(c);
            if (e.cmd < 0 && e.ref < 0) {
                continue;
            }

            os.writeChar(c.charValue());
            os.writeInt(e.cmd);
            os.writeInt(e.ref);
        }
    }
    
    /**
     *  Gets a string representing the Row.
     */
    public String toString() {
    	String res = "";
        for (Iterator<Character> i = cells.keySet().iterator(); i.hasNext(); ) {
            Character ch = i.next();
            Cell c = at(ch);
            res += "[" + ch + ":" + c + "]";
        }
        return res;
    }


    /**
     *  Description of the Method
     *
     * @param  index  Description of the Parameter
     * @return        Description of the Return Value
     */
    Cell at(Character index) {
        return cells.get(index);
    }
}
