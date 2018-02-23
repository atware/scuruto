var scuruto = {
  'link_target': function link_target(){
    $("a[href^='http']:not([href*='" + location.hostname + "'])").attr('target', '_blank');
  },
  'sequence_diagram': function sequence_diagram(){
    $('.sequence').sequenceDiagram({theme: 'simple'}).unwrap().children().unwrap();
  },
  'flowchart': function flowchart(){
    $('.flow').flowChart().unwrap().children().unwrap();
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
  },
  'articlify': function articlify(){
    scuruto.link_target();
    scuruto.sequence_diagram();
    scuruto.flowchart();
    scuruto.auto_prettify();
    scuruto.auto_emojify();
  }
};
module.exports = scuruto;