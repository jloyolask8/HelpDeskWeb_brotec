
CKEDITOR.editorConfig = function (config) {
    //var contextPath = '';
    config.scayt_autoStartup = true;
    config.scayt_sLang = 'es_ES';
    var contextPath = getContextPath();
    config.filebrowserImageUploadUrl = contextPath + '/faces/ckeditor/uploadimage';
    
};

function getContextPath() {
   return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}