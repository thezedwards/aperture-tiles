/*
 * Copyright (c) 2014 Oculus Info Inc. http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.annotation.index.impl;

import java.io.Serializable;
import java.lang.Number;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import com.oculusinfo.annotation.index.*;
import com.oculusinfo.binning.*;
import com.oculusinfo.binning.impl.*;

public class TiledAnnotationIndexer extends AnnotationIndexer {
	

    public TiledAnnotationIndexer() {
    	_pyramid = new WebMercatorTilePyramid();
    } 
    
    @Override
    public List<AnnotationIndex> getIndices( double x, double y ) {
    	
		List<AnnotationIndex> indices = new LinkedList<AnnotationIndex>();		
		for (int i=0; i<LEVELS; i++) {
			indices.add( getIndex( x, y, i ) );
			//System.out.println( "level " + i + ", index: " + indices.get(i).getValue() );
		}
		return indices;
    }
    
    @Override
    public AnnotationIndex getIndex( double x, double y, int level ) {
		
		TileIndex tile = _pyramid.rootToTile( x,  y, level, BINS );
		BinIndex bin = _pyramid.rootToBin( x,  y, tile );
		
		int bx = tile.getX()*tile.getXBins() + bin.getX();
		int by = tile.getY()*tile.getYBins() + (tile.getYBins()-1)-bin.getY();
    	
		return new AnnotationIndex( interleave( bx, by, level ) );
    }

    private long interleave( long x, long y, int level ) {

        x = (x | (x << SHIFTS[4])) & BITS[4];
        x = (x | (x << SHIFTS[3])) & BITS[3];
        x = (x | (x << SHIFTS[2])) & BITS[2];
        x = (x | (x << SHIFTS[1])) & BITS[1];
        x = (x | (x << SHIFTS[0])) & BITS[0];

        y = (y | (y << SHIFTS[4])) & BITS[4];
        y = (y | (y << SHIFTS[3])) & BITS[3];
        y = (y | (y << SHIFTS[2])) & BITS[2];
        y = (y | (y << SHIFTS[1])) & BITS[1];
        y = (y | (y << SHIFTS[0])) & BITS[0];

        long z = x | (y << 1);

        return z + getKeyLevelOffset(level);
    }

    private long getKeyLevelOffset(int level) {   	
    	if (level == 0) return 0;   	
    	return (long)Math.pow(4, level+LEVELS_EXP) + getKeyLevelOffset( level-1 );
    }
}
