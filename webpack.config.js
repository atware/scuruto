'use strict';

var webpack = require('webpack');
var Clean = require('clean-webpack-plugin');

var Path = require('path');
var FileSystem = require("fs");

var DIST = Path.join(__dirname, 'src/main/webapp/assets/dist/');

module.exports = {
    node: {
        fs: "empty"
    },
    entry: {
        style: Path.join(__dirname, 'src/main/webapp/WEB-INF/assets/style.js'),
        script: Path.join(__dirname, 'src/main/webapp/WEB-INF/assets/script.js'),
        ja: Path.join(__dirname, 'src/main/webapp/WEB-INF/assets/ja.js'),
        en: Path.join(__dirname, 'src/main/webapp/WEB-INF/assets/en.js')
    },
    output: {
        path: DIST,
        filename: '[name].[hash].js',
        publicPath: '/assets/dist/'
    },
    resolve: {
        modules: ['node_modules', 'dl_modules']
    },
    module: {
        rules: [
            { test: /\.(jpg|png)$/, loader: "file-loader" },
            { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file-loader" },
            { test: /\.(woff|woff2)$/, loader:"url-loader?prefix=font/&limit=5000" },
            { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=application/octet-stream" },
            { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=image/svg+xml" }
        ]
    },
    devtool: '#source-map',
    plugins: [
        new Clean([DIST]),
        // generate hash
        function() {
            this.plugin("done", function(statsData) {
                var stats = statsData.toJson();
                if (!stats.errors.length) {
                    FileSystem.writeFileSync(
                      Path.join(DIST, 'version.txt'),
                      stats.hash
                    );
                }
            });
        },
        new webpack.optimize.UglifyJsPlugin()
    ]
};