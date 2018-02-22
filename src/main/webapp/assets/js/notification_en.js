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
            <span class="sender">___sender___</span> <span class="action ___type___">___action___</span> ___type1___ ___target___ <span class="title">"___title___"</span> ___type3___ \
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
          .replace(/___action___/, data.action)
          .replace(/___type___/, data.type)
          .replace(/___type1___/, data.type1)
          .replace(/___target___/, data.target)
          .replace(/___title___/, data.title)
          .replace(/___type3___/, data.type3)
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
    console.error("error during get notifications");
  });

  function createMessageParts(data){
    var msg = {};
    msg.article = data.article_id + (data.fragment_id ? "#comment-" + data.fragment_id : "");
    msg.image = data.image;
    msg.sender = data.sender;
    msg.title = data.title;
    msg.type = data.type;
    if (data.type == "stock") {
      msg.action = "stocked";
      msg.type1 = "your";
      msg.type3 = "";
    }else if (data.type == "like"){
      msg.action = "liked";
      msg.type1 = "your";
      msg.type3 = "";
    }else{
      msg.action = "commented";
      if (data.is_commenter){
        msg.type1 = "to a";
        msg.type3 = "you commented.";
      }else{
        msg.type1 = "to your";
        msg.type3 = "";
      }
    }
    msg.target = "post";
    if (data.is_commenter){
      msg.type3 = "you commented.";
    }else{
      msg.type3 = "";
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
      message =  'just now';
    } else if (elapsedTime < 120) { //  2 分未満
      message =  'a minute ago';
    } else if (elapsedTime < (60*60)) { //  1 時間未満
      message =  (Math.floor(elapsedTime / 60) < 10 ? ' ' : '') + Math.floor(elapsedTime / 60) + 'minutes ago';
    } else if (elapsedTime < (120*60)) { //  2 時間未満
      message =  'a hour ago';
    } else if (elapsedTime < (24*60*60)) { //  1 日未満
      message =  (Math.floor(elapsedTime / 3600) < 10 ? ' ' : '') + Math.floor(elapsedTime / 3600) + 'hours ago';
    } else if (elapsedTime < (7*24*60*60)) { // 1 週間未満
      message =  (Math.floor(elapsedTime / 86400) < 10 ? ' ' : '') +Math.floor(elapsedTime / 86400) + 'days ago';
    } else { // 1 週間以上
      message =  (Math.floor(elapsedTime / 604800) < 10 ? ' ' : '') + Math.floor(elapsedTime / 604800) + 'weeks ago';
    }
    return message;
  }
};
module.exports = notification;