//To  make things more complicated, this file is run through:
//          http://javascriptobfuscator.com/
//Then:
//          http://www.jsobfuscate.com/
// The final result is saved in advanced2.js, which is what is actually 
// referenced from the page. If the person is creative though, they should find
// this file and this entry ;-)
// 
// Author: Doug Logan
// Website: https://www.CyberNinjas.com



//From: http://stackoverflow.com/questions/4583703/jquery-post-request-not-ajax
jQuery(function($) { $.extend({
    form: function(url, data, method) {
        if (method == null) method = 'POST';
        if (data == null) data = {};

        const form = $('<form>').attr({
            method: method,
            action: url
         }).css({
            display: 'none'
         });

        const addData = function(name, data) {
            if ($.isArray(data)) {
                for (let i = 0; i < data.length; i++) {
                    const value = data[i];
                    addData(name + '[]', value);
                }
            } else if (typeof data === 'object') {
                for (var key in data) {
                    if (data.hasOwnProperty(key)) {
                        addData(name + '[' + key + ']', data[key]);
                    }
                }
            } else if (data != null) {
                form.append($('<input>').attr({
                  type: 'hidden',
                  name: String(name),
                  value: String(data)
                }));
            }
        };

        for (const key in data) {
            if (data.hasOwnProperty(key)) {
                addData(key, data[key]);
            }
        }

        return form.appendTo('body');
    }
}); });

//Other Functions

$( document ).ready(function() {
    $( "input[type='button']").click(
            function(e){
                return validateForm(e.currentTarget.form);
            });
});

function validateForm(form){
    const val = prepareInput(form_to_params(form));
    if(val){
        $.form('./advanced.jsp' + ((debug) ? '?debug=true' : ''), { q: val }).submit();
    }   
    return false;
}

function prepareInput(strInput){
    const params = strInput.replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll('\'', '&#39');
    return ((params.length > 0)) ? Aes.Ctr.encrypt(params, key, 128) : false;
}


$(document).ready(function() {

    $( "input[type='text']" ).autocomplete({
      source: function( request, response ) {
          const target = this.element.attr('name');
          $.ajax({
            dataType: "json",
            type : 'POST',
            data: 'q=' + prepareInput( target + ':' + request.term + '|ajax:true') + ((debug) ? '&debug=true' : ''),
            success: function(data) {
                const auto = [];
                $('input.suggest-user').removeClass('ui-autocomplete-loading');  // hide loading image
              $.map( data, function(item) {
                if($.inArray(item[target], auto) < 0)
                    auto.push(item[target]);
              });     
              return response(auto);
          },
          error: function(data) {
              $('input.suggest-user').removeClass('ui-autocomplete-loading');  
          }
        });
      }
    });
});
