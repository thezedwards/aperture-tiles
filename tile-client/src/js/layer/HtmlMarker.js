/*
 * Copyright (c) 2014 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

( function() {

    "use strict";

    var MapUtil = require('../map/MapUtil');

    /**
     * Instantiate a HtmlMarker object.
     * @class HtmlMarker
     * @classdesc A HtmlMarker object that is pinned to an HtmlMarkerLayer.
     */
    function HtmlMarker( x, y, html ) {
        this.x = x;
        this.y = y;
        this.html = html;
        this.icon = new OpenLayers.Icon(
            null,
            new OpenLayers.Size( 2, 2 ),
            new OpenLayers.Pixel( -1, -2 ) );
    }

    HtmlMarker.prototype = {

        /**
         * Activates the HtmlMarker object. This should never be called manually.
         * @memberof HtmlMarker
         * @private
         */
        activate: function() {
            var viewportPx = MapUtil.getViewportPixelFromCoord( this.map, this.x, this.y ),
                lonlat = this.map.olMap.getLonLatFromViewPortPx( viewportPx );
            this.olMarker = new OpenLayers.Marker( lonlat, this.icon.clone() );
            this.layer.olLayer.addMarker( this.olMarker );
			// get marker elem
            var $parent = $( this.olMarker.icon.imageDiv );
            // hide icon element
			$parent.children().css( 'display', 'none' );
            this.$elem = $( this.html );
            // add marker
			$parent.append( this.html );
        },

        /**
         * De-activates the HtmlMarker object. This should never be called manually.
         * @memberof HtmlMarker
         * @private
         */
        deactivate: function() {
            if ( this.olMarker && this.layer.olLayer ) {
                this.layer.olLayer.removeMarker( this.olMarker );
                this.olMarker.destroy();
                this.olMarker = null;
                this.$elem = null;
            }
        }

    };

    module.exports = HtmlMarker;
}());
