/*
 * Copyright (c) 2013 Oculus Info Inc.
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

/* JSLint global declarations: these objects don't need to be declared. */
/*global OpenLayers */

/**
 * This module defines the base class for a client render layer. Must be 
 * inherited from for any functionality.
 */
define(function (require) {
    "use strict";



    var Class = require('../../../class'),
        uid = 0,
        HTMLLayer;



    HTMLLayer = Class.extend({
        ClassName: "HTMLLayer",

        /**
         * Constructs a client render layer object
         * @param id the id string for the render layer
         */
        init: function( spec ) {

            this.uid_ = uid++;
            this.map_ = spec.map || null;
            this.xAttr_ = spec.xAttr || null;
            this.yAttr_ = spec.yAttr || null;
            this.idKey_=  spec.idKey || null;
            this.html_ = spec.html || null;
            this.css_ = spec.css || {};

            this.parent_ = null;
            this.children_ = {};

            this.nodesById_ = {};
            this.nodes_ = [];

            this.$root_ = this.createLayerRoot();
            this.map_.getElement().append( this.$root_ );

            // update carousel if map is moving and mouse isn't
            this.map_.on('move', $.proxy( this.onMapUpdate, this ));
        },


        html : function(html) {
            // set the internal html of this layer
            this.html_ = html;
            this.update();
        },


        /**
         * Remove self from parent
         */
        remove : function() {

            if (this.parent_ === null) {
                return;
            }
            // remove child from parent
            delete this.parent_.children[ this.uid_ ];
            // remove parent from child
            this.parent_ = null;
            this.update();
        },


        /**
         * Add layer as child of this layer
         * @param layer
         */
        append : function( layer ) {

            // clear this layer, it is now fully dependent on its parent data
            layer.clear();
            layer.parent_ = this;

            if (layer.$root_) {
                layer.$root_.remove();
                layer.$root_ = null;
            }

            this.children_[layer.uid_] = layer;
            this.update();
        },


        /**
         * Add styling to html
         * @param attribute
         * @param value
         */
        css : function( attribute, value ) {
            if ( $.isPlainObject(attribute) ) {
                $.extend(this.css_, attribute);
            } else {
                this.css_[attribute] = value;
            }
        },


        clear : function() {

            var i;
            // remove all elements, if they exist
            for (i = 0; i < this.nodes_.length; i++) {
                if (this.nodes_[i].$elem) {
                    this.nodes_[i].$elem.remove();
                }
                if (this.nodes_[i].$parent) {
                    this.nodes_[i].$parent.remove();
                }
            }

            this.nodes_ = [];
            this.nodesById_ = {};
            return this;
        },


        evalCss: function(node, index) {

            var result = {},
                key;

            for (key in this.css_) {
                if (this.css_.hasOwnProperty(key)) {
                    // set as value or evaluate function
                    result[key] = ( $.isFunction( this.css_[key] ) )
                        ? $.proxy( this.css_[key], node )(index)
                        : this.css_[key];
                }
            }

            return result;
        },


        createLayerRoot : function() {
            var pos = this.map_.getViewportPixelFromMapPixel( 0, this.map_.getMapHeight() );
            return $('<div class="aperture-client-layer" style="position:absolute; left:'+pos.x+'px; top:' +pos.y+ 'px; width=0px; height=0px"></div>');
        },


        createNodeRoot : function(data) {
            var pos = this.map_.getMapPixelFromCoord( data[this.xAttr_], data[this.yAttr_] );
            return $('<div class="tile-root" style="position:absolute; left:'+pos.x+'px; top:'+ (this.map_.getMapHeight() - pos.y) +'px; width: 0px; height:0px;"></div>');
        },


        getParentNode : function(data) {
            var i, key, found = null;

            if (this.idKey_) {

                key = data[this.idKey_];
                found = this.nodesById_[key];

            } else {
                for (i=0; i<this.parent_.nodes_.length; i++) {
                    if ( this.parent_.nodes_[i].data === data ) {
                        found = this.parent_.nodes_[i].data;
                        break;
                    }
                }
            }
            return found;
        },

        createNode: function( data ) {

            var parentNode,
                $parent;

            if ( !this.parent_ ) {

                // root
                $parent = this.createNodeRoot(data);
                this.$root_.append($parent);

                return {
                    data : data,
                    $parent : $parent,
                    $elem : null
                };

            }

            parentNode = this.getParentNode(data);
            return {
                data : parentNode.data,
                $parent : parentNode.$elem,
                $elem : null
            };

        },


        updateNode: function( node ) {

            var html;
            // clear out root container
            if (node.$elem) {
                node.$elem.remove();
            }

            // create and style html elements
            html =  $.isFunction( this.html_ ) ? $.proxy( this.html_, node.data )() : this.html_;
            node.$elem = $(html);
            node.$elem.css( this.evalCss(node) );
            node.$parent.append( node.$elem );
        },


        onMapUpdate: function() {

            //var i;
            var pos;

            if (this.parent_) {
                this.parent_.onMapUpdate();
                return;
            }

            /*
            // only root will execute the following code
            for (i=0; i<this.nodes_.length; i++) {
                this.positionNode( this.nodes_[i] );
            }
            */
            pos = this.map_.getViewportPixelFromMapPixel( 0, this.map_.getMapHeight() );
            this.$root_.css({
                top: pos.y + "px",
                left: pos.x + "px"
            });
        },


        update :  function() {

            var i;

            // update this layer
            for (i=0; i<this.nodes_.length; i++) {
                this.updateNode( this.nodes_[i] );
            }

            // update all children
            for (i=0; i<this.children_.length; i++) {
                this.children_[i].update();
            }

        },

        /**
         * All data is managed by the parent node, calling this function will result in
         * chain of updates throughout entire hierarchy
         */
        all: function( data ) {
            var i,
                node,
                key;

            if (this.parent_) {
                this.parent_.all(data);
                return;
            }

            // only root will execute the following code
            this.clear();
            for (i=0; i<data.length; i++) {

                node = this.createNode(data[i]);
                this.nodes_.push( node );

                if (this.idKey_) {
                    key = data[i][this.idKey_];
                    this.nodesById_[key] = node;
                }
            }
            this.update();
        },

        /**
         * All data is managed by the parent node, calling this function will result in
         * chain of updates throughout entire hierarchy
         */
        union : function( data ) {

            var i,
                key,
                node,
                exists;

            if (this.parent_) {
                this.parent_.union(data);
                return;
            }

            // only root will execute the following code
            for (i=0; i<data.length; i++) {

                if (this.idKey_) {
                    // if id attribute is specified, use that to cehck duplicates
                    key = data[i][this.idKey_];
                    exists = this.ids_[key] !== undefined;
                } else {
                    // otherwise test object reference
                    exists = this.doesNodeExist( data[i] );
                }

                if ( !exists ) {

                    node = this.createNode(data[i]);
                    this.nodes_.push( node );

                    if (this.idKey_) {
                        this.nodesById_[key] = node;
                    }

                }
            }
            this.update();
        },


        doesNodeExist: function(data) {

            var i;
            for (i=0; i<this.nodes_.length; i++) {
                if ( this.nodes_[i].data === data ) {
                    return true;
                }
            }
            return false;
        },


        /*
        difference : function( data, idKey ) {

            return this;
        },


        intersect : function( data, idKey ) {

            return this;
        },
        */


        redraw: function( data ) {
            return true;
        }


    });

    return HTMLLayer;
});