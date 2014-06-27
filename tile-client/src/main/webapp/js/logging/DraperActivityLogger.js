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
/* global activityLogger */
define(function (require) {
    "use strict";



    var ActivityLogger = activityLogger,
        logger = new ActivityLogger( "./js/libjs/draper.activity_worker-2.1.1.js" )
                        .echo( true )
                            .testing( true )
                                .mute( ['SYS', 'USER'] );



    function DESCRIPTION_MAP( layerState, fieldName ) {
        return "Setting " + fieldName + " attribute to " + layerState.get( fieldName ) + " for layer " + layerState.getId();
    }

    function ACTION_MAP( fieldName ) {
        switch ( fieldName ) {
            case 'opacity': return 'filter_data';
            case 'enabled': return "filter_data";
            default:        return 'missing_action';
        }
    }

    function WORKFLOW_MAP( fieldName ) {
        switch ( fieldName ) {
            case 'opacity': return logger.WF_EXPLORE;
            case 'enabled': return logger.WF_EXPLORE;
            default:        return logger.WF_OTHER;
        }
    }

    return {

        start: function() {
            logger.mute(['SYS', 'USER']);
        },

        stop: function() {
            logger.unmute(['SYS', 'USER']);
        },

        getListener: function( layerState ) {

            return function( fieldName ) {

                logger.logUserActivity( DESCRIPTION_MAP( layerState, fieldName ), ACTION_MAP( fieldName ), WORKFLOW_MAP( fieldName ) );
            };
        }

    };

});
