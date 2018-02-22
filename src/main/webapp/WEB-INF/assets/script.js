require('expose-loader?$!expose-loader?jQuery!jquery');   // インラインjsがあるのでProvidePluginじゃなくグローバルにexpose-loader
require('bootstrap');
require('google-code-prettify/src/prettify.js');
require('expose-loader?Favico!favico/js/favico-0.3.5.min.js');
require('expose-loader?emojify!emojify.js');
require('expose-loader?sharedocs!../../assets/js/main.js');
require('jquery-textcomplete');
require('expose-loader?emoji!../../assets/js/emoji.js');
require('expose-loader?store!store');
require('expose-loader?ko!knockout');
require('knockout-jqautocomplete');
require("featherlight/src/featherlight.js");
require("expose-loader?_!underscore");
require("expose-loader?Raphael!raphael");
require("js-sequence-diagrams/sequence-diagram-min.js");
require("flowchart.js");