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
    if ("" != url && !url.endsWith(window.location.pathname)) {
        console.log("Redirecting to '%s' as it's differs from %s ...", url, window.location.pathname)
        f.submit();
    } else {
        console.log("Not redirecting to '%s' from '%s'! ", url, window.location);
    }
}
