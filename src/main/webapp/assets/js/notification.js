var notification = function(){
  var csrfToken = $('meta[name=csrf-token]').attr("content");
  $.ajax({
    method: 'GET',
    url: '/notifications',
    dataType: 'json',
    data: {'csrf-token': csrfToken}
  }).done(function(json){
    $('#notification_count').text(json.count);
    if(json.data.length>0){
      $('#notification-items').children().remove();
      $('#notification-default').remove();
      var html = '\
      <li class="notification-contents-list-item"> \
      <a href="/articles/___article___" class="notification-contents-item-link___state___"> \
        <div class="notification-contents-item-icon"> \
          <img src="___image___" /> \
        </div> \
        <div class="notification-contents-item-text"> \
          <span class="sender">___sender___</span>があなたの___target___<span class="title">"___title___"</span>___type1___<span class="action ___type___">___action___</span>しました。 \
          <div class="status">___when___</div> \
        </div> \
      </a> \
      </li>';
      for(var i=0; i < json.data.length; i++){
        var data = createMessageParts(json.data[i]);
        $('#notification-items').append($(html
          .replace(/___article___/, data.article)
          .replace(/___image___/, data.image)
          .replace(/___sender___/, data.sender)
          .replace(/___target___/, data.target)
          .replace(/___title___/, data.title)
          .replace(/___type___/, data.type)
          .replace(/___type1___/, data.type1)
          .replace(/___action___/, data.action)
          .replace(/___when___/, data.when)
          .replace(/___state___/, data.state)
        ));
      }
    }
    var favicon = new Favico({
      animation:'none'
    });
    if (json.count != 0) {
      $('#notification_count').addClass('unchecked');
      $('.notification-surface').on('click', function(){
        $.ajax({
          method: 'POST',
          url: '/notifications',
          dataType: 'json',
          data: {'csrf-token': csrfToken}
        }).done(function(json){
          $('#notification_count').removeClass('unchecked').text(json.count);
          favicon.reset();
        });
      });
      favicon.badge(json.count);
    } else {
      favicon.reset();
    }
    $(document).on('click', function(){$('.dropdownWrapper').css('display', 'none')});
    $('.notification-surface').on('click', function(e){e.stopPropagation();$('.dropdownWrapper').toggle()});
  }).fail(function(xhr, status, error){
    console.log("error during get notifications");
  });

  function createMessageParts(data){
    var msg = {};
    msg.article = data.article_id + (data.fragment_id ? "#comment-" + data.fragment_id : "");
    msg.image = data.image;
    msg.sender = data.sender;
    msg.title = data.title;
    msg.type = data.type;
    if (data.type == "stock") {
      msg.type1 = "を";
      msg.action = "ストック";
    }else if (data.type == "like"){
      msg.type1 = "に";
      msg.action = "いいね!";
    }else{
      msg.type1 = "で";
      msg.action = "コメント";
    }
    if (data.is_commenter){
      msg.target = "コメントした投稿";
    }else{
      msg.target = "投稿";
    }
    msg.when = getRelativeTime(data.when);
    if (data.state) {
      msg.state = "";
    }else{
      msg.state = "unread";
    }
    return msg;
  }

  // http://qiita.com/rev84/items/2a14a804857de27e9c44
  function getRelativeTime(targetDateStr){
    var baseDate = new Date();
    var targetDate = new Date(targetDateStr);
    var elapsedTime = Math.ceil((baseDate.getTime() - targetDate.getTime())/1000);
    var message = null;
    if (elapsedTime < 60) { //  1 分未満
      message =  'たった今';
    } else if (elapsedTime < 120) { //  2 分未満
      message =  '約 1分前';
    } else if (elapsedTime < (60*60)) { //  1 時間未満
      message =  '約' + (Math.floor(elapsedTime / 60) < 10 ? ' ' : '') + Math.floor(elapsedTime / 60) + '分前';
    } else if (elapsedTime < (120*60)) { //  2 時間未満
      message =  '約 1時間前';
    } else if (elapsedTime < (24*60*60)) { //  1 日未満
      message =  '約' + (Math.floor(elapsedTime / 3600) < 10 ? ' ' : '') + Math.floor(elapsedTime / 3600) + '時間前';
    } else if (elapsedTime < (7*24*60*60)) { // 1 週間未満
      message =  '約' + (Math.floor(elapsedTime / 86400) < 10 ? ' ' : '') +Math.floor(elapsedTime / 86400) + '日前';
    } else { // 1 週間以上
      message =  '約' + (Math.floor(elapsedTime / 604800) < 10 ? ' ' : '') + Math.floor(elapsedTime / 604800) + '週間前';
    }
    return message;
  }
};
module.exports = notification;

