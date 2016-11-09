var sharedocs = {
  'link_target': function link_target(){
    $("a[href^='http']:not([href*='" + location.hostname + "'])").attr('target', '_blank');
  },
  'auto_prettify': function auto_prettify(){
    $('pre code').addClass('prettyprint');
    prettyPrint();
  },
  'auto_emojify': function auto_emojify(){
    emojify.setConfig({
      mode: 'data-uri'
    });
    $('.markdown-content').each(function(){
      emojify.run(this);
    })
  },
  'lightboxfy': function(){
    $('.markdown-content img').each(function(){
      var raw = new Image();
      raw.src=this.src;
      if($('.markdown-content').width() < raw.width){
        var $a = $(this).wrap('<a href="'+this.src+'" data-featherlight="image"></a>').parent();
        $a.featherlight(this.src);
      }
    });
  }
};
module.exports = sharedocs;