
CKEDITOR.editorConfig = function(config) {
 var contextPath = $('.contextPath').html();
 config.filebrowserImageUploadUrl = contextPath+'../../../ckeditor/uploadimage';
};