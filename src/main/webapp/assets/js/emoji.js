var emoji = {
  'textcomplete': function textcomplete() {
    $("#body, textarea[name=body]").textcomplete([
      {
        match: /(^|\s):([\-+\w]*)$/,
        search: function (term, callback) {
          callback($.map(emojify.emojiNames, function (emoji) {
            return emoji.indexOf(term) === 0 ? emoji : null;
          }));
        },
        template: function (value) {
          value = value.replace("+", "plus");
          return '<span class="emoji emoji-' + value + '" title=":' + value + ':"></span>' + value;
        },
        replace: function (value) {
          return '$1:' + value + ': ';
        },
        index: 2,
        maxCount: 8
      }
    ]);
  }
};
module.exports = emoji;
emoji.textcomplete();