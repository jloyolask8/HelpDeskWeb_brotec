HelpDeskWeb_brotec
==================

HelpDeskWeb_brotec


Pedientes Godesk

- Remover historia del area de texto responder.godesk done. pending betterlife/beltec/brotec.
- facilitar la creacion de clientes contactos, agregar tabla organizacion.
- Implementar drag and drop en Direcciones de correos - respuestas por correos.
http://jqueryui.com/autocomplete/#multiple-remote
http://goodies.pixabay.com/jquery/tag-editor/demo.html
http://brianreavis.github.io/selectize.js/

- add dashboards
- add get started page with instructions on how to proceed.
- cache de notificaciones para no pajear la app.
- implementar logout countdown cuando se detecte inactividad.
- foto de perfil de clientes y agentes =)
- +Inbox FiltroVista by default  
- tags as Vistas in tabs

<h:panelGroup layout="block" styleClass="panel panel-transparent hidden-xs" 
                                  rendered="#{(not empty tagCloudBean.etiquetasByUsuario) and (not casoController.mobileClient)}">
                        <div class="panel-heading">
                            <span class="panel-title"><i class="panel-title-icon fa fa-tags"></i>Mis Tags</span>
                        </div>

                        <div class="panel-body">

                            <ui:repeat value="#{tagCloudBean.etiquetasByUsuario}" var="etiquetaUser">
                                <p:commandLink actionListener="#{tagCloudBean.onEtiquetaSelected(etiquetaUser.tagId)}"
                                               oncomplete="$('.nav-pills, .nav-tabs').tabdrop();"
                                               styleClass="label label-tag label-info" value="#{etiquetaUser}"
                                               update=":vistasList:vistasForm :mainform:messages :form0 :formfilter:panelFilter0:panelFilter">

                                </p:commandLink>&nbsp;
                            </ui:repeat>


                        </div>
                    </h:panelGroup>


- Enter in your credit card information to continue your service beyond the free trial
