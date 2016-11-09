'use strict';

var webpack = require('webpack');
var Clean = require('clean-webpack-plugin');

var Path = require('path');
var FileSystem = require("fs");

var DIST = Path.join(__dirname, 'src/main/webapp/assets/dist/');

module.exports = {
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
        modulesDirectories: ['node_modules', 'dl_modules']
    },
    module: {
        loaders: [
            { test: /\.(jpg|png)$/, loader: "file" },
            { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file" },
            { test: /\.(woff|woff2)$/, loader:"url?prefix=font/&limit=5000" },
            { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=application/octet-stream" },
            { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=image/svg+xml" }
        ]
    },
    devtool: '#source-map',
    plugins: [
        new Clean([DIST]),
        /* 全てjsファイル化した暁にはこれを有効化
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            "window.jQuery": "jquery"
        }),
        */
        // update js hash in layouts/default.ssp
        function() {
            this.plugin("done", function(statsData) {
                var stats = statsData.toJson();
                if (!stats.errors.length) {
                    ['style', 'script', 'ja', 'en'].forEach(function(name) {
                        var layoutFileName = 'src/main/webapp/WEB-INF/layouts/default.ssp';
                        var layoutSource = FileSystem.readFileSync(Path.join(__dirname, layoutFileName), "utf8");

                        var target = new RegExp("<script\\s+src=([\"'])(.+?)" + name + "\\..+?\\.js\\1", "i");
                        var htmlOutput = layoutSource.replace(
                            target,
                            "<script src=$1$2" + name + "." + stats.hash + ".js" + "$1");
                        target = new RegExp("<script\\s+src=([\"'])(.+?)" + name + "\\..+?\\.js\\.map\\1", "i");
                        htmlOutput = htmlOutput.replace(
                            target,
                            "<script src=$1$2" + name + "." + stats.hash + ".js.map" + "$1");

                        FileSystem.writeFileSync(
                            Path.join(__dirname, layoutFileName),
                            htmlOutput);
                    });
                }
            });
        },
        new webpack.optimize.UglifyJsPlugin()
    ]
};