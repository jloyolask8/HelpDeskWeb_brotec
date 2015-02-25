HelpDeskWeb_brotec
==================

HelpDeskWeb_brotec


Pedientes Godesk

- separar tours, 1. dashboard, 2. inbox. 3. settings.
- al registrarse preguntar que areas de la empresa desea crear!

en editar area agregar link directo a crear el canal necesario para la comunicación.

- disable tour debug on production.

- disable cache selectively. enable for casos.

- setting nombre de empresa asignarle el valor ingresado en el register.

- explicar la creacion de eventos publicos y mostrar formulario de inscripcion + form new casos embedded.
hacer una pagina de test.

- add button save all in SLA Settings.
- remove the users creator and add a invite users by email, send email and create them an account.

- mejorar el mail con historia, ver "slack" zendesk en jonathan@godesk.cl

- crear caso de prueba cuando se crea la cuenta.

Graficar el customer satisfaction en el dashboard.

- Remover historia del area de texto responder.godesk done. pending betterlife/beltec/brotec.
- facilitar la creacion de clientes contactos, agregar tabla organizacion.
- Implementar drag and drop en Direcciones de correos - respuestas por correos.
http://jqueryui.com/autocomplete/#multiple-remote
http://goodies.pixabay.com/jquery/tag-editor/demo.html
http://brianreavis.github.io/selectize.js/

- add dashboards
- add get started page with instructions on how to proceed.

+Configure your support emails (Skip)
Configure emails to start receiving cases.

+Add UserVoice to your website (Skip)
Definitely setup your portal, but our embedded widgets are what make UserVoice awesome. With them installed you'll not only give your users a way to contact you when there's a problem, but you'll drive self-service of those issues with Instant Answers and also collect in-depth product feedback & customer satisfaction data.
Next: Use the widget builder to customize your widget and then copy the code into your HTML. (Skip)

+Add Custom Fields (Skip)
Need to categorize your tickets by more than just "open" or "closed"? Add a custom fields to ask additional questions of customers, agents or setup metadata fields you can populate via Javascript.

+Double-check who's responsible (Skip)
Currently all your tickets will be assigned to jonathan. Change who tickets are assigned to by editing or adding Rules in Ticket Settings.

+Add Ticket Rules (Skip)
Rules allow you to define actions (like auto reply, or assignment) to take on new tickets that match a specific set of criteria.

+Upload your logo and customize colors (Skip)
Your site is looking pretty drab. Cheer it up by editing your site design to make it feel more like your company.



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



Open/Future items
==================
    Bug 355458: Provide admin user access data from multiple tenants

BUG for sequences numbers
=========================

    Tenant column when part of the entity identifier
        Incorporate sequence generators



