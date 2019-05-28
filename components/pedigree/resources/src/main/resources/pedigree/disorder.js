/*
 * Disorder is a class for storing genetic disorder info.
 * These disorders can be attributed to an individual in the Pedigree.
 *
 * @param disorderID the id number for the disorder
 * @param name a string representing the name of the disorder e.g. "Down Syndrome"
 */
define([
        "pedigree/model/helpers"
    ], function(
        Helpers
    ){
    var Disorder = Class.create( {

        initialize: function(disorderID, name, callWhenReady) {
            this._disorderID = disorderID;
            this._name       = name ? name : "loading...";

            if (!name && callWhenReady)
                this.load(callWhenReady);
        },

        /*
         * Returns the disorderID of the disorder
         */
        getDisorderID: function() {
            return this._disorderID;
        },

        /*
         * Returns the name of the disorder
         */
        getName: function() {
            return this._name;
        },

        load: function(callWhenReady) {
            // Convert to fully-prefixed term
            var curie = this._disorderID;
            if (curie.indexOf(':') < 0) {
                if (Helpers.isInt(curie)) {
                    // Treat it like an OMIM term
                    curie = 'MIM:' + curie;
                } else {
                    // Freetext term
                    curie = '';
                    this._name = this._disorderID;
                }
            }

            if (curie) {
                var queryURL = editor.getExternalEndpoint().getDisorderDetailsURL(curie);
                new Ajax.Request(queryURL, {
                    method: "GET",
                    onSuccess: this.onDataReady.bind(this),
                    onFailure: this.onDataError.bind(this),
                    onComplete: callWhenReady ? callWhenReady : {}
                });
            }
        },

        onDataReady : function(response) {
            this._name = this._disorderID;
            try {
                var parsed = JSON.parse(response.responseText);
                if (parsed && parsed.hasOwnProperty("name")) {
                    console.log("LOADED DISORDER: disorder id = " + this._disorderID + ", name = " + parsed.name);
                    this._name = parsed.name;
                } else {
                    console.log("LOADED DISORDER: id = " + this._disorderID + " -> NO DATA");
                }
            } catch (err) {
                console.log("[LOAD DISORDER] Parse error for disorder: " + this._disorderID);
            }
        },

        onDataError : function(response) {
            console.log("[LOAD DISORDER] Data error for disorder: " + this._disorderID);
            // Unable to resolve term, so use id as term name
            this._name = this._disorderID;
        }
    });

    return Disorder;
});