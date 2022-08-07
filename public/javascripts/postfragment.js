function postFragment(url, postData) {
    var f = document.createElement('form');
    f.action = url;
    f.method = 'POST';

    postData.forEach((value, key) => {
        var i = document.createElement('input');
        i.type = 'hidden';
        i.name = key;
        i.value = value;
        f.appendChild(i);
    });

    document.body.appendChild(f);
    if (window.location.pathname != url && "" != url) {
        f.submit();
    } else {
        console.log("Not redirecting to '%s' from '%s'! ", url, window.location);
    }
}
