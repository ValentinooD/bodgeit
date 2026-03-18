
function loadfile(filename){
 const filetype = filename.split('.').pop();
    let insert;
    switch (filetype){
     case "js":
         insert=document.createElement('script')
         insert.setAttribute("type","text/javascript")
         insert.setAttribute("src", filename)
         break;
    case 'css':
        insert=document.createElement("link");
        insert.setAttribute("type", "text/css")
        insert.setAttribute("href", filename)
        insert.setAttribute("rel", "stylesheet")
        break;
 }
 if (insert !== undefined)
  document.getElementsByTagName("head")[0].appendChild(insert);
 return false;
}


////The following is from:
//http://stackoverflow.com/questions/316781/how-to-build-query-string-with-javascript

function form_to_params( form )
{
    let output = "";
    const length = form.elements.length;
    let element;

    for (let i = 0; i < length; i++) {
        element = form.elements[i]

        if (element.tagName === 'TEXTAREA') {
            output += "|" + element.name + ":" + element.value;
        } else if (element.tagName === 'INPUT') {
            output += appendOutputForInputTag(element);
        }
    }
    return output.substring(1);
}

function appendOutputForInputTag(element) {
    let output = "";
    switch(element.type){
        case 'radio':
        case 'checkbox':
            if(element.checked && !element.value){
                output = "|" + element.name + ":on";
                break;
            }
            break;
        case 'text':
        case 'hidden':
        case 'password':
            if(element.value)
                output = "|" + element.name + ":" + element.value;
            break;
    }
    return output;
}


function htmlEntities(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}